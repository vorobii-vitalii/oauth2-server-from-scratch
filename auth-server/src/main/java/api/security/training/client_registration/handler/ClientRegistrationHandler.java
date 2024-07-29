package api.security.training.client_registration.handler;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.api.dto.RegisterClientResponse;
import api.security.training.client_registration.ClientSecret;
import api.security.training.client_registration.ClientSecretSupplier;
import api.security.training.client_registration.UUIDSupplier;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
import api.security.training.client_registration.domain.ClientRegistration;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ClientRegistrationHandler implements Handler {
	private final ClientRegistrationRepository clientRegistrationRepository;
	private final UUIDSupplier UUIDSupplier;
	private final ClientSecretSupplier clientSecretSupplier;

	@Override
	public void handle(@NotNull Context ctx) {
		var registerClientRequest = ctx.bodyAsClass(RegisterClientRequest.class);
		var clientName = registerClientRequest.name();
		var alreadyExists = clientRegistrationRepository.existsByClientName(clientName);
		if (alreadyExists) {
			log.warn("Client by name {} already exists", clientName);
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Client with such name already exists"));
		} else {
			UUID newClientId = UUIDSupplier.createUUID();
			ClientSecret clientSecret = clientSecretSupplier.createClientSecret();
			var clientRegistration = ClientRegistration.builder()
					.clientId(newClientId)
					// Not encrypt for now for simplicity
					.clientSecretEncrypted(clientSecret.clientSecretEncrypted())
					.clientName(clientName)
					.clientDescription(registerClientRequest.description())
					.clientType(registerClientRequest.clientType().getValue())
					.redirectURL(registerClientRequest.redirectUrl().toString())
					.build();
			try {
				clientRegistrationRepository.save(clientRegistration);
				log.info("Successfully inserted new client {}", clientRegistration);
				ctx.status(HttpStatus.OK);
				ctx.json(new RegisterClientResponse(newClientId, clientSecret.clientSecretPlainText()));
			}
			catch (DataAccessException error) {
				log.error("Error occurred on save of registration", error);
				ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
				ctx.json(List.of("Server error"));
			}
		}
	}

}
