package api.security.training.authorization.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record ResourceOwnerConsentRequest(
		String clientName,
		String clientDescription,
		List<String> scopeList,
		String authorizationRequestId
) {
}
