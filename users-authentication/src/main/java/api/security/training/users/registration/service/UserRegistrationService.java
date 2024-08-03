package api.security.training.users.registration.service;

import java.util.UUID;

import com.spencerwi.either.Result;

import api.security.training.api.dto.UserRegistrationRequest;

public interface UserRegistrationService {
	Result<UUID> performRegistration(UserRegistrationRequest userRegistrationRequest);
}
