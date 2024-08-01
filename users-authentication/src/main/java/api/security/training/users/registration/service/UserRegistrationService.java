package api.security.training.users.registration.service;

import com.spencerwi.either.Either;

import api.security.training.api.dto.UserRegistrationRequest;
import api.security.training.users.registration.dto.UserAlreadyExists;

public interface UserRegistrationService {
	Either<Void, UserAlreadyExists> performRegistration(UserRegistrationRequest userRegistrationRequest);
}
