package api.security.training.token.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import api.security.training.token.dto.AuthorizationScope;
import api.security.training.token.dto.TokenInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

class TestJwtAccessTokenInfoReader {
	private static final String USERNAME = "myUser";
	private static final Date NOW = new Date();

	Key signKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

	JwtAccessTokenInfoReader tokenInfoReader = new JwtAccessTokenInfoReader(signKey);

	@Test
	void readExpiredTokenInfo() {
		var expiredToken = Jwts.builder()
				.setClaims(Map.of(
						"scopes", "read_first_name read_last_name"
				))
				.setSubject(USERNAME)
				.setIssuedAt(new Date(NOW.getTime() - 6000))
				.setExpiration(new Date(NOW.getTime() - 3000))
				.signWith(signKey, SignatureAlgorithm.HS256)
				.compact();
		assertThat(tokenInfoReader.readTokenInfo(expiredToken).isExpired()).isTrue();
	}

	@Test
	void readNotExpiredTokenInfo() {
		var expiredToken = Jwts.builder()
				.setClaims(Map.of(
						"scopes", "read_first_name read_last_name"
				))
				.setSubject(USERNAME)
				.setIssuedAt(new Date(NOW.getTime() - 6000))
				.setExpiration(new Date(NOW.getTime() + 3000))
				.signWith(signKey, SignatureAlgorithm.HS256)
				.compact();
		assertThat(tokenInfoReader.readTokenInfo(expiredToken)).isEqualTo(
				TokenInfo.builder()
						.isExpired(false)
						.username(USERNAME)
						.authScopes(List.of(AuthorizationScope.READ_FIRST_NAME, AuthorizationScope.READ_LAST_NAME))
						.build());
	}

}
