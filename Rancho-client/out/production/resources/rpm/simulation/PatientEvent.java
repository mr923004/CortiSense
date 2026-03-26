package rpm.simulation;

import java.time.Duration;
import java.time.Instant;

public final class PatientEvent {
    private final PatientEventType type;
    private final Instant startTime;
    private final double durationSeconds;

    public PatientEvent(PatientEventType type, Instant startTime, double durationSeconds) {
        this.type = type;
        this.startTime = startTime;
        this.durationSeconds = durationSeconds;
    }

    public PatientEventType getType() { return type; }
    public Instant getStartTime() { return startTime; }
    public double getDurationSeconds() { return durationSeconds; }

    public boolean isFinished(Instant now) {
        return secondsSinceStart(now) >= durationSeconds;
    }

    public double intensityAt(Instant now) {
        double t = secondsSinceStart(now) / durationSeconds;
        if (t <= 0.0 || t >= 1.0) return 0.0;

        double rampUp = 0.2;
        double plateauEnd = 0.6;

        if (t < rampUp) {
            return t / rampUp;
        }
        if (t < plateauEnd) {
            return 1.0;
        }
        return 1.0 - ((t - plateauEnd) / (1.0 - plateauEnd));
    }

    private double secondsSinceStart(Instant now) {
        return Duration.between(startTime, now).toMillis() / 1000.0;
    }

    public static PatientEvent create(PatientEventType type, Instant startTime) {
        return new PatientEvent(type, startTime, defaultDurationSeconds(type));
    }

    private static double defaultDurationSeconds(PatientEventType type) {
        switch (type) {
            case FEVER_SPIKE: return 240.0;
            case TACHY_EPISODE: return 70.0;
            case BP_SPIKE: return 90.0;
            case BP_DROP: return 120.0;
            case RESP_DISTRESS: return 180.0;

            case HEART_FAILURE_DECOMP: return 900.0;
            case MI_LIKE: return 300.0;
            case STROKE_LIKE: return 600.0;

            default: return 120.0;
        }
    }
}

