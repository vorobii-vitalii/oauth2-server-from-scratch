package api.security.training.client_registration.domain;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
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
		URI redirectURL,

		@Column(CLIENT_NAME)
		String clientName,

		@Column(CLIENT_DESCRIPTION)
		String clientDescription,

		@Version Integer version
) {

	public static final String REDIRECT_URL = "redirect_url";
	public static final String CLIENT_NAME = "client_name";
	public static final String CLIENT_DESCRIPTION = "client_description";
	public static final String CLIENT_TYPE = "client_type";
	public static final String CLIENT_SECRET_ENC = "client_secret_enc";
	public static final String CLIENT_ID = "client_id";
}
