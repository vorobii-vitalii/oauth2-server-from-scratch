package api.security.training.token.impl;

import java.security.Key;
import java.util.Date;
import java.util.function.Supplier;

import api.security.training.token.TokenInfo;
import api.security.training.token.TokenInfoReader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenInfoReaderImpl implements TokenInfoReader {
	private final Key signKey;
	private final Supplier<Date> currentDateProvider;

	@Override
	public TokenInfo readTokenInfo(String token) {
		Claims claims = extractAllClaims(token);
		return TokenInfo.builder()
				.username(claims.getSubject())
				.isExpired(claims.getExpiration().before(currentDateProvider.get()))
				.build();
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
