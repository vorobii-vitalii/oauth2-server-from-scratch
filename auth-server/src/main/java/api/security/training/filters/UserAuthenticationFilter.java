package api.security.training.filters;

import org.jetbrains.annotations.NotNull;

import api.security.training.exception.AuthenticationRequiredException;
import api.security.training.params.RequestParameterService;
import api.security.training.params.RequestParameters;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UserAuthenticationFilter implements Handler {
	private final RequestParameterService requestParameterService;

	@Override
	public void handle(@NotNull Context ctx) {
		if (!Boolean.TRUE.equals(requestParameterService.get(ctx, RequestParameters.IS_AUTH_SESSION_ACTIVE))) {
			throw new AuthenticationRequiredException(ctx.fullUrl());
		}
	}
}
