package api.security.training.spring;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import api.security.training.client_registration.dao.ClientRegistrationRepository;
import api.security.training.client_registration.secret.ClientSecretSupplier;
import api.security.training.client_registration.secret.impl.ClientSecretSupplierImpl;
import api.security.training.client_registration.service.ClientRegistrationService;
import api.security.training.client_registration.service.impl.ClientRegistrationServiceImpl;
import api.security.training.token.AccessTokenCreator;
import api.security.training.token.AccessTokenInfoReader;
import api.security.training.token.impl.JwtAccessTokenCreator;
import api.security.training.token.impl.JwtAccessTokenInfoReader;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Configuration
@Import(DBConfiguration.class)
@ComponentScan("api.security.training")
public class RootConfig {
	public static final String SECRET = "357638792F423F4428472B4B6250655368566D597133743677397A2443264629";
	public static final int TOKEN_EXPIRATION_IN_MS = 300_000;

	private static Key createSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	@Bean
	AccessTokenCreator accessTokenCreator() {
		return new JwtAccessTokenCreator(createSignKey(), Date::new, TOKEN_EXPIRATION_IN_MS);
	}

	@Bean
	AccessTokenInfoReader accessTokenInfoReader() {
		return new JwtAccessTokenInfoReader(createSignKey());
	}

	@Bean
	ClientSecretSupplier clientSecretSupplier() {
		return new ClientSecretSupplierImpl();
	}

	@Bean
	ClientRegistrationService clientRegistrationService(
			ClientRegistrationRepository clientRegistrationRepository,
			ClientSecretSupplier clientSecretSupplier
	) {
		return new ClientRegistrationServiceImpl(clientRegistrationRepository, UUID::randomUUID, clientSecretSupplier);
	}

}
