package rpm.ui.bindings;

// Format vital sign values into strings for UI display
public final class VitalDisplay {

    // Format a double to a 1 decimal place string
    public static String fmt1(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "--";
        return String.format("%.1f", v);
    }

    // Format a double to a 0 decimal place string
    public static String fmt0(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "--";
        return String.format("%.0f", v);
    }

    // Prevent instantiation of class with private constructor
    private VitalDisplay() {}
}
