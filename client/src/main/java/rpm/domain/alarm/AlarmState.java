package rpm.domain.alarm;

import rpm.domain.VitalType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class AlarmState {

    // Highest alarm level across all vitals for this patient
    private final AlarmLevel overall;

    // Alarm status for each vital (only contains non-green vitals)
    private final Map<VitalType, VitalAlarmStatus> byVital;

    public AlarmState(AlarmLevel overall, Map<VitalType, VitalAlarmStatus> byVital) {
        this.overall = overall;

        // Make a copy of the map and make it read-only so it can't be changed from outside

        this.byVital = Collections.unmodifiableMap(new EnumMap<>(byVital));
    }

    public AlarmLevel getOverall() {
        return overall;
    }

    public Map<VitalType, VitalAlarmStatus> getByVital() {
        return byVital;
    }
}
