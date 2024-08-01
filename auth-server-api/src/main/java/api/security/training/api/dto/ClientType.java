package api.security.training.api.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ClientType {
	CONFIDENTIAL("confidential"),
	PUBLIC("public");

	private final String name;

	ClientType(String name) {
		this.name = name;
	}

	@JsonValue
	public String getValue() {
		return name;
	}
}
