package api.security.training.authorization.handler;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.apache.hc.core5.net.URIBuilder;

import api.security.training.authorization.AuthorizationRedirectHandler;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.authorization.domain.AuthorizationScope;
import api.security.training.token.TokenCreator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class ImplicitAuthorizationRedirectHandler implements AuthorizationRedirectHandler {
	public static final String TOKEN_RESPONSE_TYPE = "token";

	private final TokenCreator tokenCreator;

	@SneakyThrows
	@Override
	public Mono<String> handleAuthorizationRedirect(AuthorizationRequest authorizationRequest) {
		// TODO: Store in DB so that revoke can be done!
		log.info("Handling implicit redirect {}", authorizationRequest);
		var parsedAuthScopes = Optional.ofNullable(authorizationRequest.scope())
				.stream()
				.flatMap(v -> Arrays.stream(v.split("\\s+")))
				.map(String::trim)
				.filter(v -> !v.isEmpty())
				.map(AuthorizationScope::parse)
				.filter(Objects::nonNull)
				.toList();
		log.info("Parsed scopes = {}", parsedAuthScopes);
		var scopesToUse = parsedAuthScopes.isEmpty() ? Arrays.asList(AuthorizationScope.values()) : parsedAuthScopes;
		var generatedToken = tokenCreator.createToken(authorizationRequest.username(), scopesToUse);
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
		return Mono.just(uriBuilder.build().toString());
	}

	// TODO: Use to validate request
	@Override
	public boolean canHandleResponseType(String responseType) {
		return TOKEN_RESPONSE_TYPE.equals(responseType);
	}
}
