package rpm.domain.alarm;

public enum AlarmLevel {

    // Alarm severity levels from lowest to highest
    GREEN,
    AMBER,
    RED;

    // Returns the more severe of the two alarm levels
    public static AlarmLevel max(AlarmLevel a, AlarmLevel b) {
        return (a.ordinal() >= b.ordinal()) ? a : b;
    }
}
