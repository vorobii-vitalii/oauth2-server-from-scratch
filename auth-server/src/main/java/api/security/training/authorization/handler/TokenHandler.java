package api.security.training.authorization.handler;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import api.security.training.api.dto.TokenRequest;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dto.ClientCredentials;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TokenHandler implements Handler {
	private final List<TokenRequestHandler> tokenRequestHandlers;
	private final ClientRegistrationRepository clientRegistrationRepository;

	@Override
	public void handle(@NotNull Context ctx) {
		var tokenRequest = ctx.bodyAsClass(TokenRequest.class);
		var grantType = tokenRequest.grantType();
		var tokenRequestHandler = tokenRequestHandlers.stream()
				.filter(v -> v.canHandleGrantType(grantType))
				.findFirst();
		if (tokenRequestHandler.isEmpty()) {
			log.warn("Grant type {} not supported", grantType);
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Grant type not supported"));
		} else {
			var basicAuthCredentials = ctx.basicAuthCredentials();
			if (basicAuthCredentials == null) {
				log.warn("Client hasn't passed credentials");
				ctx.status(HttpStatus.UNAUTHORIZED);
				ctx.json(List.of("No credentials"));
				return;
			}
			var clientId = basicAuthCredentials.getUsername();
			var clientRegistrationOpt = clientRegistrationRepository.findById(UUID.fromString(clientId));
			if (clientRegistrationOpt.isEmpty()) {
				log.warn("Client by id = {} not exists", clientId);
				ctx.status(HttpStatus.UNAUTHORIZED);
				ctx.json(List.of("Wrong client id or secret"));
				return;
			}
			var clientRegistration = clientRegistrationOpt.get();
			// For now encrypted = not encrypted
			var actualClientSecret = clientRegistration.clientSecretEncrypted();
			var clientSecret = basicAuthCredentials.getPassword();
			if (!Objects.equals(clientSecret, actualClientSecret)) {
				log.warn("Wrong client secret...");
				ctx.status(HttpStatus.UNAUTHORIZED);
				ctx.json(List.of("Wrong client id or secret"));
				return;
			}
			var result = tokenRequestHandler.get().handleTokenRequest(tokenRequest, ClientCredentials.builder()
					.clientId(clientId)
					.clientSecret(clientSecret)
					.build());
			if (result.isLeft()) {
				log.info("Token was successfully generated!");
				ctx.status(HttpStatus.OK);
				ctx.json(result.getLeft());
			} else {
				log.warn("Error on token generation");
				ctx.status(HttpStatus.BAD_REQUEST);
				ctx.json(result.getRight().reason());
			}
		}
	}
}
