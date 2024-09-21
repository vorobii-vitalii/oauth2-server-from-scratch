package api.security.training.api.dto;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Valid
public record RegisterClientRequest(

		@NotNull
		@JsonProperty("client_type") ClientType clientType,

		@NotNull
		@JsonProperty("redirect_url") URI redirectUrl,

		@NotNull
		@NotBlank
		@JsonProperty("name") String name,

		@NotNull
		@NotBlank
		@JsonProperty("description") String description
) {

}
