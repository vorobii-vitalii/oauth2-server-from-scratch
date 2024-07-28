package api.security.training.token;

import java.util.List;

import api.security.training.authorization.domain.AuthorizationScope;

public interface TokenCreator {
	String createToken(String username, List<AuthorizationScope> authScopes);
}
