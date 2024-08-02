package api.security.training.authorization.dto;

import lombok.Builder;

@Builder
public record ResourceOwnerAuthorizationRequest(
	String responseType,
	String clientId,
	String scope,
	String state,
	String username
) {
}
