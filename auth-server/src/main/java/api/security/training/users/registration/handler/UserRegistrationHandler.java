package api.security.training.users.registration.handler;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Query;

import api.security.training.client_registration.UUIDSupplier;
import api.security.training.users.domain.User;
import api.security.training.users.password.PasswordService;
import api.security.training.users.registration.dto.UserRegistrationRequest;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class UserRegistrationHandler implements Handler {
	private final PasswordService passwordService;
	private final R2dbcEntityOperations entityOperations;
	private final UUIDSupplier uuidSupplier;

	@Override
	public void handle(@NotNull Context ctx) {
		var registrationRequest = ctx.bodyAsClass(UserRegistrationRequest.class);
		log.info("Handling registration request {}", registrationRequest);
		var username = registrationRequest.username();
		ctx.future(entityOperations.exists(queryByUsername(username), User.class)
				.flatMap(doesExist -> {
					if (doesExist) {
						log.warn("User with username = {} already exists", username);
						ctx.status(HttpStatus.BAD_REQUEST);
						ctx.json(List.of("Such user already exists..."));
						return Mono.empty();
					} else {
						log.info("User by username = {} doesn't exist yet. Creating...", username);
						var newUser = toUser(registrationRequest);
						return entityOperations.insert(newUser)
								.doOnNext(createdUser -> {
									log.info("User created!");
									ctx.status(HttpStatus.CREATED);
								})
								.doOnError(error -> {
									log.warn("Error on user persist", error);
									ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
									ctx.json("Server error");
								});
					}
				})::toFuture);
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
