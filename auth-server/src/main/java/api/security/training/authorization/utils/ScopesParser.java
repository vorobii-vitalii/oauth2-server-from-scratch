package api.security.training.authorization.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import api.security.training.authorization.domain.AuthorizationScope;
import api.security.training.exception.InvalidScopeException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ScopesParser {

	/**
	 * Parses scopes
	 * @param scopes - scopes in format mentioned in OAuth2 specification
	 * @return List of authorization scopes
	 */
	public Optional<List<AuthorizationScope>> parseAuthorizationScopes(String scopes) throws InvalidScopeException {
		if (scopes == null) {
			return Optional.empty();
		}
		List<AuthorizationScope> parsedAuthorizationScopes = new ArrayList<>();
		for (String scope : scopes.split("\\s+")) {
			if (scope.isBlank()) {
				continue;
			}
			parsedAuthorizationScopes.add(AuthorizationScope.parse(scope.trim()));
		}
		return Optional.of(parsedAuthorizationScopes);
	}

}
