package api.security.training.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record AuthorizationCodeTokenRequest(
	String code
) {
}
