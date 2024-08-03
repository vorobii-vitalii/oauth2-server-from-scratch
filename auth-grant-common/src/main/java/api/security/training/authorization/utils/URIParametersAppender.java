package api.security.training.authorization.utils;

import java.util.Map;

public interface URIParametersAppender {
	String appendParameters(String originalURI, Map<String, String> parameters);
}
