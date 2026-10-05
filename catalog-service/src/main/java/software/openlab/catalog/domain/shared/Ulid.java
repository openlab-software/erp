package software.openlab.catalog.domain.shared;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Small, self-contained ULID (Universally Unique Lexicographically Sortable
 * Identifier) generator/validator: a 48-bit millisecond timestamp followed by
 * 80 bits of randomness, Crockford Base32-encoded into 26 characters — see
 * https://github.com/ulid/spec. No external dependency required.
 */
public final class Ulid {

    private static final char[] ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final Set<Character> ALPHABET_SET = buildAlphabetSet();
    private static final SecureRandom RANDOM = new SecureRandom();

    public static final int LENGTH = 26;
    private static final int TIME_CHARS = 10;
    private static final int RANDOM_CHARS = 16;
    private static final int RANDOM_BYTES = 10;

    private Ulid() {
    }

    public static String generate() {
        return encodeTime(System.currentTimeMillis()) + encodeRandom(randomBytes());
    }

    public static boolean isValid(String value) {
        if (value == null || value.length() != LENGTH) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!ALPHABET_SET.contains(Character.toUpperCase(value.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    private static byte[] randomBytes() {
        byte[] bytes = new byte[RANDOM_BYTES];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    /** Encodes a 48-bit timestamp (ms) into 10 base32 chars, MSB first. */
    private static String encodeTime(long timestamp) {
        char[] chars = new char[TIME_CHARS];
        long t = timestamp;
        for (int i = TIME_CHARS - 1; i >= 0; i--) {
            chars[i] = ALPHABET[(int) (t & 0x1F)];
            t >>>= 5;
        }
        return new String(chars);
    }

    /** Encodes 80 bits (10 bytes) into 16 base32 chars — an exact 5-bit-group fit, no padding needed. */
    private static String encodeRandom(byte[] bytes) {
        StringBuilder sb = new StringBuilder(RANDOM_CHARS);
        int buffer = 0;
        int bitsInBuffer = 0;

        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsInBuffer += 8;
            while (bitsInBuffer >= 5) {
                bitsInBuffer -= 5;
                sb.append(ALPHABET[(buffer >>> bitsInBuffer) & 0x1F]);
            }
        }
        return sb.toString();
    }

    private static Set<Character> buildAlphabetSet() {
        Set<Character> set = new HashSet<>();
        for (char c : ALPHABET) {
            set.add(c);
        }
        return set;
    }
}
