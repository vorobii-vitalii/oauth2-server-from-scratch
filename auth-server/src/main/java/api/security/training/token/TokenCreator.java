package api.security.training.token;

import java.util.Map;

public interface TokenCreator {
	String createToken(String username, Map<String, Object> claims);
}
