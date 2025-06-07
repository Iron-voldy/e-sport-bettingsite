package lk.esports.betting.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.regex.Pattern;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Utility class for password operations including hashing, verification, and validation
 */
public class PasswordUtil {

    private static final Logger logger = Logger.getLogger(PasswordUtil.class.getName());

    // Password validation patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

    // Constants
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 100;
    private static final int BCRYPT_ROUNDS = 12; // Higher is more secure but slower
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Hash a password using BCrypt
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
        } catch (Exception e) {
            logger.severe("Error hashing password: " + e.getMessage());
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Verify a password against its hash
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            logger.warning("Error verifying password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate password strength
     */
    public static PasswordValidationResult validatePassword(String password) {
        PasswordValidationResult result = new PasswordValidationResult();

        if (password == null || password.isEmpty()) {
            result.addError("Password is required");
            return result;
        }

        // Check length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            result.addError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            result.addError("Password must not exceed " + MAX_PASSWORD_LENGTH + " characters");
        }

        // Check character requirements
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            result.addError("Password must contain at least one uppercase letter");
        }

        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            result.addError("Password must contain at least one lowercase letter");
        }

        if (!DIGIT_PATTERN.matcher(password).matches()) {
            result.addError("Password must contain at least one digit");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            result.addError("Password must contain at least one special character");
        }

        // Check for common weak passwords
        if (isCommonPassword(password)) {
            result.addError("Password is too common and easily guessable");
        }

        // Check for repeated characters
        if (hasRepeatedCharacters(password)) {
            result.addError("Password should not contain too many repeated characters");
        }

        return result;
    }

    /**
     * Calculate password strength score (0-100)
     */
    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length score (max 25 points)
        if (password.length() >= 8) score += 10;
        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 5;

        // Character variety (max 40 points)
        if (UPPERCASE_PATTERN.matcher(password).matches()) score += 10;
        if (LOWERCASE_PATTERN.matcher(password).matches()) score += 10;
        if (DIGIT_PATTERN.matcher(password).matches()) score += 10;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) score += 10;

        // Uniqueness score (max 20 points)
        score += Math.min(getUniqueCharCount(password) * 2, 20);

        // Penalty for common patterns (max -15 points)
        if (isCommonPassword(password)) score -= 15;
        if (hasRepeatedCharacters(password)) score -= 5;
        if (hasSequentialCharacters(password)) score -= 5;

        // Additional bonus for very strong passwords (max 15 points)
        if (password.length() >= 20 && getUniqueCharCount(password) >= 15) score += 15;

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Generate a secure random password
     */
    public static String generateSecurePassword(int length) {
        if (length < MIN_PASSWORD_LENGTH) {
            length = MIN_PASSWORD_LENGTH;
        }

        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        String allChars = uppercase + lowercase + digits + special;

        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each category
        password.append(uppercase.charAt(SECURE_RANDOM.nextInt(uppercase.length())));
        password.append(lowercase.charAt(SECURE_RANDOM.nextInt(lowercase.length())));
        password.append(digits.charAt(SECURE_RANDOM.nextInt(digits.length())));
        password.append(special.charAt(SECURE_RANDOM.nextInt(special.length())));

        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(SECURE_RANDOM.nextInt(allChars.length())));
        }

        // Shuffle the password
        return shuffleString(password.toString());
    }

    /**
     * Generate a temporary password for password reset
     */
    public static String generateTemporaryPassword() {
        return generateSecurePassword(12);
    }

    /**
     * Check if password is commonly used
     */
    private static boolean isCommonPassword(String password) {
        String[] commonPasswords = {
                "password", "123456", "password123", "admin", "qwerty",
                "letmein", "welcome", "monkey", "1234567890", "abc123",
                "Password1", "password1", "123456789", "welcome123"
        };

        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.equals(common.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for repeated characters (more than 3 consecutive)
     */
    private static boolean hasRepeatedCharacters(String password) {
        int count = 1;
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1)) {
                count++;
                if (count > 3) {
                    return true;
                }
            } else {
                count = 1;
            }
        }
        return false;
    }

    /**
     * Check for sequential characters (like 123 or abc)
     */
    private static boolean hasSequentialCharacters(String password) {
        int count = 1;
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1) + 1) {
                count++;
                if (count > 3) {
                    return true;
                }
            } else {
                count = 1;
            }
        }
        return false;
    }

    /**
     * Get count of unique characters in password
     */
    private static int getUniqueCharCount(String password) {
        return (int) password.chars().distinct().count();
    }

    /**
     * Shuffle a string randomly
     */
    private static String shuffleString(String string) {
        char[] array = string.toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        return new String(array);
    }

    /**
     * Generate a secure random salt (for additional security if needed)
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Check if a password needs to be rehashed (if using different hash parameters)
     */
    public static boolean needsRehash(String hashedPassword) {
        // BCrypt hashes start with $2a$, $2b$, $2x$, or $2y$
        // Check if the hash uses our current rounds setting
        if (hashedPassword == null || hashedPassword.length() < 7) {
            return true;
        }

        try {
            String[] parts = hashedPassword.split("\\$");
            if (parts.length >= 3) {
                int rounds = Integer.parseInt(parts[2]);
                return rounds < BCRYPT_ROUNDS;
            }
        } catch (NumberFormatException e) {
            return true;
        }

        return true;
    }

    /**
     * Password validation result class
     */
    public static class PasswordValidationResult {
        private boolean valid = true;
        private StringBuilder errors = new StringBuilder();

        public void addError(String error) {
            valid = false;
            if (errors.length() > 0) {
                errors.append("; ");
            }
            errors.append(error);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrors() {
            return errors.toString();
        }

        public boolean hasErrors() {
            return !valid;
        }
    }
}