package rpm.domain.alarm;

// Temporary listener used for testing the alarm system in the console.
// This will be removed once the UI handles alarms properly.

public final class ConsoleAlarmListener implements AlarmListener {

    @Override
    public void onAlarmTransition(AlarmTransition t) {
        System.out.printf(
                "[ALARM] %s %s: %s -> %s (%s)%n",
                t.getPatientId().getDisplayName(),
                t.getVitalType(),
                t.getFrom(),
                t.getTo(),
                t.getReason()
        );
    }

    @Override
    public void onAlarmState(rpm.domain.PatientId id, java.time.Instant time, AlarmState state) {
        // Ignored for now since this listener only cares about transitions
    }
}
