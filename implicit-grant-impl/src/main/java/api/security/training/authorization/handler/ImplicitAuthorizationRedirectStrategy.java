package api.security.training.authorization.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.hc.core5.net.URIBuilder;

import api.security.training.authorization.AuthorizationRedirectStrategy;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.token.AccessTokenCreator;
import api.security.training.token.dto.AuthorizationScope;
import api.security.training.token.utils.ScopesParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ImplicitAuthorizationRedirectStrategy implements AuthorizationRedirectStrategy {
	public static final String TOKEN_RESPONSE_TYPE = "token";

	private final AccessTokenCreator accessTokenCreator;

	@SneakyThrows
	@Override
	public String computeAuthorizationRedirectURL(AuthorizationRequest authorizationRequest) {
		log.info("Handling implicit redirect {}", authorizationRequest);
		List<AuthorizationScope> scopesToUse = ScopesParser.parseAuthorizationScopes(authorizationRequest.scope())
				.orElseGet(() -> Arrays.asList(AuthorizationScope.values()));
		var generatedToken = accessTokenCreator.createToken(authorizationRequest.username(), scopesToUse);
		var uriBuilder = new URIBuilder(authorizationRequest.redirectURL())
				.addParameter("client_id", authorizationRequest.clientId().toString())
				.addParameter("access_token", generatedToken)
				.addParameter("token_type", "bearer");
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
