package rpm.ui.bindings;

import rpm.domain.VitalType;

// Converts vital enums into readable string units
public final class UnitFormatter {
    public static String unit(VitalType t) {
        switch (t) {
            case HEART_RATE: return "bpm";
            case RESP_RATE: return "br/min";
            case BP_SYSTOLIC:
            case BP_DIASTOLIC: return "mmHg";
            case TEMPERATURE: return "°C";
            default: return "";
        }
    }

    // Prevent instantiation with private constructor
    private UnitFormatter() {}
}
