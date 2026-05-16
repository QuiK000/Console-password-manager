package util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsoleUtils.mask")
public class ConsoleUtilsMaskTest {
    @ParameterizedTest
    @CsvSource({
            "password, pa******",
            "ab, ab",
            "'', ''",
    })
    @DisplayName("mask hides the password, leaving the first 2 characters visible")
    void maskBehaviour(String input, String expected) {
        assertThat(ConsoleUtils.mask(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("mask returns an empty string for null")
    void maskNull() {
        assertThat(ConsoleUtils.mask(null)).isEmpty();
    }

    @Test
    @DisplayName("mask does not reveal the full password")
    void maskDoesNotRevealFullPassword() {
        String masked = ConsoleUtils.mask("supersecret");
        assertThat(masked).doesNotContain("supersecret");
        assertThat(masked).startsWith("su");
        assertThat(masked).contains("*");
    }
}
