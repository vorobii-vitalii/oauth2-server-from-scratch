package api.security.training.converter;

import java.net.URI;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class URIToStringConverter implements Converter<URI, String> {
	@Override
	public String convert(URI source) {
		return String.valueOf(source);
	}
}
