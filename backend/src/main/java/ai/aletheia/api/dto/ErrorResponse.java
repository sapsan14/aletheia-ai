package ai.aletheia.api.dto;

/** Generic error response for 4xx/5xx. */
public record ErrorResponse(String error, Object details) {
    public static ErrorResponse notFound(String message, Object id) {
        return new ErrorResponse(message, id);
    }
}
