package cli;

import lombok.Getter;

public class SessionSettings {
    private static final long DEFAULT_TIMEOUT_MILLIS = 300_000;
    private static final long MIN_TIMEOUT_MILLIS = 30_000;
    private static final long MAX_TIMEOUT_MILLIS = 86_400_000;

    @Getter
    private long autoLockTimeoutMillis = DEFAULT_TIMEOUT_MILLIS;

    public void setAutoLockTimeoutMillis(long autoLockTimeoutMillis) {
        if (autoLockTimeoutMillis < MIN_TIMEOUT_MILLIS || autoLockTimeoutMillis > MAX_TIMEOUT_MILLIS)
            throw new IllegalArgumentException("Timeout must be between 30 seconds and 24 hours");

        this.autoLockTimeoutMillis = autoLockTimeoutMillis;
    }

    public String describeAutoLockTimeout() {
        long seconds = autoLockTimeoutMillis / 1000;

        if (seconds % 3600 == 0) return seconds / 3600 + "h";
        if (seconds % 60 == 0) return seconds / 60 + "m";

        return seconds + "s";
    }
}
