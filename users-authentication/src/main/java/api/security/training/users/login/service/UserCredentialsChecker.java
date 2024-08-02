package api.security.training.users.login.service;

public interface UserCredentialsChecker {
	boolean areCredentialsCorrect(String username, String password);
}
