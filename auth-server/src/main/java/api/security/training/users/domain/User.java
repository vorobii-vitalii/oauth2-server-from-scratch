package api.security.training.users.domain;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Builder
@Table("users")
public record User(

		@Id
		@Column(USER_ID)
		UUID userId,

		@Column(USERNAME)
		String username,

		@Column(FIRST_NAME)
		String firstName,

		@Column(LAST_NAME)
		String lastName,

		@Column(PHONE_NUMBER)
		String phoneNumber,

		@Column(PASSWORD)
		String password
) {
	public static final String USER_ID = "user_id";
	public static final String USERNAME = "username";
	public static final String FIRST_NAME = "first_name";
	public static final String LAST_NAME = "last_name";
	public static final String PHONE_NUMBER = "phone_number";
	public static final String PASSWORD = "password";
}
