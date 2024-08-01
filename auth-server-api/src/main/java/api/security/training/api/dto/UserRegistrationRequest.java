package api.security.training.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Valid
public record UserRegistrationRequest(

		@NotNull
		@NotBlank
		@Size(min = 5, max = 50)
		String username,

		@NotNull
		@NotBlank
		@Size(min = 5, max = 50)
		String firstName,

		@NotNull
		@NotBlank
		@Size(min = 5, max = 50)
		String lastName,

		@Pattern(regexp = "^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$")
		@NotNull
		String phoneNumber,

		@Size(min = 6, max = 120)
		@NotNull
		String password
) {
}
