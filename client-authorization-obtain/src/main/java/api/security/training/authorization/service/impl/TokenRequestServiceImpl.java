package api.security.training.authorization.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.spencerwi.either.Result;

import api.security.training.api.dto.TokenRequest;
import api.security.training.api.dto.TokenResponse;
import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dto.ClientCredentials;
import api.security.training.authorization.service.TokenRequestService;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TokenRequestServiceImpl implements TokenRequestService {
	private final ClientRegistrationRepository clientRegistrationRepository;
	private final List<TokenRequestHandler> tokenRequestHandlers;

	@Override
	public Result<TokenResponse> handleTokenRequest(TokenRequest tokenRequest, ClientCredentials clientCredentials) {
		var grantType = tokenRequest.grantType();
		var tokenRequestHandler = tokenRequestHandlers.stream()
				.filter(v -> v.canHandleGrantType(grantType))
				.findFirst();
		if (tokenRequestHandler.isEmpty()) {
			log.warn("Grant type {} not supported", grantType);
			return Result.err(new IllegalArgumentException("Grant type not supported"));
		}
		var clientId = clientCredentials.clientId();
		var clientRegistrationOpt = clientRegistrationRepository.findById(UUID.fromString(clientId));
		if (clientRegistrationOpt.isEmpty()) {
			log.warn("Client by id = {} not exists", clientId);
			return Result.err(new IllegalArgumentException("Client not exists"));
		}
		var clientRegistration = clientRegistrationOpt.get();
		// For now encrypted = not encrypted
		var actualClientSecret = clientRegistration.clientSecretEncrypted();
		var clientSecret = clientCredentials.clientSecret();
		if (!Objects.equals(clientSecret, actualClientSecret)) {
			log.warn("Wrong client secret...");
			return Result.err(new IllegalArgumentException("Wrong client credentials"));
		}
		var result = tokenRequestHandler.get().handleTokenRequest(tokenRequest, clientId);
		if (result.isLeft()) {
			log.info("Token was successfully generated!");
			return Result.ok(result.getLeft());
		} else {
			log.warn("Error on token generation");
			return Result.err(new IllegalStateException(result.getRight().reason()));
		}
	}
}
