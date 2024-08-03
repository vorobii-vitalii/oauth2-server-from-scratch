package api.security.training.authorization.handler;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import api.security.training.authorization.AuthorizationRedirectStrategy;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.authorization.utils.URIParametersAppender;
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
	private final URIParametersAppender uriParametersAppender;

	@SneakyThrows
	@Override
	public URI computeAuthorizationRedirectURL(AuthorizationRequest authorizationRequest) {
		log.info("Handling implicit redirect {}", authorizationRequest);
		List<AuthorizationScope> scopesToUse = ScopesParser.parseAuthorizationScopes(authorizationRequest.scope())
				.orElseGet(() -> Arrays.asList(AuthorizationScope.values()));
		var generatedToken = accessTokenCreator.createToken(authorizationRequest.username(), scopesToUse);
		var parameters = new HashMap<String, String>();
		parameters.put("client_id", authorizationRequest.clientId().toString());
		parameters.put("access_token", generatedToken);
		parameters.put("token_type", "bearer");
		if (authorizationRequest.state() != null) {
			parameters.put("state", authorizationRequest.state());
		}
		if (authorizationRequest.scope() != null) {
			parameters.put("scope", authorizationRequest.scope());
		}
		return uriParametersAppender.appendParameters(authorizationRequest.redirectURL(), parameters);
	}

	@Override
	public boolean canHandleResponseType(String responseType) {
		return TOKEN_RESPONSE_TYPE.equals(responseType);
	}
}
