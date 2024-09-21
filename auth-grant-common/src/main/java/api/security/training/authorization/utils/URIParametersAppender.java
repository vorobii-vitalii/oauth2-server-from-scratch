package api.security.training.authorization.utils;

import java.net.URI;
import java.util.Map;

public interface URIParametersAppender {
	URI appendParameters(URI originalURI, Map<String, String> parameters);
}
