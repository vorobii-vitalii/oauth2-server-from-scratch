package api.security.training.authorization.domain;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Table("authorization_requests")
@Builder
public record AuthorizationRequest(
		@Id
		@Column(ID)
		UUID id,

		@Column(USERNAME)
		String username,

		@Column(RESPONSE_TYPE)
		String responseType,

		@Column(CLIENT_ID)
		UUID clientId,

		@Column(SCOPE)
		String scope,

		@Column(STATE)
		String state,

		@Column(REDIRECT_URL)
		URI redirectURL,

		@Version Integer version
) {
	public static final String ID = "id";
	public static final String USERNAME = "username";
	public static final String RESPONSE_TYPE = "response_type";
	public static final String CLIENT_ID = "client_id";
	public static final String SCOPE = "scope";
	public static final String STATE = "state";
	public static final String REDIRECT_URL = "redirect_url";

}
