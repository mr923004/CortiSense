package rpm.ui.alerts;

import rpm.domain.alarm.AlarmLevel;
import rpm.domain.alarm.AlarmState;
import rpm.ui.app.AlertDuration;
import rpm.ui.app.AppContext;

import java.time.Duration;

/*
- Central place for small alert-related rules and timing behaviour.
- Keeps UI logic consistent across the application.
*/
public final class AlertRules {

    // Only treat an alarm as alerting if it is RED
    public static boolean isAlertingRedOnly(AlarmState s) {
        return s != null && s.getOverall() == AlarmLevel.RED;
    }

    /*
    - After resolving an alert, we hide it temporarily using a cooldown.
    - This prevents the same alert from instantly popping back up
      if the condition has not yet cleared.
    */
    public static Duration resolveCooldown(AppContext ctx) {
        AlertDuration d = ctx.settings.getAlertDuration();
        if (d == null) return Duration.ofSeconds(30);

        switch (d) {
            case SEC_10: return Duration.ofSeconds(10);
            case SEC_30: return Duration.ofSeconds(30);
            case MIN_1:  return Duration.ofMinutes(1);

            // If alerts are set to stay visible until resolved,
            // still apply a small cooldown to avoid UI spam
            case UNTIL_RESOLVED:
            default:
                return Duration.ofSeconds(45);
        }
    }

    // Utility class, not meant to be instantiated
    private AlertRules() {}
}
