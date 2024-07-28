package api.security.training.authorization.handler;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.hc.core5.net.URIBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Query;

import api.security.training.authorization.domain.AuthorizationRequest;
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
public class RejectAuthorizationRequestHandler implements Handler {
	public static final String AUTH_REQUEST_ID = "authRequestId";
	private final R2dbcEntityOperations entityOperations;
	private final TokenInfoReader tokenInfoReader;
	private final RequestTokenExtractor requestTokenExtractor;

	@Override
	public void handle(@NotNull Context ctx) {
		var token = requestTokenExtractor.extractTokenFromRequest(ctx).orElseThrow();
		// TODO: Set by filter to reduce latency
		// TODO: get rid of reactive
		var actualUsername = tokenInfoReader.readTokenInfo(token).username();
		var authRequestId = UUID.fromString(ctx.pathParam(AUTH_REQUEST_ID));
		log.info("Checking whether authentication request by id = {} exists", authRequestId);
		ctx.future(entityOperations.selectOne(queryAuthRequestById(authRequestId), AuthorizationRequest.class)
				.map(Optional::ofNullable)
				.switchIfEmpty(Mono.just(Optional.empty()))
				.flatMap(authRequestOpt -> {
					if (authRequestOpt.isEmpty()) {
						log.warn("Authentication request is absent in DB!");
						ctx.status(HttpStatus.NOT_FOUND);
						ctx.json(List.of("Authentication request was not found!"));
						return Mono.empty();
					} else {
						var authorizationRequest = authRequestOpt.get();
						if (Objects.equals(authorizationRequest.username(), actualUsername)) {
							log.info("Performing rejected redirect...");
							try {
								URIBuilder uriBuilder = new URIBuilder(authorizationRequest.redirectURL())
										.addParameter("error", "access_denied");
								if (authorizationRequest.state() != null) {
									uriBuilder.addParameter("state", authorizationRequest.state());
								}
								var redirectUrl = uriBuilder.build().toString();
								log.info("Will perform redirect to {}", redirectUrl);
								ctx.json(Map.of("redirectURL", redirectUrl));
								ctx.status(HttpStatus.OK);
								return Mono.empty();
							}
							catch (URISyntaxException e) {
								return Mono.error(new RuntimeException(e));
							}
						} else {
							ctx.status(HttpStatus.BAD_REQUEST);
							ctx.json(List.of("You tried to reject request not requested by you!"));
							return Mono.empty();
						}
					}
				})::toFuture);
	}

	private @NotNull Query queryAuthRequestById(UUID id) {
		return query(where(AuthorizationRequest.ID).is(id));
	}

}
