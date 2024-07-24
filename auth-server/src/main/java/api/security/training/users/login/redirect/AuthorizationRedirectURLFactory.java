package api.security.training.users.login.redirect;

import java.util.List;

import api.security.training.client_registration.domain.ClientRegistration;
import api.security.training.users.domain.User;

public interface AuthorizationRedirectURLFactory {
	String grantType();
	String constructRedirectURL(User user, ClientRegistration clientRegistration, List<String> scopes);
}
