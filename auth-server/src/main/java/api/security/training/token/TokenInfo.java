package api.security.training.token;

import lombok.Builder;

@Builder
public record TokenInfo(String username, boolean isExpired) {
}
