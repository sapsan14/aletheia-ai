package ai.aletheia.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link CanonicalizationServiceImpl}.
 * Verifies: same input → same bytes; \r\n vs \n → same result; null/empty handled.
 */
class CanonicalizationServiceTest {

    private CanonicalizationService service;

    @BeforeEach
    void setUp() {
        service = new CanonicalizationServiceImpl();
    }

    @Test
    void sameInputAlwaysProducesSameByteArray() {
        String input = "Hello\nWorld\n";
        byte[] first = service.canonicalize(input);
        byte[] second = service.canonicalize(input);
        assertThat(first).isEqualTo(second);
        assertThat(Arrays.hashCode(first)).isEqualTo(Arrays.hashCode(second));
    }

    @Test
    void crLfAndLfProduceSameResult() {
        String withCrLf = "line1\r\nline2\r\n";
        String withLf = "line1\nline2\n";
        byte[] fromCrLf = service.canonicalize(withCrLf);
        byte[] fromLf = service.canonicalize(withLf);
        assertThat(fromCrLf).isEqualTo(fromLf);
    }

    @Test
    void crOnlyNormalizedToLf() {
        String withCr = "a\rb\rc";
        byte[] result = service.canonicalize(withCr);
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("a\nb\nc\n");
    }

    @Test
    void nullProducesEmptyByteArray() {
        byte[] result = service.canonicalize(null);
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void emptyStringProducesEmptyByteArray() {
        byte[] result = service.canonicalize("");
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void trimTrailingWhitespacePerLine() {
        String input = "  hello  \n  world  ";
        byte[] result = service.canonicalize(input);
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("hello\nworld\n");
    }

    @Test
    void collapseMultipleBlankLinesToOne() {
        String input = "a\n\n\n\nb";
        byte[] result = service.canonicalize(input);
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("a\n\nb\n");
    }

    @Test
    void nonEmptyEndsWithSingleNewline() {
        String input = "hello";
        byte[] result = service.canonicalize(input);
        assertThat(result[result.length - 1]).isEqualTo((byte) '\n');
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("hello\n");
    }

    @Test
    void inputAlreadyEndingWithNewlineStillEndsWithOneNewline() {
        String input = "hello\n";
        byte[] result = service.canonicalize(input);
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("hello\n");
    }

    @Test
    void inputExceedingMaxLength_throwsIllegalArgumentException() {
        int max = CanonicalizationServiceImpl.MAX_INPUT_LENGTH;
        String tooLong = "x".repeat(max + 1);
        assertThatThrownBy(() -> service.canonicalize(tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds maximum length")
                .hasMessageContaining(String.valueOf(max + 1))
                .hasMessageContaining(String.valueOf(max));
    }

    @Test
    void inputAtMaxLength_succeeds() {
        String atLimit = "x".repeat(CanonicalizationServiceImpl.MAX_INPUT_LENGTH);
        byte[] result = service.canonicalize(atLimit);
        assertThat(result).isNotEmpty();
    }
}
