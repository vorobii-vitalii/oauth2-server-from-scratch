package api.security.training.spring.grants;

import java.time.Clock;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dao.ClientRefreshTokenRepository;
import api.security.training.token.AccessTokenCreator;
import api.security.training.users.login.service.UserCredentialsChecker;
import pi.security.training.ResourceOwnerCredentialsTokenRequestHandler;

@Configuration
public class ResourceOwnerCredentialsGrantConfiguration {

	@Bean
	TokenRequestHandler resourceOwnerCredentialsTokenRequestHandler(
			UserCredentialsChecker userCredentialsChecker,
			AccessTokenCreator accessTokenCreator,
			ClientRefreshTokenRepository clientRefreshTokenRepository
	) {
		return new ResourceOwnerCredentialsTokenRequestHandler(
				userCredentialsChecker,
				accessTokenCreator,
				UUID::randomUUID,
				clientRefreshTokenRepository,
				Clock.systemUTC()
		);

	}

}
