package api.security.training.token;

import java.util.List;

import api.security.training.token.dto.AuthorizationScope;

public interface AccessTokenCreator {
	String createToken(String username, List<AuthorizationScope> authScopes);
}
