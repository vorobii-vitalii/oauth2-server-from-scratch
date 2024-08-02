package api.security.training.authorization.handler;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.hc.core5.net.URIBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.relational.core.query.Query;

import api.security.training.RequestTokenExtractor;
import api.security.training.token.AccessTokenInfoReader;
import api.security.training.authorization.dao.AuthorizationRequestRepository;
import api.security.training.authorization.domain.AuthorizationRequest;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RejectAuthorizationRequestHandler implements Handler {
	public static final String AUTH_REQUEST_ID = "authRequestId";
	private final AuthorizationRequestRepository authorizationRequestRepository;
	private final AccessTokenInfoReader accessTokenInfoReader;
	private final RequestTokenExtractor requestTokenExtractor;

	@SneakyThrows
	@Override
	public void handle(@NotNull Context ctx) {
		var token = requestTokenExtractor.extractTokenFromRequest(ctx).orElseThrow();
		// TODO: Set by filter to reduce latency
		// TODO: get rid of reactive
		var actualUsername = accessTokenInfoReader.readTokenInfo(token).username();
		var authRequestId = UUID.fromString(ctx.pathParam(AUTH_REQUEST_ID));
		log.info("Checking whether authentication request by id = {} exists", authRequestId);
		var authRequestOpt = authorizationRequestRepository.findById(authRequestId);
		if (authRequestOpt.isEmpty()) {
			log.warn("Authentication request is absent in DB!");
			ctx.status(HttpStatus.NOT_FOUND);
			ctx.json(List.of("Authentication request was not found!"));
		} else {
			var authorizationRequest = authRequestOpt.get();
			if (Objects.equals(authorizationRequest.username(), actualUsername)) {
				log.info("Performing rejected redirect...");
				URIBuilder uriBuilder = new URIBuilder(authorizationRequest.redirectURL())
						.addParameter("error", "access_denied");
				if (authorizationRequest.state() != null) {
					uriBuilder.addParameter("state", authorizationRequest.state());
				}
				var redirectUrl = uriBuilder.build().toString();
				log.info("Will perform redirect to {}", redirectUrl);
				ctx.json(Map.of("redirectURL", redirectUrl));
				ctx.status(HttpStatus.OK);
			} else {
				ctx.status(HttpStatus.BAD_REQUEST);
				ctx.json(List.of("You tried to reject request not requested by you!"));
			}
		}
	}

	private @NotNull Query queryAuthRequestById(UUID id) {
		return query(where(AuthorizationRequest.ID).is(id));
	}

}
