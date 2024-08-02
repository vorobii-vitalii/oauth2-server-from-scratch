package api.security.training.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record TokenResponse(
		@JsonProperty("access_token")
		String accessToken,
		@JsonProperty("refresh_token")
		String refreshToken
) {
}
