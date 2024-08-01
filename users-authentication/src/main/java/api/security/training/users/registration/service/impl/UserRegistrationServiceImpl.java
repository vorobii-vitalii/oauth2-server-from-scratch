package api.security.training.users.registration.service.impl;

import java.util.UUID;
import java.util.function.Supplier;

import com.spencerwi.either.Either;

import api.security.training.api.dto.UserRegistrationRequest;
import api.security.training.users.dao.UserRepository;
import api.security.training.users.domain.User;
import api.security.training.users.password.PasswordService;
import api.security.training.users.registration.dto.UserAlreadyExists;
import api.security.training.users.registration.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UserRegistrationServiceImpl implements UserRegistrationService {
	private final PasswordService passwordService;
	private final UserRepository userRepository;
	private final Supplier<UUID> uuidSupplier;

	@Override
	public Either<Void, UserAlreadyExists> performRegistration(UserRegistrationRequest registrationRequest) {
		log.info("Handling registration request {}", registrationRequest);
		var username = registrationRequest.username();
		boolean doesExist = userRepository.existsByUsername(username);
		if (doesExist) {
			log.warn("User with username = {} already exists", username);
			return Either.right(new UserAlreadyExists());
		} else {
			log.info("User by username = {} doesn't exist yet. Creating...", username);
			var newUser = toUser(registrationRequest);
			userRepository.save(newUser);
			log.info("User created!");
			return Either.left(null);
		}
	}

	private User toUser(UserRegistrationRequest userRegistrationRequest) {
		return User.builder()
				.userId(uuidSupplier.get())
				.username(userRegistrationRequest.username())
				.firstName(userRegistrationRequest.firstName())
				.lastName(userRegistrationRequest.lastName())
				.phoneNumber(userRegistrationRequest.phoneNumber())
				.password(passwordService.hashPassword(userRegistrationRequest.password()))
				.build();
	}

}
