package api.security.training.authorization.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import com.spencerwi.either.Either;
import com.spencerwi.either.Result;

import api.security.training.authorization.AuthorizationRedirectStrategy;
import api.security.training.authorization.service.ObtainResourceOwnerConsentService;
import api.security.training.authorization.dao.AuthorizationRequestRepository;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.authorization.dto.ResourceOwnerAuthorizationRequest;
import api.security.training.authorization.dto.ResourceOwnerConsentRequest;
import api.security.training.authorization.utils.URIParametersAppender;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
import api.security.training.token.dto.AuthorizationScope;
import api.security.training.token.exception.InvalidScopeException;
import api.security.training.token.utils.ScopesParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ObtainResourceOwnerConsentServiceImpl implements ObtainResourceOwnerConsentService {
	private final AuthorizationRequestRepository authorizationRequestRepository;
	private final ClientRegistrationRepository clientRegistrationRepository;
	private final Supplier<UUID> uuidSupplier;
	private final List<AuthorizationRedirectStrategy> authorizationRedirectStrategies;
	private final URIParametersAppender uriParametersAppender;

	@Override
	public Result<Either<ResourceOwnerConsentRequest, String>> obtainResourceOwnerConsent(ResourceOwnerAuthorizationRequest request) {
		log.info("Handling resource owner auth request = {}", request);
		if (Objects.isNull(request.clientId())) {
			log.warn("Client id is null...");
			return Result.err(new IllegalArgumentException("Client id is null"));
		}
		var clientId = UUID.fromString(request.clientId());
		var foundClientRegistration = clientRegistrationRepository.findById(clientId);
		if (foundClientRegistration.isEmpty()) {
			log.warn("Client not found...");
			return Result.err(new IllegalArgumentException("Client not found"));
		}
		var clientRegistration = foundClientRegistration.get();
		// Compare redirect_uri with stored if present
		var redirectURI = coalesce(request.redirectURI(), clientRegistration.redirectURL());
		if (!Objects.equals(redirectURI, clientRegistration.redirectURL())) {
			log.warn("Redirect URI specified by client is wrong! {} != {}", redirectURI, clientRegistration.redirectURL());
			return Result.err(new IllegalArgumentException("Wrong redirect_uri"));
		}
		var state = request.state();
		if (Objects.isNull(request.responseType())) {
			log.warn("Response type not specified");
			return Result.ok(Either.right(createErrorRedirectionURI(redirectURI, state,
					"invalid_request",
					"Response type not specified"
			)));
		}
		// Check if response type is supported by any strategy
		if (isResponseTypeNotSupported(request.responseType())) {
			log.warn("Response type {} not supported", request.responseType());
			return Result.ok(Either.right(createErrorRedirectionURI(redirectURI, state,
					"unsupported_response_type",
					"Response type not supported"
			)));
		}
		try {
			var scopeList = ScopesParser.parseAuthorizationScopes(request.scope()).orElseGet(() -> Arrays.asList(AuthorizationScope.values()));
			var authorizationRequestSaveResult = Result.attempt(() -> authorizationRequestRepository.save(AuthorizationRequest.builder()
					.id(uuidSupplier.get())
					.clientId(clientId)
					.scope(request.scope())
					.state(state)
					.responseType(request.responseType())
					.username(request.username())
					.redirectURL(redirectURI)
					.build()));
			if (authorizationRequestSaveResult.isErr()) {
				log.warn("Error occurred on save of auth request to DB", authorizationRequestSaveResult.getException());
				return Result.ok(Either.right(
						createErrorRedirectionURI(redirectURI, state, "server_error", "Server error")));
			}
			log.info("Successfully inserted new authorization request {}", authorizationRequestSaveResult.getResult());
			return Result.ok(Either.left(
					ResourceOwnerConsentRequest.builder()
							.clientName(clientRegistration.clientName())
							.clientDescription(clientRegistration.clientDescription())
							.scopeList(scopeList.stream().map(AuthorizationScope::getDisplayName).toList())
							.authorizationRequestId(authorizationRequestSaveResult.getResult().id().toString())
							.build()
			));
		}
		catch (InvalidScopeException e) {
			log.warn("Failed to parse scopes", e);
			return Result.ok(Either.right(
					createErrorRedirectionURI(redirectURI, state, "invalid_scope", "Invalid scopes")));
		}
	}

	private boolean isResponseTypeNotSupported(String responseType) {
		return authorizationRedirectStrategies.stream().noneMatch(v -> v.canHandleResponseType(responseType));
	}

	/**
	 * @param originalURI - OAuth2 client redirect URI
	 * @param state - state (nullable)
	 * @return Redirection with error parameters
	 */
	private String createErrorRedirectionURI(String originalURI, String state, String errorType, String errorDescription) {
		Map<String, String> parameters = new HashMap<>();
		if (state != null) {
			parameters.put("state", state);
		}
		parameters.put("error", errorType);
		parameters.put("error_description", errorDescription);
		return uriParametersAppender.appendParameters(originalURI, parameters);
	}

	@SafeVarargs
	private <T> T coalesce(T... args) {
		for (T arg : args) {
			if (arg != null) {
				return arg;
			}
		}
		return null;
	}

}
