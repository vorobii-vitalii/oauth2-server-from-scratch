package api.security.training.authorization.handler;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Query;

import api.security.training.api.dto.RegisterClientResponse;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.client_registration.UUIDSupplier;
import api.security.training.client_registration.domain.ClientRegistration;
import api.security.training.token.RequestTokenExtractor;
import api.security.training.token.TokenInfoReader;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class AuthorizationHandler implements Handler {
	public static final String RESPONSE_TYPE = "response_type";
	public static final String CLIENT_ID = "client_id";
	public static final String SCOPE = "scope";
	public static final String STATE = "state";

	private final RequestTokenExtractor requestTokenExtractor;
	private final TokenInfoReader tokenInfoReader;
	private final R2dbcEntityOperations entityOperations;
	private final UUIDSupplier uuidSupplier;

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		var token = requestTokenExtractor.extractTokenFromRequest(ctx).orElseThrow();
		// TODO: Set by filter to reduce latency
		var username = tokenInfoReader.readTokenInfo(token).username();

		// code, token etc
		var responseType = ctx.queryParam(RESPONSE_TYPE);
		if (responseType == null) {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Response type not specified"));
			return;
		}
		// TODO: redirect_uri
		// TODO: client_id
		var clientIdParamValue = ctx.queryParam(CLIENT_ID);
		if (clientIdParamValue == null) {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Client id not specified"));
			return;
		}
		var scope = ctx.queryParam(SCOPE);
		var state = ctx.queryParam(STATE);
		var clientId = UUID.fromString(clientIdParamValue);

		var authorizationRequest = AuthorizationRequest.builder()
				.id(uuidSupplier.createUUID())
				.clientId(clientId)
				.scope(scope)
				.state(state)
				.responseType(responseType)
				.username(username)
				.build();
		ctx.future(entityOperations.selectOne(queryClientRegistrationByID(clientId), ClientRegistration.class)
				.map(Optional::ofNullable)
				.switchIfEmpty(Mono.just(Optional.empty()))
				.flatMap(clientRegistrationOpt -> {
					if (clientRegistrationOpt.isEmpty()) {
						log.warn("Client with id = {} not found...", clientId);
						ctx.status(HttpStatus.BAD_REQUEST);
						ctx.json(List.of("Client id not valid"));
						return Mono.empty();
					}
					var clientRegistration = clientRegistrationOpt.get();
					return entityOperations.insert(authorizationRequest)
							.doOnNext(v -> {
								log.info("Successfully inserted new authorization request {}", v);
								ctx.status(HttpStatus.OK);
								ctx.render("provide-consent-page.jte", Map.of(
										"clientName", clientRegistration.clientName(),
										"clientDescription", clientRegistration.clientDescription()
								));
							})
							.doOnError(error -> {
								log.error("Error occurred on creation of registration request", error);
								ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
								ctx.json(List.of("Server error"));
							});
				})::toFuture);
	}

	private @NotNull Query queryClientRegistrationByID(UUID clientId) {
		return query(where(ClientRegistration.CLIENT_ID).is(clientId));
	}

}
