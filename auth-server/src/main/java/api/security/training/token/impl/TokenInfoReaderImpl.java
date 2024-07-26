package api.security.training.token.impl;

import java.security.Key;
import java.util.Date;
import java.util.function.Supplier;

import api.security.training.token.TokenInfo;
import api.security.training.token.TokenInfoReader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenInfoReaderImpl implements TokenInfoReader {
	private final Key signKey;
	private final Supplier<Date> currentDateProvider;

	@Override
	public TokenInfo readTokenInfo(String token) {
		try {
			Claims claims = extractAllClaims(token);
			return TokenInfo.builder()
					.isExpired(claims.getExpiration().before(currentDateProvider.get()))
					.username(claims.getSubject())
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
