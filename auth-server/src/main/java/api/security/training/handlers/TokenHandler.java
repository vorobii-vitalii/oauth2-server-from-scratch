package api.security.training.handlers;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import api.security.training.api.dto.TokenRequest;
import api.security.training.authorization.dto.ClientCredentials;
import api.security.training.authorization.service.TokenRequestService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TokenHandler implements Handler {
	private final TokenRequestService tokenRequestService;

	@Override
	public void handle(@NotNull Context ctx) {
		var tokenRequest = ctx.bodyAsClass(TokenRequest.class);
		var basicAuthCredentials = ctx.basicAuthCredentials();
		if (basicAuthCredentials == null) {
			log.warn("Client hasn't passed credentials");
			ctx.status(HttpStatus.UNAUTHORIZED);
			ctx.json(List.of("No credentials"));
			return;
		}
		var clientCredentials = ClientCredentials.builder()
				.clientId(basicAuthCredentials.getUsername())
				.clientSecret(basicAuthCredentials.getPassword())
				.build();
		var tokenResult = tokenRequestService.handleTokenRequest(tokenRequest, clientCredentials);
		if (tokenResult.isOk()) {
			ctx.status(HttpStatus.OK);
			ctx.json(tokenResult.getResult());
		} else {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(tokenResult.getException().getMessage());
		}
	}
}
