package api.security.training.users.auth;

import org.jetbrains.annotations.NotNull;

import api.security.training.exception.AuthenticationRequiredException;
import api.security.training.RequestTokenExtractor;
import api.security.training.token.AccessTokenInfoReader;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UserAuthenticationFilter implements Handler {
	private final AccessTokenInfoReader accessTokenInfoReader;
	private final RequestTokenExtractor requestTokenExtractor;

	@Override
	public void handle(@NotNull Context ctx) {
		var sessionToken = requestTokenExtractor.extractTokenFromRequest(ctx);
		if (sessionToken.isEmpty()) {
			log.warn("Session cookie absent");
			throw new AuthenticationRequiredException(ctx.fullUrl());
		}
		var tokenInfo = accessTokenInfoReader.readTokenInfo(sessionToken.get());
		if (tokenInfo.isExpired()) {
			log.warn("Session cookie expired!");
			throw new AuthenticationRequiredException(ctx.fullUrl());
		} else {
			log.info("Session cookie not expired yet...");
		}
	}
}
