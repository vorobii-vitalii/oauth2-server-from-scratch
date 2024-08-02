package api.security.training.authorization.dto;

import lombok.Builder;

@Builder
public record TokenGenerationError(String reason) {
}
