package api.security.training.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record TokenRequest(
		@JsonProperty("grant_type")
		String grantType,
		String username,
		String password,
		String scope,
		String code,
		@JsonProperty("refresh_token")
		String refreshToken
) {
}
