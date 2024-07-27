package api.security.training.authorization.domain;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Table("authorization_requests")
@Builder
public record AuthorizationRequest(
		@Id
		@Column("id")
		UUID id,

		@Column("username")
		String username,

		@Column("response_type")
		String responseType,

		@Column("client_id")
		UUID clientId,

		@Column("scope")
		String scope,

		@Column("state")
		String state
) {
}
