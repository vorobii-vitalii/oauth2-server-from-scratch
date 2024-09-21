package api.security.training.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Valid
public record UserLoginRequest(

		@NotBlank
		@NotNull
		String username,

		@NotBlank
		@NotNull
		String password
) {

}
