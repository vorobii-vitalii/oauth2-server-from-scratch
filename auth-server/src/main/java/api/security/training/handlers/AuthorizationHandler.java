package api.security.training.handlers;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.spencerwi.either.Either;

import api.security.training.RequestTokenExtractor;
import api.security.training.authorization.service.ObtainResourceOwnerConsentService;
import api.security.training.authorization.dto.ResourceOwnerAuthorizationRequest;
import api.security.training.authorization.dto.ResourceOwnerConsentRequest;
import api.security.training.token.AccessTokenInfoReader;
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

	private final RequestTokenExtractor requestTokenExtractor;
	private final AccessTokenInfoReader accessTokenInfoReader;
	private final ObtainResourceOwnerConsentService obtainResourceOwnerConsentService;

	@Override
	public void handle(@NotNull Context ctx) {
		var token = requestTokenExtractor.extractTokenFromRequest(ctx).orElseThrow();
		var username = accessTokenInfoReader.readTokenInfo(token).username();
		var resourceOwnerAuthorizationRequest = ResourceOwnerAuthorizationRequest.builder()
				.clientId(ctx.queryParam(CLIENT_ID))
				.redirectURI(ctx.queryParam(REDIRECT_URI))
				.responseType(ctx.queryParam(RESPONSE_TYPE))
				.scope(ctx.queryParam(SCOPE))
				.state(ctx.queryParam(STATE))
				.username(username)
				.build();
		Either<ResourceOwnerConsentRequest, Either<String, String>> result =
				obtainResourceOwnerConsentService.obtainResourceOwnerConsent(resourceOwnerAuthorizationRequest);
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
			var errorEither = result.getRight();
			if (errorEither.isLeft()) {
				ctx.status(HttpStatus.BAD_REQUEST);
				ctx.json(List.of(errorEither.getLeft()));
			} else {
				ctx.redirect(errorEither.getRight());
			}
		}

	}

}
