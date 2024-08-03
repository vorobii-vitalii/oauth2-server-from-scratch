package api.security.training.authorization.dto;

import java.util.UUID;

import lombok.Builder;

@Builder
public record ApproveAuthorizationRequest(
		UUID authorizationRequestId,
		String validator
) {
}
