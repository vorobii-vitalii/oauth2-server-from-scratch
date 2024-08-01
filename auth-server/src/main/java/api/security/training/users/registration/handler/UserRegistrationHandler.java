package api.security.training.users.registration.handler;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.relational.core.query.Query;

import api.security.training.UUIDSupplier;
import api.security.training.users.dao.UserRepository;
import api.security.training.users.domain.User;
import api.security.training.users.password.PasswordService;
import api.security.training.api.dto.UserRegistrationRequest;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserRegistrationHandler implements Handler {
	private final PasswordService passwordService;
	private final UserRepository userRepository;
	private final UUIDSupplier uuidSupplier;

	@Override
	public void handle(@NotNull Context ctx) {
		var registrationRequest = ctx.bodyAsClass(UserRegistrationRequest.class);
		log.info("Handling registration request {}", registrationRequest);
		var username = registrationRequest.username();
		boolean doesExist = userRepository.existsByUsername(username);
		if (doesExist) {
			log.warn("User with username = {} already exists", username);
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Such user already exists..."));
		} else {
			log.info("User by username = {} doesn't exist yet. Creating...", username);
			var newUser = toUser(registrationRequest);
			try {
				userRepository.save(newUser);
				log.info("User created!");
				ctx.status(HttpStatus.CREATED);
			}
			catch (DataAccessException error) {
				log.warn("Error on user persist", error);
				ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
				ctx.json("Server error");
			}
		}
	}

	private User toUser(UserRegistrationRequest userRegistrationRequest) {
		return User.builder()
				.userId(uuidSupplier.createUUID())
				.username(userRegistrationRequest.username())
				.firstName(userRegistrationRequest.firstName())
				.lastName(userRegistrationRequest.lastName())
				.phoneNumber(userRegistrationRequest.phoneNumber())
				.password(passwordService.hashPassword(userRegistrationRequest.password()))
				.build();
	}

	private @NotNull Query queryByUsername(String username) {
		return query(where(User.USERNAME).is(username));
	}

}
