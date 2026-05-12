package util;

import java.security.SecureRandom;

public class PasswordGenerator {
    public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    public static final String DIGITS = "0123456789";
    public static final String SYMBOLS = "!@#$%^&*()-_=+";

    private static final SecureRandom RANDOM = new SecureRandom();

    public static char[] generate(int length, boolean useUpper, boolean useDigits, boolean useSymbols) {
        StringBuilder charset = new StringBuilder(LOWER);

        if (useUpper) charset.append(UPPER);
        if (useDigits) charset.append(DIGITS);
        if (useSymbols) charset.append(SYMBOLS);

        char[] password = new char[length];
        for (int i = 0; i < length; i++) password[i] = charset.charAt(RANDOM.nextInt(charset.length()));

        return password;
    }
}
