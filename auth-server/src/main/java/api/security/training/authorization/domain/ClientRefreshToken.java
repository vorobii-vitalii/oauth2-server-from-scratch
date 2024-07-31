package api.security.training.authorization.domain;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Table("client_refresh_tokens")
@Builder
public record ClientRefreshToken(
		@Id
		@Column("refresh_token")
		UUID refreshToken,
		@Column("client_id")
		UUID clientId,
		@Column("username")
		String username,
		@Column("scope")
		String scope,
		@Version
		@Column("version")
		Integer version,
		@Column("created_at")
		Instant createdAt
) {
}
