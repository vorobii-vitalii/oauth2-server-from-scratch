package api.security.training.users.auth;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import api.security.training.exception.AuthenticationRequiredException;
import api.security.training.token.RequestTokenExtractor;
import api.security.training.token.TokenInfoReader;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UserAuthenticationFilter implements Handler {
	private final TokenInfoReader tokenInfoReader;
	private final RequestTokenExtractor requestTokenExtractor;
	private final Function<Context, AuthenticationRequiredException> errorFactory;

	@Override
	public void handle(@NotNull Context ctx) {
		var sessionToken = requestTokenExtractor.extractTokenFromRequest(ctx);
		if (sessionToken.isEmpty()) {
			log.warn("Session cookie absent");
			throw errorFactory.apply(ctx);
		}
		var tokenInfo = tokenInfoReader.readTokenInfo(sessionToken.get());
		if (tokenInfo.isExpired()) {
			log.warn("Session cookie expired!");
			throw errorFactory.apply(ctx);
		} else {
			log.info("Session cookie not expired yet...");
		}
	}
}
