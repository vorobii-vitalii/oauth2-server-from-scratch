package api.security.training.handlers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import api.security.training.authorization.dto.ApproveAuthorizationRequest;
import api.security.training.authorization.service.ApproveAuthorizationRequestService;
import api.security.training.request.RequestParameterService;
import api.security.training.request.RequestParameters;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ApproveAuthorizationRequestHandler implements Handler {
	private static final String AUTH_REQUEST_ID = "authRequestId";

	private final RequestParameterService requestParameterService;
	private final ApproveAuthorizationRequestService approveAuthorizationRequestService;

	@Override
	public void handle(@NotNull Context ctx) {
		var actualUsername = requestParameterService.get(ctx, RequestParameters.USERNAME);
		var authRequestId = UUID.fromString(ctx.pathParam(AUTH_REQUEST_ID));

		var approveResult = approveAuthorizationRequestService.approveAuthorizationRequest(ApproveAuthorizationRequest.builder()
				.authorizationRequestId(authRequestId)
				.validator(actualUsername)
				.build());
		if (approveResult.isOk()) {
			ctx.json(Map.of("redirectURL", approveResult.getResult()));
			ctx.status(HttpStatus.OK);
		} else {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Server error"));
		}
	}
}
