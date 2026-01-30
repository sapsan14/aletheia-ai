package ai.aletheia.crypto;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Canonical form for LLM response text before hashing.
 * <p>
 * Rules:
 * <ol>
 *   <li>Normalize Unicode to NFC.</li>
 *   <li>Normalize line endings to {@code \n} ({@code \r\n} and {@code \r} → {@code \n}).</li>
 *   <li>Trim trailing whitespace from each line; collapse multiple consecutive blank lines to one.</li>
 *   <li>Non-empty result ends with exactly one newline (so "line" and "line\n" both become "line\n").</li>
 * </ol>
 * Output is UTF-8 bytes. Same input always produces same bytes; {@code \r\n} vs {@code \n} yields same result.
 */
@Service
public class CanonicalizationServiceImpl implements CanonicalizationService {

    private static final String LINE_SEP = "\n";

    @Override
    public byte[] canonicalize(String input) {
        if (input == null) {
            return new byte[0];
        }
        // (1) Unicode NFC
        String nfc = Normalizer.normalize(input, Normalizer.Form.NFC);
        // (2) Line endings to \n
        String linesOnly = nfc.replace("\r\n", LINE_SEP).replace("\r", LINE_SEP);
        String[] parts = linesOnly.split(LINE_SEP, -1);
        // (3) Trim each line, collapse consecutive blank lines to one
        List<String> out = new ArrayList<>();
        boolean lastWasBlank = false;
        for (String part : parts) {
            String trimmed = part.trim();
            boolean blank = trimmed.isEmpty();
            if (blank) {
                if (!lastWasBlank) {
                    out.add("");
                }
                lastWasBlank = true;
            } else {
                out.add(trimmed);
                lastWasBlank = false;
            }
        }
        // Remove trailing blank line if we added one (so we don't end with \n\n before rule 4)
        if (!out.isEmpty() && out.get(out.size() - 1).isEmpty()) {
            out.remove(out.size() - 1);
        }
        String joined = String.join(LINE_SEP, out);
        // (4) Non-empty → exactly one trailing newline
        if (!joined.isEmpty()) {
            joined = joined + LINE_SEP;
        }
        return joined.getBytes(StandardCharsets.UTF_8);
    }
}
