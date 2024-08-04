package api.security.training.spring.grants;

import java.time.Clock;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dao.ClientAuthenticationCodeRepository;
import api.security.training.authorization.dao.ClientRefreshTokenRepository;
import api.security.training.authorization.handler.AuthorizationCodeTokenRequestHandler;
import api.security.training.authorization.handler.CodeAuthorizationRedirectStrategy;
import api.security.training.authorization.handler.RefreshTokenRequestHandler;
import api.security.training.authorization.utils.impl.URIParametersAppenderImpl;
import api.security.training.token.AccessTokenCreator;

@Configuration
public class AuthenticationCodeGrantConfiguration {

	@Bean
	TokenRequestHandler tokenRequestHandler(
			ClientAuthenticationCodeRepository clientAuthenticationCodeRepository,
			ClientRefreshTokenRepository clientRefreshTokenRepository,
			AccessTokenCreator accessTokenCreator
	) {
		return new AuthorizationCodeTokenRequestHandler(
				clientAuthenticationCodeRepository,
				UUID::randomUUID,
				clientRefreshTokenRepository,
				Clock.systemUTC(),
				accessTokenCreator
		);
	}

	@Bean
	CodeAuthorizationRedirectStrategy codeAuthorizationRedirectStrategy(
			ClientAuthenticationCodeRepository clientAuthenticationCodeRepository
	) {
		return new CodeAuthorizationRedirectStrategy(clientAuthenticationCodeRepository, UUID::randomUUID, new URIParametersAppenderImpl());
	}

	@Bean
	RefreshTokenRequestHandler refreshTokenRequestHandler(
			ClientRefreshTokenRepository clientRefreshTokenRepository,
			AccessTokenCreator accessTokenCreator
	) {
		return new RefreshTokenRequestHandler(clientRefreshTokenRepository, accessTokenCreator);
	}

}
