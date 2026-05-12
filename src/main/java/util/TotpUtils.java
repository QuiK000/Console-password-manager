package util;

import com.warrenstrange.googleauth.GoogleAuthenticator;

public class TotpUtils {
    private static final GoogleAuthenticator AUTH = new GoogleAuthenticator();

    public static String generateCode(String secret) {
        if (secret == null || secret.isBlank()) return null;
        try {
            int code = AUTH.getTotpPassword(secret);
            return String.format("%06d", code);
        } catch (Exception e) {
            return "Invalid secret";
        }
    }
}
