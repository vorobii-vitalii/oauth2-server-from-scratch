package api.security.training.spring.grants;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import api.security.training.authorization.handler.ImplicitAuthorizationRedirectStrategy;
import api.security.training.authorization.utils.impl.URIParametersAppenderImpl;
import api.security.training.token.AccessTokenCreator;

@Configuration
public class ImplicitGrantConfiguration {

	@Bean
	ImplicitAuthorizationRedirectStrategy implicitAuthorizationRedirectStrategy(AccessTokenCreator accessTokenCreator) {
		return new ImplicitAuthorizationRedirectStrategy(accessTokenCreator, new URIParametersAppenderImpl());
	}

}
