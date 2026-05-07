package util;

import java.security.SecureRandom;

public class PasswordGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static char[] generate(int length) {
        char[] password = new char[length];

        for (int i = 0; i < length; i++) password[i] = CHARS.charAt(RANDOM.nextInt(CHARS.length()));
        return password;
    }
}
