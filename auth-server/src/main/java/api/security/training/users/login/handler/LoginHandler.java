package api.security.training.users.login.handler;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import api.security.training.authorization.domain.AuthorizationScope;
import api.security.training.token.AccessTokenCreator;
import api.security.training.users.dao.UserRepository;
import api.security.training.users.login.dto.UserLoginRequest;
import api.security.training.users.password.PasswordService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class LoginHandler implements Handler {
	private final UserRepository userRepository;
	private final PasswordService passwordService;
	private final AccessTokenCreator accessTokenCreator;
	private final long sessionCookieExpirationMs;

	@Override
	public void handle(@NotNull Context ctx) {
		var userLoginRequest = ctx.bodyAsClass(UserLoginRequest.class);
		log.info("Handling user login request = {}", userLoginRequest);
		var username = userLoginRequest.username();
		var foundUser = userRepository.findByUsername(username);
		if (foundUser.isPresent()) {
			log.info("User found. Verifying password...");
			var actualPasswordHash = foundUser.get().password();
			if (passwordService.isPasswordCorrect(actualPasswordHash, userLoginRequest.password())) {
				log.info("Password is correct!");
				// Generate JWT, sign it and set in cookie. STRICT TRUE, HTTP ONLY
				ctx.cookie(
						"Session",
						accessTokenCreator.createToken(username, Arrays.asList(AuthorizationScope.values())),
						(int) sessionCookieExpirationMs
				);
				ctx.status(HttpStatus.OK);
			} else {
				log.warn("Password is wrong...");
				ctx.status(HttpStatus.UNAUTHORIZED);
				ctx.json(List.of("Wrong password"));
			}
		} else {
			log.warn("User with such username {} not found...", username);
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Such user not found"));
		}
	}

}
