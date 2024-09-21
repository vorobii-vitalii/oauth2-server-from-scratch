package api.security.training.token.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Key;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import api.security.training.token.dto.AuthorizationScope;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

class TestJwtAccessTokenCreator {
	private static final Date NOW = new Date();
	private static final long TOKEN_EXPIRATION_IN_MS = 5000L;
	private static final String USERNAME = "myUser";
	public static final int DELTA = 1000;

	Key signKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

	JwtAccessTokenCreator accessTokenCreator = new JwtAccessTokenCreator(signKey, () -> NOW, TOKEN_EXPIRATION_IN_MS);

	@Test
	void createToken() {
		var scopes = List.of(AuthorizationScope.READ_FIRST_NAME, AuthorizationScope.CHANGE_FIRST_NAME);
		var createdToken = accessTokenCreator.createToken(USERNAME, scopes);
		var tokenClaims = Jwts.parserBuilder()
				.setSigningKey(signKey)
				.build()
				.parseClaimsJws(createdToken)
				.getBody();
		assertThat(tokenClaims.getSubject()).isEqualTo(USERNAME);
		assertThat(tokenClaims.getIssuedAt()).isCloseTo(NOW, DELTA);
		assertThat(tokenClaims.getExpiration()).isCloseTo(new Date(NOW.getTime() + TOKEN_EXPIRATION_IN_MS), DELTA);
		assertThat(tokenClaims.get("scopes")).isEqualTo("read_first_name change_first_name");
	}
}
