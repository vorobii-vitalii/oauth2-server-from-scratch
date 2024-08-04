package api.security.training.handlers;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import api.security.training.authorization.dto.ApproveAuthorizationRequest;
import api.security.training.authorization.service.ApproveAuthorizationRequestService;
import api.security.training.request.RequestParameterService;
import api.security.training.request.dto.RequestParameters;
import api.security.training.utils.ResultProcessor;
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
	public void handle(@NotNull Context ctx) throws Exception {
		var actualUsername = requestParameterService.get(ctx, RequestParameters.USERNAME);
		var authRequestId = UUID.fromString(ctx.pathParam(AUTH_REQUEST_ID));
		var request = ApproveAuthorizationRequest.builder()
				.authorizationRequestId(authRequestId)
				.validator(actualUsername)
				.build();
		ResultProcessor.processResult(approveAuthorizationRequestService.approveAuthorizationRequest(request), v -> {
			ctx.json(Map.of("redirectURL", v));
			ctx.status(HttpStatus.OK);
		});
	}
}
