package api.security.training.handlers;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import api.security.training.api.dto.UserRegistrationRequest;
import api.security.training.users.registration.service.UserRegistrationService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserRegistrationHandler implements Handler {
	private final UserRegistrationService userRegistrationService;

	@Override
	public void handle(@NotNull Context ctx) {
		var registrationRequest = ctx.bodyAsClass(UserRegistrationRequest.class);
		var result = userRegistrationService.performRegistration(registrationRequest);
		if (result.isOk()) {
			log.info("User created! ID = {}", result.getResult());
			ctx.status(HttpStatus.CREATED);
		} else {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of(result.getException().getCause()));
		}
	}

}
