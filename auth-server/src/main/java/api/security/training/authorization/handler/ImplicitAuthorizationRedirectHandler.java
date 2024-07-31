package api.security.training.authorization.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.hc.core5.net.URIBuilder;

import api.security.training.authorization.AuthorizationRedirectHandler;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.authorization.domain.AuthorizationScope;
import api.security.training.authorization.utils.ScopesParser;
import api.security.training.token.AccessTokenCreator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ImplicitAuthorizationRedirectHandler implements AuthorizationRedirectHandler {
	public static final String TOKEN_RESPONSE_TYPE = "token";

	private final AccessTokenCreator accessTokenCreator;

	@SneakyThrows
	@Override
	public String handleAuthorizationRedirect(AuthorizationRequest authorizationRequest) {
		// TODO: Store in DB so that revoke can be done!
		log.info("Handling implicit redirect {}", authorizationRequest);
		List<AuthorizationScope> scopesToUse = ScopesParser.parseAuthorizationScopes(authorizationRequest.scope())
				.orElseGet(() -> Arrays.asList(AuthorizationScope.values()));
		var generatedToken = accessTokenCreator.createToken(authorizationRequest.username(), scopesToUse);
		var uriBuilder = new URIBuilder(authorizationRequest.redirectURL())
				.addParameter("client_id", authorizationRequest.clientId().toString())
				.addParameter("access_token", generatedToken)
				.addParameter("token_type", "jwt");
		if (authorizationRequest.state() != null) {
			uriBuilder.addParameter("state", authorizationRequest.state());
		}
		if (authorizationRequest.scope() != null) {
			uriBuilder.addParameter("scope", authorizationRequest.scope());
		}
		return uriBuilder.build().toString();
	}

	@Override
	public boolean canHandleResponseType(String responseType) {
		return TOKEN_RESPONSE_TYPE.equals(responseType);
	}
}
