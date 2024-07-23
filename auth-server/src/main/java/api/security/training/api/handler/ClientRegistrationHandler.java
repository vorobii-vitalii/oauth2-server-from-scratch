package api.security.training.api.handler;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.api.dto.RegisterClientResponse;
import api.security.training.domain.ClientRegistration;
import api.security.training.registration.ClientSecret;
import api.security.training.registration.ClientSecretSupplier;
import api.security.training.registration.UUIDSupplier;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class ClientRegistrationHandler implements Handler {
	private final R2dbcEntityTemplate entityTemplate;
	private final UUIDSupplier UUIDSupplier;
	private final ClientSecretSupplier clientSecretSupplier;

	@Override
	public void handle(@NotNull Context ctx) {
		var registerClientRequest = ctx.bodyAsClass(RegisterClientRequest.class);
		ctx.future(() -> {
			var clientName = registerClientRequest.name();
			return entityTemplate.exists(queryByName(clientName), ClientRegistration.class)
					.flatMap(alreadyExists -> {
						if (alreadyExists) {
							log.warn("Client by name {} already exists", clientName);
							ctx.status(HttpStatus.BAD_REQUEST);
							ctx.json(List.of("Client with such name already exists"));
							return Mono.empty();
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
							return entityTemplate.insert(clientRegistration)
									.doOnNext(v -> {
										log.info("Successfully inserted new client {}", v);
										ctx.status(HttpStatus.OK);
										ctx.json(new RegisterClientResponse(newClientId, clientSecret.clientSecretPlainText()));
									})
									.doOnError(error -> {
										log.error("Error occurred on save of registration", error);
										ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
										ctx.json(List.of("Server error"));
									});
						}
					})
					.toFuture();
		});
	}

	private @NotNull Query queryByName(String clientName) {
		return query(where(ClientRegistration.CLIENT_NAME).is(clientName));
	}

}
