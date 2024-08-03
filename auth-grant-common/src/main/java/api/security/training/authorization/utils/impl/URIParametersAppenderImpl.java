package api.security.training.authorization.utils.impl;

import java.net.URI;
import java.util.Map;

import org.apache.hc.core5.net.URIBuilder;

import api.security.training.authorization.utils.URIParametersAppender;
import lombok.SneakyThrows;

public class URIParametersAppenderImpl implements URIParametersAppender {

	@SneakyThrows
	@Override
	public URI appendParameters(URI originalURI, Map<String, String> parameters) {
		var uriBuilder = new URIBuilder(originalURI);
		parameters.forEach(uriBuilder::addParameter);
		return uriBuilder.build();
	}

}
