package util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordGenerator")
public class PasswordGeneratorTest {
    @Test
    @DisplayName("Generates a password of the specified length")
    void generatesPasswordOfRequestedLength() {
        char[] password = PasswordGenerator.generate(16, true, true, true);
        assertThat(password).hasSize(16);
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12, 16, 24, 32, 64})
    @DisplayName("The generated password length matches the requested length")
    void lengthMatchesRequest(int length) {
        assertThat(PasswordGenerator.generate(length, true, true, true)).hasSize(length);
    }

    @Test
    @DisplayName("Without symbols, the password does not contain special characters")
    void noSymbolsWhenDisabled() {
        String symbols = PasswordGenerator.SYMBOLS;
        for (int i = 0; i < 50; i++) {
            char[] pass = PasswordGenerator.generate(20, true, true, false);
            for (char c : pass) {
                assertThat(symbols).doesNotContain(String.valueOf(c));
            }
        }
    }

    @Test
    @DisplayName("Without digits, the password does not contain digits")
    void noDigitsWhenDisabled() {
        String digits = PasswordGenerator.DIGITS;
        for (int i = 0; i < 50; i++) {
            char[] pass = PasswordGenerator.generate(20, true, false, false);
            for (char c : pass) {
                assertThat(digits).doesNotContain(String.valueOf(c));
            }
        }
    }

    @Test
    @DisplayName("Without uppercase letters, the password contains only lowercase letters, digits, and symbols")
    void noUppercaseWhenDisabled() {
        String upper = PasswordGenerator.UPPER;
        for (int i = 0; i < 50; i++) {
            char[] pass = PasswordGenerator.generate(20, false, true, true);
            for (char c : pass) {
                assertThat(upper).doesNotContain(String.valueOf(c));
            }
        }
    }

    @RepeatedTest(5)
    @DisplayName("Different calls generate different passwords")
    void generatesUniquePasswords() {
        Set<String> passwords = new HashSet<>();
        for (int i = 0; i < 10; i++) passwords.add(new String(
                PasswordGenerator.generate(20, true, true, true)
        ));

        assertThat(passwords).hasSize(10);
    }
}
