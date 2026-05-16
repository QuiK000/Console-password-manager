package cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SessionSettings")
public class SessionSettingsTest {
    private SessionSettings settings;

    @BeforeEach
    void setUp() {
        settings = new SessionSettings();
    }

    @Test
    @DisplayName("The default timeout is 5 minutes (300,000 ms)")
    void defaultTimeoutIsFixeMinutes() {
        assertThat(settings.getAutoLockTimeoutMillis()).isEqualTo(300_000L);
    }

    @Test
    @DisplayName("Setting a valid timeout is persisted")
    void setValidTimeout() {
        settings.setAutoLockTimeoutMillis(60_000);
        assertThat(settings.getAutoLockTimeoutMillis()).isEqualTo(60_000L);
    }

    @Test
    @DisplayName("Throws an exception if the timeout is less than 30 seconds")
    void throwsOnTooShortTimeout() {
        assertThatThrownBy(() -> settings.setAutoLockTimeoutMillis(1000)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Throws an exception if the timeout is greater than 24 hours")
    void throwsOnTooLongTimeout() {
        assertThatThrownBy(() -> settings.setAutoLockTimeoutMillis(86_400_001L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("The boundary value of 30 seconds is accepted")
    void minBoundaryAccepted() {
        assertThatCode(() -> settings.setAutoLockTimeoutMillis(30_000)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The boundary value of 24 hours is accepted")
    void maxBoundaryAccepted() {
        assertThatCode(() -> settings.setAutoLockTimeoutMillis(86_400_000)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "3600000, 1h",
            "7200000, 2h",
            "300000,  5m",
            "60000,   1m",
            "30000,   30s",
            "45000,   45s",
    })
    @DisplayName("describeAutoLockTimeout formats the timeout into a human-readable form")
    void describeFormatsCorrectly(long millis, String expected) {
        settings.setAutoLockTimeoutMillis(millis);
        assertThat(settings.describeAutoLockTimeout()).isEqualTo(expected.trim());
    }
}
