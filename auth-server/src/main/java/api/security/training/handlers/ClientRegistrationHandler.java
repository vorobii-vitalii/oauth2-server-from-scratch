package api.security.training.handlers;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.client_registration.service.ClientRegistrationService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ClientRegistrationHandler implements Handler {
	private final ClientRegistrationService clientRegistrationService;

	@Override
	public void handle(@NotNull Context ctx) {
		var registerClientRequest = ctx.bodyAsClass(RegisterClientRequest.class);
		var registrationResult = clientRegistrationService.registerClient(registerClientRequest);
		if (registrationResult.isOk()) {
			ctx.status(HttpStatus.OK);
			ctx.json(registrationResult.getResult());
		} else {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of(registrationResult.getException().getMessage()));
		}
	}
}
