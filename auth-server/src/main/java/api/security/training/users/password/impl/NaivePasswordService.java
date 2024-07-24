package api.security.training.users.password.impl;

import java.util.Objects;

import api.security.training.users.password.PasswordService;

public class NaivePasswordService implements PasswordService {
	@Override
	public String hashPassword(String passwordInPlainText) {
		return passwordInPlainText;
	}

	@Override
	public boolean isPasswordCorrect(String passwordHash, String enteredPassword) {
		return Objects.equals(passwordHash, enteredPassword);
	}
}
