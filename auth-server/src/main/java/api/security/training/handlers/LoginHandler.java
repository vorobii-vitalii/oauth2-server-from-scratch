package api.security.training.handlers;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import api.security.training.api.dto.UserLoginRequest;
import api.security.training.users.login.service.UserLoginService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class LoginHandler implements Handler {
	private final UserLoginService userLoginService;
	private final long sessionCookieExpirationMs;

	@Override
	public void handle(@NotNull Context ctx) {
		var userLoginRequest = ctx.bodyAsClass(UserLoginRequest.class);
		var result = userLoginService.performLogin(userLoginRequest);
		if (result.isLeft()) {
			ctx.cookie("Session", result.getLeft(), (int) sessionCookieExpirationMs);
			ctx.status(HttpStatus.OK);
		} else {
			log.warn("Password is wrong...");
			ctx.status(HttpStatus.UNAUTHORIZED);
			ctx.json(List.of("Wrong password"));
		}
	}

}
