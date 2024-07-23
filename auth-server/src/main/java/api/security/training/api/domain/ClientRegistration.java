package api.security.training.api.domain;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Table("client_registrations")
@Builder
public record ClientRegistration(

		@Id
		@Column(CLIENT_ID)
		UUID clientId,

		@Column(CLIENT_SECRET_ENC)
		String clientSecretEncrypted,

		@Column(CLIENT_TYPE)
		String clientType,

		@Column(REDIRECT_URL)
		String redirectURL,

		@Column(CLIENT_NAME)
		String clientName,

		@Column(CLIENT_DESCRIPTION)
		String clientDescription
) {

	public static final String REDIRECT_URL = "redirect_url";
	public static final String CLIENT_NAME = "client_name";
	public static final String CLIENT_DESCRIPTION = "client_description";
	public static final String CLIENT_TYPE = "client_type";
	public static final String CLIENT_SECRET_ENC = "client_secret_enc";
	public static final String CLIENT_ID = "client_id";
}
