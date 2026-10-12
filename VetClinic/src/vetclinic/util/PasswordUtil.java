package vetclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil - turns a password into a SHA-256 hash so we never store the plain password.
 * Owner: Jermil
 *
 * OOP concepts: STATIC methods, PRIVATE constructor.
 * NOTE: this is school-project level security (no salt). Real systems use a salted slow hash.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    // Returns the SHA-256 hash of the password as hex text, for example "a665a459...".
    public static String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));   // each byte becomes 2 hex characters
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 exists in every Java installation, so this should never happen.
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    // True if hashing the typed password gives the same hash that was stored.
    public static boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        return hash(plainPassword).equals(storedHash);
    }
}
