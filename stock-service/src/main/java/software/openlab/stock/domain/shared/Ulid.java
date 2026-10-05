package software.openlab.stock.domain.shared;

import java.security.SecureRandom;

/**
 * Self-contained ULID (Universally Unique Lexicographically Sortable Identifier) generator
 * and validator — no external dependency.
 *
 * <p>Layout: 48-bit millisecond timestamp + 80 bits of randomness, encoded as 26 characters
 * of Crockford's Base32 (10 chars for the timestamp, 16 for the randomness). Lexicographic
 * ordering of the encoded string matches creation order because the timestamp occupies the
 * leading characters.
 *
 * @see <a href="https://github.com/ulid/spec">ULID spec</a>
 */
public final class Ulid {

    private static final char[] ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final int LENGTH = 26;
    private static final int TIMESTAMP_CHARS = 10;
    private static final int RANDOMNESS_BYTES = 10;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final boolean[] VALID_CHAR = buildValidCharTable();

    private Ulid() {
    }

    public static String generate() {
        return generate(System.currentTimeMillis());
    }

    static String generate(long timestampMillis) {
        byte[] randomness = new byte[RANDOMNESS_BYTES];
        RANDOM.nextBytes(randomness);

        char[] chars = new char[LENGTH];
        encodeTimestamp(timestampMillis, chars);
        encodeRandomness(randomness, chars);
        return new String(chars);
    }

    public static boolean isValid(String candidate) {
        if (candidate == null || candidate.length() != LENGTH) {
            return false;
        }
        for (int i = 0; i < LENGTH; i++) {
            char c = candidate.charAt(i);
            if (c > 'Z' || c < '0' || !VALID_CHAR[c]) {
                return false;
            }
        }
        return true;
    }

    private static void encodeTimestamp(long timestampMillis, char[] out) {
        long ts = timestampMillis;
        for (int i = TIMESTAMP_CHARS - 1; i >= 0; i--) {
            out[i] = ALPHABET[(int) (ts & 0x1F)];
            ts >>>= 5;
        }
    }

    private static void encodeRandomness(byte[] randomness, char[] out) {
        int bitBuffer = 0;
        int bitsInBuffer = 0;
        int charIndex = TIMESTAMP_CHARS;

        for (byte b : randomness) {
            bitBuffer = (bitBuffer << 8) | (b & 0xFF);
            bitsInBuffer += 8;
            while (bitsInBuffer >= 5) {
                bitsInBuffer -= 5;
                out[charIndex++] = ALPHABET[(bitBuffer >> bitsInBuffer) & 0x1F];
            }
        }
    }

    private static boolean[] buildValidCharTable() {
        boolean[] table = new boolean[128];
        for (char c : ALPHABET) {
            table[c] = true;
        }
        return table;
    }
}
