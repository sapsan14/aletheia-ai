package ai.aletheia.api.dto;

/**
 * Standardized error response with a short code and message.
 */
public record ApiErrorResponse(String code, String message) {
    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message);
    }
}
