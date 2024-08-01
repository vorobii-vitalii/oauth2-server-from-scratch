package api.security.training.api.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterClientResponse(
		@JsonProperty("client_id") UUID clientId,
		@JsonProperty("client_secret") String clientSecret
) {

}
