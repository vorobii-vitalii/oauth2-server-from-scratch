package api.security.training.users.login.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
		String password,

		@NotEmpty
		@NotNull
		List<String> scopes,

		@NotNull
		UUID clientId,

		@NotNull
		String grantType
) {

}
