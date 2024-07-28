package api.security.training.token;

import java.util.List;

import api.security.training.authorization.domain.AuthorizationScope;
import lombok.Builder;

@Builder
public record TokenInfo(String username, List<AuthorizationScope> authScopes, boolean isExpired) {
}
