package api.security.training.client_registration.secret.impl;

import java.security.SecureRandom;
import java.util.Base64;

import api.security.training.client_registration.secret.ClientSecret;
import api.security.training.client_registration.secret.ClientSecretSupplier;

public class ClientSecretSupplierImpl implements ClientSecretSupplier {
	private static final int CLIENT_SECRET_BYTES = 50;

	@Override
	public ClientSecret createClientSecret() {
		byte[] secretArray = new byte[CLIENT_SECRET_BYTES];
		new SecureRandom().nextBytes(secretArray);
		var clientSecretAsBase64 = Base64.getEncoder().encodeToString(secretArray);
		// Encrypt later
		return new ClientSecret(clientSecretAsBase64, clientSecretAsBase64);
	}
}
