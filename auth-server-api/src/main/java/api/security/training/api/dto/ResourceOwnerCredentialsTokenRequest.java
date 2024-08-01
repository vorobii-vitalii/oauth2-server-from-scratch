package api.security.training.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record ResourceOwnerCredentialsTokenRequest(
		@JsonProperty("grant_type")
		String grantType,
		String username,
		String password,
		String scope
) {
}
