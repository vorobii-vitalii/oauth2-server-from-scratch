package api.security.training.handlers;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import api.security.training.api.dto.UserLoginRequest;
import api.security.training.token.AccessTokenCreator;
import api.security.training.token.dto.AuthorizationScope;
import api.security.training.users.login.service.UserCredentialsChecker;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class LoginHandler implements Handler {
	private final UserCredentialsChecker userCredentialsChecker;
	private final AccessTokenCreator accessTokenCreator;
	private final long sessionCookieExpirationMs;

	@Override
	public void handle(@NotNull Context ctx) {
		var userLoginRequest = ctx.bodyAsClass(UserLoginRequest.class);
		var username = userLoginRequest.username();
		var areCredentialsCorrect = userCredentialsChecker.areCredentialsCorrect(username, userLoginRequest.password());
		if (areCredentialsCorrect) {
			var accessToken = accessTokenCreator.createToken(username, Arrays.asList(AuthorizationScope.values()));
			ctx.cookie("Session", accessToken, (int) sessionCookieExpirationMs);
			ctx.status(HttpStatus.OK);
		} else {
			log.warn("Password is wrong...");
			ctx.status(HttpStatus.UNAUTHORIZED);
			ctx.json(List.of("Wrong password"));
		}
	}

}
