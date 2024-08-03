package api.security.training.authorization.utils.impl;

import java.util.Map;

import org.apache.hc.core5.net.URIBuilder;

import api.security.training.authorization.utils.URIParametersAppender;
import lombok.SneakyThrows;

public class URIParametersAppenderImpl implements URIParametersAppender {

	@SneakyThrows
	@Override
	public String appendParameters(String originalURI, Map<String, String> parameters) {
		if (parameters.isEmpty()) {
			return originalURI;
		}
		var uriBuilder = new URIBuilder(originalURI);
		parameters.forEach(uriBuilder::addParameter);
		return uriBuilder.build().toString();
	}

}
