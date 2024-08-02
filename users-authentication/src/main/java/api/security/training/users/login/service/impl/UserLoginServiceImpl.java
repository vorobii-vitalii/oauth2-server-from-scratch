package api.security.training.users.login.service.impl;

import java.util.Arrays;

import com.spencerwi.either.Either;

import api.security.training.api.dto.UserLoginRequest;
import api.security.training.token.AccessTokenCreator;
import api.security.training.token.dto.AuthorizationScope;
import api.security.training.users.login.dto.InvalidCredentialsError;
import api.security.training.users.login.service.UserCredentialsChecker;
import api.security.training.users.login.service.UserLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UserLoginServiceImpl implements UserLoginService {
	private final UserCredentialsChecker userCredentialsChecker;
	private final AccessTokenCreator accessTokenCreator;

	@Override
	public Either<String, InvalidCredentialsError> performLogin(UserLoginRequest userLoginRequest) {
		log.info("Handling user login request = {}", userLoginRequest);
		var username = userLoginRequest.username();
		boolean areCredentialsCorrect = userCredentialsChecker.areCredentialsCorrect(username, userLoginRequest.password());
		if (areCredentialsCorrect) {
			log.info("Password is correct!");
			return Either.left(accessTokenCreator.createToken(username, Arrays.asList(AuthorizationScope.values())));
		}
		log.warn("Invalid credentials");
		return Either.right(new InvalidCredentialsError());
	}
}
