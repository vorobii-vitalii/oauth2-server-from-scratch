package api.security.training.users.auth;

import org.jetbrains.annotations.NotNull;

import api.security.training.token.TokenInfoReader;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UserAuthenticationFilter implements Handler {
	private static final String SESSION_COOKIE = "Session";

	private final TokenInfoReader tokenInfoReader;
	private final Handler handlerOnAuthenticationFailure;

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		var sessionToken = ctx.cookie(SESSION_COOKIE);
		if (sessionToken == null) {
			handlerOnAuthenticationFailure.handle(ctx);
			return;
		}
		var tokenInfo = tokenInfoReader.readTokenInfo(sessionToken);
		if (tokenInfo.isExpired()) {
			handlerOnAuthenticationFailure.handle(ctx);
		}
	}
}
