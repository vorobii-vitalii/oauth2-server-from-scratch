package api.security.training.handlers;

import org.jetbrains.annotations.NotNull;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.client_registration.service.ClientRegistrationService;
import api.security.training.utils.ResultProcessor;
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
	public void handle(@NotNull Context ctx) throws Exception {
		var registerClientRequest = ctx.bodyAsClass(RegisterClientRequest.class);
		ResultProcessor.processResult(clientRegistrationService.registerClient(registerClientRequest), v -> {
			ctx.json(v);
			ctx.status(HttpStatus.OK);
		});
	}
}
