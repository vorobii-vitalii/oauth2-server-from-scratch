package api.security.training.handlers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import api.security.training.authorization.dto.RejectAuthorizationRequest;
import api.security.training.authorization.service.RejectAuthorizationRequestService;
import api.security.training.request.RequestParameterService;
import api.security.training.request.RequestParameters;
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
	private final RejectAuthorizationRequestService rejectAuthorizationRequestService;
	private final RequestParameterService requestParameterService;

	@SneakyThrows
	@Override
	public void handle(@NotNull Context ctx) {
		var actualUsername = requestParameterService.get(ctx, RequestParameters.USERNAME);
		var authRequestId = UUID.fromString(ctx.pathParam(AUTH_REQUEST_ID));
		var rejectResult = rejectAuthorizationRequestService.rejectAuthorizationRequest(RejectAuthorizationRequest.builder()
				.authorizationRequestId(authRequestId)
				.validator(actualUsername)
				.build());
		if (rejectResult.isOk()) {
			var redirectUrl = rejectResult.getResult();
			log.info("Will perform redirect to {}", redirectUrl);
			// Check if redirect can be performed immediately...
			ctx.json(Map.of("redirectURL", redirectUrl));
			ctx.status(HttpStatus.OK);
		} else {
			ctx.json(List.of("Server error"));
			ctx.status(HttpStatus.BAD_REQUEST);
		}
	}

}
