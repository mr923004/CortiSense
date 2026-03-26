package rpm.ui.app;

import java.time.Duration;

/*
- Represents how long alert popups and audio should remain active.
- Some options map directly to a fixed time, while others have no timeout.
*/
public enum AlertDuration {
    SEC_10,
    SEC_30,
    MIN_1,
    UNTIL_RESOLVED;

    // Convert the selected option into a Duration, or null if it has no fixed timeout
    public Duration toDurationOrNull() {
        switch (this) {
            case SEC_10: return Duration.ofSeconds(10);
            case SEC_30: return Duration.ofSeconds(30);
            case MIN_1:  return Duration.ofMinutes(1);

            // Alerts stay visible until manually resolved
            case UNTIL_RESOLVED:
            default:
                return null;
        }
    }
}
