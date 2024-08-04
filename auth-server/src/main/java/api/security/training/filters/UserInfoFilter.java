package api.security.training.filters;

import org.jetbrains.annotations.NotNull;

import api.security.training.request.RequestTokenExtractor;
import api.security.training.request.RequestParameterService;
import api.security.training.request.dto.RequestParameters;
import api.security.training.token.AccessTokenInfoReader;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UserInfoFilter implements Handler {
	private final AccessTokenInfoReader accessTokenInfoReader;
	private final RequestTokenExtractor requestTokenExtractor;
	private final RequestParameterService requestParameterService;

	@Override
	public void handle(@NotNull Context ctx) {
		var sessionToken = requestTokenExtractor.extractTokenFromRequest(ctx);
		if (sessionToken.isEmpty()) {
			log.warn("Session cookie absent");
			requestParameterService.set(ctx, RequestParameters.IS_AUTH_SESSION_ACTIVE, false);
			return;
		}
		var tokenInfo = accessTokenInfoReader.readTokenInfo(sessionToken.get());
		log.info("Token info = {}", tokenInfo);
		if (tokenInfo.isExpired()) {
			log.warn("Session cookie expired!");
			requestParameterService.set(ctx, RequestParameters.IS_AUTH_SESSION_ACTIVE, false);
		} else {
			log.info("Session cookie not expired yet...");
			requestParameterService.set(ctx, RequestParameters.IS_AUTH_SESSION_ACTIVE, true);
			requestParameterService.set(ctx, RequestParameters.USERNAME, tokenInfo.username());
		}
	}

}
