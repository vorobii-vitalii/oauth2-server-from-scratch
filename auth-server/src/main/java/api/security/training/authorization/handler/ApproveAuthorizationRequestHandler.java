package api.security.training.authorization.handler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import api.security.training.RequestTokenExtractor;
import api.security.training.token.AccessTokenInfoReader;
import api.security.training.authorization.AuthorizationRedirectStrategy;
import api.security.training.authorization.dao.AuthorizationRequestRepository;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ApproveAuthorizationRequestHandler implements Handler {
	public static final String AUTH_REQUEST_ID = "authRequestId";
	private final AuthorizationRequestRepository authorizationRequestRepository;
	private final AccessTokenInfoReader accessTokenInfoReader;
	private final RequestTokenExtractor requestTokenExtractor;
	private final List<AuthorizationRedirectStrategy> authorizationRedirectStrategies;

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
			//TODO: transactional
			var authorizationRequest = authRequestOpt.get();
			if (Objects.equals(authorizationRequest.username(), actualUsername)) {
				log.info("Performing redirect...");
				var authorizationRedirectHandler = authorizationRedirectStrategies.stream()
						.filter(v -> v.canHandleResponseType(authorizationRequest.responseType()))
						.findFirst()
						.orElseThrow();
				var redirectUrl = authorizationRedirectHandler.computeAuthorizationRedirectURL(authorizationRequest);
				log.info("Will perform redirect to {}", redirectUrl);
				ctx.json(Map.of("redirectURL", redirectUrl));
				ctx.status(HttpStatus.OK);
				// TODO: Delete auth request
			} else {
				ctx.status(HttpStatus.BAD_REQUEST);
				ctx.json(List.of("You tried to approve request not requested by you!"));
			}
		}
	}

}
