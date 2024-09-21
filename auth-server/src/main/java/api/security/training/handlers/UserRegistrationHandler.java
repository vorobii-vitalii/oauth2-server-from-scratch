package api.security.training.handlers;

import org.jetbrains.annotations.NotNull;

import api.security.training.api.dto.UserRegistrationRequest;
import api.security.training.users.registration.service.UserRegistrationService;
import api.security.training.utils.ResultProcessor;
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
	public void handle(@NotNull Context ctx) throws Exception {
		var registrationRequest = ctx.bodyAsClass(UserRegistrationRequest.class);
		ResultProcessor.processResult(userRegistrationService.performRegistration(registrationRequest), v -> {
			log.info("User created! ID = {}", v);
			ctx.status(HttpStatus.CREATED);
		});
	}

}
