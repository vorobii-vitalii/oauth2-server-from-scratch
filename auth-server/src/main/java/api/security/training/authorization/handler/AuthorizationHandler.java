package api.security.training.authorization.handler;

import org.jetbrains.annotations.NotNull;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class AuthorizationHandler implements Handler {
	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		// code, token etc
		final String responseType = ctx.queryParam("response_type");

	}
}
