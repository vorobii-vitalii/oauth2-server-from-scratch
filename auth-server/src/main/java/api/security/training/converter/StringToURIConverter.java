package api.security.training.converter;


import java.net.URI;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class StringToURIConverter implements Converter<String, URI> {
	@Override
	public URI convert(String source) {
		return source == null ? null : URI.create(source);
	}
}
