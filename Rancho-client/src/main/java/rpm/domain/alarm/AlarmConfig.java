package rpm.domain.alarm;

import rpm.domain.VitalType;

import java.util.EnumMap;
import java.util.Map;

public final class AlarmConfig {
    public final Map<VitalType, ThresholdBand> bands = new EnumMap<>(VitalType.class);

    // persistence in seconds
    public int amberPersistSeconds = 5;
    public int redPersistSeconds = 3;

    // hysteresis: require value to be this far back inside safe band before clearing
    public double hysteresis = 0.5;

    public ThresholdBand band(VitalType t) {
        return bands.get(t);
    }

    public static AlarmConfig defaultAdult() {
        AlarmConfig c = new AlarmConfig();

        // HR (bpm)
        c.bands.put(VitalType.HEART_RATE, new ThresholdBand(
                50, 40,   // low amber, low red
                110, 130  // high amber, high red
        ));

        // RR (breaths/min)
        c.bands.put(VitalType.RESP_RATE, new ThresholdBand(
                10, 8,
                24, 30
        ));

        // Temp (°C)
        c.bands.put(VitalType.TEMPERATURE, new ThresholdBand(
                36.0, 35.0,
                38.0, 39.0
        ));

        // BP systolic (mmHg)
        c.bands.put(VitalType.BP_SYSTOLIC, new ThresholdBand(
                80, 70,
                160, 180
        ));

        // BP diastolic (mmHg)
        c.bands.put(VitalType.BP_DIASTOLIC, new ThresholdBand(
                50, 40,
                100, 110
        ));

        return c;
    }
}
