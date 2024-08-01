package api.security.training.users.login.service;

import com.spencerwi.either.Either;

import api.security.training.api.dto.UserLoginRequest;
import api.security.training.users.login.dto.InvalidCredentialsError;

public interface UserLoginService {
	Either<String, InvalidCredentialsError> performLogin(UserLoginRequest userLoginRequest);
}
