package api.security.training.users.password;

public interface PasswordService {
	String hashPassword(String passwordInPlainText);
	boolean isPasswordCorrect(String passwordHash, String enteredPassword);
}
