package api.security.training.token.impl;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import api.security.training.authorization.domain.AuthorizationScope;
import api.security.training.token.TokenCreator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtTokenCreator implements TokenCreator {
	private final Key signKey;
	private final Supplier<Date> currentDateProvider;
	private final long tokenExpirationInMs;

	@Override
	public String createToken(String username, List<AuthorizationScope> authScopes) {
		Date currentDate = currentDateProvider.get();
		Date expiryDate = new Date(currentDate.getTime() + tokenExpirationInMs);
		return Jwts.builder()
				.setClaims(Map.of(
						"scopes", authScopes.stream().map(AuthorizationScope::getCode).toList()
				))
				.setSubject(username)
				.setIssuedAt(currentDate)
				.setExpiration(expiryDate)
				.signWith(signKey, SignatureAlgorithm.HS256).compact();
	}
}
