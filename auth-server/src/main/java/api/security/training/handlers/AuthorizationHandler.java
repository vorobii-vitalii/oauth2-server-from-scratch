package api.security.training.handlers;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import api.security.training.authorization.dto.ResourceOwnerAuthorizationRequest;
import api.security.training.authorization.service.ObtainResourceOwnerConsentService;
import api.security.training.request.RequestParameterService;
import api.security.training.request.RequestParameters;
import api.security.training.utils.ResultProcessor;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AuthorizationHandler implements Handler {
	public static final String RESPONSE_TYPE = "response_type";
	public static final String CLIENT_ID = "client_id";
	public static final String SCOPE = "scope";
	public static final String STATE = "state";
	public static final String REDIRECT_URI = "redirect_uri";

	private final RequestParameterService requestParameterService;
	private final ObtainResourceOwnerConsentService obtainResourceOwnerConsentService;

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		var username = requestParameterService.get(ctx, RequestParameters.USERNAME);
		var resourceOwnerAuthorizationRequest = ResourceOwnerAuthorizationRequest.builder()
				.clientId(ctx.queryParam(CLIENT_ID))
				.redirectURI(Optional.ofNullable(ctx.queryParam(REDIRECT_URI)).map(URI::create).orElse(null))
				.responseType(ctx.queryParam(RESPONSE_TYPE))
				.scope(ctx.queryParam(SCOPE))
				.state(ctx.queryParam(STATE))
				.username(username)
				.build();
		ResultProcessor.processResult(
				obtainResourceOwnerConsentService.obtainResourceOwnerConsent(resourceOwnerAuthorizationRequest),
				result -> {
					if (result.isLeft()) {
						var resourceOwnerConsentRequest = result.getLeft();
						ctx.render("provide-consent-page.jte", Map.of(
								"clientName", resourceOwnerConsentRequest.clientName(),
								"clientDescription", resourceOwnerConsentRequest.clientDescription(),
								"scopeList", resourceOwnerConsentRequest.scopeList(),
								"authorizationRequestId", resourceOwnerConsentRequest.authorizationRequestId()
						));
						ctx.status(HttpStatus.OK);
					} else {
						ctx.redirect(result.getRight().toString());
					}
				});
	}

}
