package api.security.training.token.impl;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import api.security.training.token.AccessTokenCreator;
import api.security.training.token.dto.AuthorizationScope;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAccessTokenCreator implements AccessTokenCreator {
	private final Key signKey;
	private final Supplier<Date> currentDateProvider;
	private final long tokenExpirationInMs;

	@Override
	public String createToken(String username, List<AuthorizationScope> authScopes) {
		Date currentDate = currentDateProvider.get();
		Date expiryDate = new Date(currentDate.getTime() + tokenExpirationInMs);
		return Jwts.builder()
				.setClaims(Map.of(
						"scopes", authScopes.stream().map(AuthorizationScope::getCode).collect(Collectors.joining(" "))
				))
				.setSubject(username)
				.setIssuedAt(currentDate)
				.setExpiration(expiryDate)
				.signWith(signKey, SignatureAlgorithm.HS256).compact();
	}
}
