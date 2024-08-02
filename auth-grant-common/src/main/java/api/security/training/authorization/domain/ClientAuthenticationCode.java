package api.security.training.authorization.domain;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Table("client_authentication_codes")
@Builder
public record ClientAuthenticationCode(
		@Id
		@Column("authentication_code")
		UUID code,
		@Column("authorization_request")
		UUID authorizationRequestId,
		@Column("client_id")
		UUID clientId,
		@Column("scope")
		String scope,
		@Column("state")
		String state,
		@Version
		Integer version,
		@Column("username")
		String username
) {
}
