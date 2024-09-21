package api.security.training.authorization.dto;

import java.net.URI;

import lombok.Builder;

@Builder
public record ResourceOwnerAuthorizationRequest(
	String responseType,
	String clientId,
	String scope,
	URI redirectURI,
	String state,
	String username
) {
}
