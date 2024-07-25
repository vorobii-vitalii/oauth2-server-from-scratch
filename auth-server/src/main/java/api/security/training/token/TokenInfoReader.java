package api.security.training.token;

public interface TokenInfoReader {
	TokenInfo readTokenInfo(String token);
}
