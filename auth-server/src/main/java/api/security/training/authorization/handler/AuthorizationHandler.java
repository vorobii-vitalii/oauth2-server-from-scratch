package api.security.training.authorization.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;

import api.security.training.authorization.AuthorizationRedirectHandler;
import api.security.training.authorization.dao.AuthorizationRequestRepository;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.token.dto.AuthorizationScope;
import api.security.training.token.exception.InvalidScopeException;
import api.security.training.token.utils.ScopesParser;
import api.security.training.client_registration.UUIDSupplier;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
import api.security.training.client_registration.RequestTokenExtractor;
import api.security.training.token.AccessTokenInfoReader;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AuthorizationHandler implements Handler {
	public static final String RESPONSE_TYPE = "response_type";
	public static final String CLIENT_ID = "client_id";
	public static final String SCOPE = "scope";
	public static final String STATE = "state";

	private final RequestTokenExtractor requestTokenExtractor;
	private final AccessTokenInfoReader accessTokenInfoReader;
	private final AuthorizationRequestRepository authorizationRequestRepository;
	private final ClientRegistrationRepository clientRegistrationRepository;
	private final UUIDSupplier uuidSupplier;
	private final List<AuthorizationRedirectHandler> authorizationRedirectHandlers;

	@Override
	public void handle(@NotNull Context ctx) {
		// TODO: redirect_uri
		var clientIdParamValue = ctx.queryParam(CLIENT_ID);
		if (clientIdParamValue == null) {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Client id not specified"));
			return;
		}
		// code, token etc
		var responseType = ctx.queryParam(RESPONSE_TYPE);
		if (responseType == null) {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Response type not specified"));
			return;
		}
		if (authorizationRedirectHandlers.stream().noneMatch(v -> v.canHandleResponseType(responseType))) {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Response type not supported"));
			return;
		}
		var scope = ctx.queryParam(SCOPE);
		var state = ctx.queryParam(STATE);
		var clientId = UUID.fromString(clientIdParamValue);

		var authorizationRequestId = uuidSupplier.createUUID();
		try {
			var scopeList = ScopesParser.parseAuthorizationScopes(scope).orElseGet(() -> Arrays.asList(AuthorizationScope.values()));
			var token = requestTokenExtractor.extractTokenFromRequest(ctx).orElseThrow();
			// TODO: Set by filter to reduce latency
			var username = accessTokenInfoReader.readTokenInfo(token).username();

			authorizationRequestRepository.findById(clientId);

			var clientRegistrationOpt = clientRegistrationRepository.findById(clientId);

			if (clientRegistrationOpt.isEmpty()) {
				log.warn("Client with id = {} not found...", clientId);
				ctx.status(HttpStatus.BAD_REQUEST);
				ctx.json(List.of("Client id not valid"));
				return;
			}
			var clientRegistration = clientRegistrationOpt.get();
			var authorizationRequest = AuthorizationRequest.builder()
					.id(authorizationRequestId)
					.clientId(clientId)
					.scope(scope)
					.state(state)
					.responseType(responseType)
					.username(username)
					.redirectURL(clientRegistration.redirectURL())
					.build();
			try {
				authorizationRequestRepository.save(authorizationRequest);
				log.info("Successfully inserted new authorization request {}", authorizationRequest);
				ctx.status(HttpStatus.OK);
				ctx.render("provide-consent-page.jte", Map.of(
						"clientName", clientRegistration.clientName(),
						"clientDescription", clientRegistration.clientDescription(),
						"scopeList", scopeList.stream().map(AuthorizationScope::getDisplayName).toList(),
						"authorizationRequestId", authorizationRequestId.toString()
				));
			}
			catch (DataAccessException error) {
				log.error("Error occurred on creation of registration request", error);
				ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
				ctx.json(List.of("Server error"));
			}
		}
		catch (InvalidScopeException e) {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Scope is invalid"));
		}

	}

}
