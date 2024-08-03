package api.security.training.token.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.spencerwi.either.Result;

import api.security.training.token.dto.AuthorizationScope;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ScopesParser {

	/**
	 * Parses scopes
	 * @param scopes - scopes in format mentioned in OAuth2 specification
	 * @return List of authorization scopes
	 */
	public Result<Optional<List<AuthorizationScope>>> parseAuthorizationScopes(String scopes) {
		if (scopes == null) {
			return Result.ok(Optional.empty());
		}
		List<AuthorizationScope> parsedAuthorizationScopes = new ArrayList<>();
		for (String scope : scopes.split("\\s+")) {
			if (scope.isBlank()) {
				continue;
			}
			var parsedAuthScope = AuthorizationScope.parse(scope.trim());
			if (parsedAuthScope.isEmpty()) {
				return Result.err(new IllegalArgumentException("Invalid scope"));
			}
			parsedAuthorizationScopes.add(parsedAuthScope.get());
		}
		return Result.ok(Optional.of(parsedAuthorizationScopes));
	}

}
