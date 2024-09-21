package api.security.training.token;

import api.security.training.token.dto.TokenInfo;

public interface AccessTokenInfoReader {
	TokenInfo readTokenInfo(String token);
}
