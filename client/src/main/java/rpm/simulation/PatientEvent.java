package rpm.simulation;

import java.time.Duration;
import java.time.Instant;

// Represents a temporary physiological event affecting a patient
// (e.g., fever spike, tachycardia episode, blood pressure spike).
public final class PatientEvent {

    private final PatientEventType type;
    private final Instant startTime;
    private final double durationSeconds;

    public PatientEvent(PatientEventType type,
                        Instant startTime,
                        double durationSeconds) {
        this.type = type;
        this.startTime = startTime;
        this.durationSeconds = durationSeconds;
    }

    public PatientEventType getType() {
        return type;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    // Returns true once the event duration has elapsed
    public boolean isFinished(Instant now) {
        return secondsSinceStart(now) >= durationSeconds;
    }

    // Computes the intensity of the event at the current time.
    // The intensity ramps up, stays at full strength, then fades out.
    // Output range: 0.0 → 1.0
    public double intensityAt(Instant now) {
        double t = secondsSinceStart(now) / durationSeconds;
        if (t <= 0.0 || t >= 1.0) return 0.0;

        double rampUp = 0.2;       // first 20% ramps up
        double plateauEnd = 0.6;  // next 40% stays at full strength

        if (t < rampUp) {
            return t / rampUp;
        }
        if (t < plateauEnd) {
            return 1.0;
        }
        return 1.0 - ((t - plateauEnd) / (1.0 - plateauEnd));
    }

    // Returns elapsed seconds since the event started
    private double secondsSinceStart(Instant now) {
        return Duration.between(startTime, now).toMillis() / 1000.0;
    }

    // Factory method for creating an event with a predefined duration
    public static PatientEvent create(PatientEventType type, Instant startTime) {
        return new PatientEvent(type, startTime, defaultDurationSeconds(type));
    }

    // Standard durations (seconds) for each medical event type
    private static double defaultDurationSeconds(PatientEventType type) {
        switch (type) {
            case FEVER_SPIKE:         return 240.0;
            case TACHY_EPISODE:       return 70.0;
            case BP_SPIKE:            return 90.0;
            case BP_DROP:             return 120.0;
            case RESP_DISTRESS:       return 180.0;

            case HEART_FAILURE_DECOMP:return 900.0;
            case MI_LIKE:             return 300.0;
            case STROKE_LIKE:         return 600.0;

            default:                 return 120.0;
        }
    }
}
