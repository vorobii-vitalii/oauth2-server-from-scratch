package api.security.training.spring;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import api.security.training.authorization.AuthorizationRedirectStrategy;
import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dao.AuthorizationRequestRepository;
import api.security.training.authorization.service.ApproveAuthorizationRequestService;
import api.security.training.authorization.service.ObtainResourceOwnerConsentService;
import api.security.training.authorization.service.RejectAuthorizationRequestService;
import api.security.training.authorization.service.TokenRequestService;
import api.security.training.authorization.service.impl.ApproveAuthorizationRequestServiceImpl;
import api.security.training.authorization.service.impl.ObtainResourceOwnerConsentServiceImpl;
import api.security.training.authorization.service.impl.RejectAuthorizationRequestServiceImpl;
import api.security.training.authorization.service.impl.TokenRequestServiceImpl;
import api.security.training.authorization.utils.impl.URIParametersAppenderImpl;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
import api.security.training.users.dao.UserRepository;
import api.security.training.users.login.service.UserCredentialsChecker;
import api.security.training.users.login.service.impl.UserCredentialsCheckerImpl;
import api.security.training.users.password.PasswordService;
import api.security.training.users.password.impl.NaivePasswordService;
import api.security.training.users.registration.service.UserRegistrationService;
import api.security.training.users.registration.service.impl.UserRegistrationServiceImpl;

@Configuration
public class UserManagementConfiguration {

	@Bean
	PasswordService passwordService() {
		return new NaivePasswordService();
	}

	@Bean
	UserCredentialsChecker userCredentialsChecker(UserRepository userRepository, PasswordService passwordService) {
		return new UserCredentialsCheckerImpl(userRepository, passwordService);
	}

	@Bean
	UserRegistrationService userRegistrationService(PasswordService passwordService, UserRepository userRepository) {
		return new UserRegistrationServiceImpl(passwordService, userRepository, UUID::randomUUID);
	}

	@Bean
	ObtainResourceOwnerConsentService obtainResourceOwnerConsentService(
			AuthorizationRequestRepository authorizationRequestRepository,
			ClientRegistrationRepository clientRegistrationRepository,
			List<AuthorizationRedirectStrategy> authorizationRedirectStrategies
	) {
		return new ObtainResourceOwnerConsentServiceImpl(
				authorizationRequestRepository,
				clientRegistrationRepository,
				UUID::randomUUID,
				authorizationRedirectStrategies,
				new URIParametersAppenderImpl()
		);
	}

	@Bean
	ApproveAuthorizationRequestService approveAuthorizationRequestService(
			AuthorizationRequestRepository authorizationRequestRepository,
			List<AuthorizationRedirectStrategy> authorizationRedirectStrategies
	) {
		return new ApproveAuthorizationRequestServiceImpl(authorizationRequestRepository, authorizationRedirectStrategies);
	}

	@Bean
	RejectAuthorizationRequestService rejectAuthorizationRequestService(AuthorizationRequestRepository authorizationRequestRepository){
		return new RejectAuthorizationRequestServiceImpl(authorizationRequestRepository);
	}

	@Bean
	TokenRequestService tokenRequestService(
			ClientRegistrationRepository clientRegistrationRepository,
			List<TokenRequestHandler> tokenRequestHandlers
	) {
		return new TokenRequestServiceImpl(clientRegistrationRepository, tokenRequestHandlers);
	}

}
