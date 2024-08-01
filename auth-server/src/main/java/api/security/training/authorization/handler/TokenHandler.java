package api.security.training.authorization.handler;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import api.security.training.api.dto.TokenRequest;
import api.security.training.authorization.TokenRequestHandler;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
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

//	grant_type
//	REQUIRED. Value MUST be set to "authorization_code".
//	code
//	REQUIRED. The authorization code received from the
//	authorization server.
//	redirect_uri
//	REQUIRED, if the "redirect_uri" parameter was included in the
//	authorization request as described in Section 4.1.1, and their
//	values MUST be identical.
//	client_id
//	REQUIRED, if the client is not authenticating with the
//	authorization server as described in Section 3.2.1.

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
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
			tokenRequestHandler.get().handle(ctx);
		}
	}
}
