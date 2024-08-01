package api.security.training.users.login.service.impl;

import java.util.Arrays;

import com.spencerwi.either.Either;

import api.security.training.api.dto.UserLoginRequest;
import api.security.training.token.AccessTokenCreator;
import api.security.training.token.dto.AuthorizationScope;
import api.security.training.users.dao.UserRepository;
import api.security.training.users.login.dto.InvalidCredentialsError;
import api.security.training.users.login.service.UserLoginService;
import api.security.training.users.password.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UserLoginServiceImpl implements UserLoginService {
	private final UserRepository userRepository;
	private final PasswordService passwordService;
	private final AccessTokenCreator accessTokenCreator;

	@Override
	public Either<String, InvalidCredentialsError> performLogin(UserLoginRequest userLoginRequest) {
		log.info("Handling user login request = {}", userLoginRequest);
		var username = userLoginRequest.username();
		var foundUser = userRepository.findByUsername(username);
		if (foundUser.isPresent()) {
			log.info("User found. Verifying password...");
			var actualPasswordHash = foundUser.get().password();
			if (passwordService.isPasswordCorrect(actualPasswordHash, userLoginRequest.password())) {
				log.info("Password is correct!");
				return Either.left(accessTokenCreator.createToken(username, Arrays.asList(AuthorizationScope.values())));
			} else {
				log.warn("Password is wrong...");
				return Either.right(new InvalidCredentialsError());
			}
		} else {
			log.warn("User with such username {} not found...", username);
			return Either.right(new InvalidCredentialsError());
		}
	}
}
