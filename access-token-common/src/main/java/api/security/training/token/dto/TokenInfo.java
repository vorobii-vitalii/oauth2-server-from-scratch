package api.security.training.token.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record TokenInfo(String username, List<AuthorizationScope> authScopes, boolean isExpired) {
}
