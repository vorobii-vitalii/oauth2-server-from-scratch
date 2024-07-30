package api.security.training.token.impl;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import api.security.training.authorization.domain.AuthorizationScope;
import api.security.training.authorization.utils.ScopesParser;
import api.security.training.token.TokenInfo;
import api.security.training.token.TokenInfoReader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class TokenInfoReaderImpl implements TokenInfoReader {
	private final Key signKey;
	private final Supplier<Date> currentDateProvider;

	@SneakyThrows
	@Override
	public TokenInfo readTokenInfo(String token) {
		try {
			var claims = extractAllClaims(token);
			final Object scopes = claims.get("scopes");
			List<AuthorizationScope> authScopes = scopes == null
					? List.of()
					: ScopesParser.parseAuthorizationScopes(scopes.toString()).orElse(List.of());
			return TokenInfo.builder()
					.isExpired(claims.getExpiration().before(currentDateProvider.get()))
					.username(claims.getSubject())
					.authScopes(authScopes)
					.build();
		}
		catch (ExpiredJwtException error) {
			return TokenInfo.builder().isExpired(true).build();
		}
	}

	private Claims extractAllClaims(String token) {
		return Jwts
				.parserBuilder()
				.setSigningKey(signKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

}
