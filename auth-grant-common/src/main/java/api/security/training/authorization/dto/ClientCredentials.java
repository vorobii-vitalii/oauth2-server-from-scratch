package api.security.training.authorization.dto;

import lombok.Builder;

@Builder
public record ClientCredentials(String clientId, String clientSecret) {
}
