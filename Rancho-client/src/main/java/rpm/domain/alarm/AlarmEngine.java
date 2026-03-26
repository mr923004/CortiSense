package rpm.domain.alarm;

import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.domain.VitalType;

import java.time.Instant;
import java.util.*;

public final class AlarmEngine {

    // Alarm thresholds, persistence times and hysteresis configuration
    private final AlarmConfig config;

    // Trackers per patient and per vital
    private final Map<PatientId, EnumMap<VitalType, VitalTracker>> trackers = new HashMap<>();

    // Last calculated alarm state for each patient
    private final Map<PatientId, AlarmState> lastState = new HashMap<>();

    public AlarmEngine(AlarmConfig config) {
        this.config = config;
    }

    // Returns the latest alarm state for the patient
    public AlarmState getState(PatientId id) {
        return lastState.get(id);
    }

    public List<AlarmTransition> update(PatientId id, VitalSnapshot snapshot) {

        // Create tracker map for this patient if it does not exist yet
        EnumMap<VitalType, VitalTracker> perVital =
                trackers.computeIfAbsent(id, k -> new EnumMap<>(VitalType.class));

        Instant t = snapshot.getTimestamp();
        Map<VitalType, Double> values = snapshot.getValues();

        List<AlarmTransition> transitions = new ArrayList<>();
        EnumMap<VitalType, VitalAlarmStatus> statusMap = new EnumMap<>(VitalType.class);

        AlarmLevel overall = AlarmLevel.GREEN;

        // Process each vital reading
        for (VitalType vt : VitalType.values()) {
            ThresholdBand band = config.band(vt);
            if (band == null) continue;   // No thresholds for this vital

            double v = values.getOrDefault(vt, Double.NaN);
            if (Double.isNaN(v) || Double.isInfinite(v)) continue;   // Skip invalid values

            VitalTracker tracker = perVital.computeIfAbsent(vt, k -> new VitalTracker(vt));
            AlarmLevel old = tracker.level;

            tracker.step(v, band, config, t);

            // Record a transition if the level changed
            if (tracker.level != old) {
                transitions.add(new AlarmTransition(
                        id, vt, old, tracker.level, t, tracker.reason
                ));
            }

            // Only store vitals that are not green
            if (tracker.level != AlarmLevel.GREEN) {
                statusMap.put(vt,
                        new VitalAlarmStatus(vt, tracker.level, tracker.reason, tracker.since));
            }

            // Keep track of the highest severity seen so far
            overall = AlarmLevel.max(overall, tracker.level);
        }

        AlarmState newState = new AlarmState(overall, statusMap);
        lastState.put(id, newState);
        return transitions;
    }

    // Tracks the alarm state of one vital for one patient
    private static final class VitalTracker {
        final VitalType type;

        AlarmLevel level = AlarmLevel.GREEN;

        // Counts how long the signal has stayed amber or red
        int amberCount = 0;
        int redCount = 0;

        Instant since = null;    // When the current level started
        String reason = "";     // Short description for display

        VitalTracker(VitalType type) {
            this.type = type;
        }

        void step(double value, ThresholdBand b, AlarmConfig cfg, Instant t) {

            // Classify current value using thresholds only
            AlarmLevel instantaneous = classify(value, b);

            // Update persistence counters
            if (instantaneous == AlarmLevel.RED) redCount++;
            else redCount = 0;

            if (instantaneous == AlarmLevel.AMBER) amberCount++;
            else amberCount = 0;

            AlarmLevel next = level;

            // Escalate situation after enough consecutive samples
            if (level != AlarmLevel.RED && redCount >= cfg.redPersistSeconds) {
                next = AlarmLevel.RED;
            } else if (level == AlarmLevel.GREEN && amberCount >= cfg.amberPersistSeconds) {
                next = AlarmLevel.AMBER;
            }

            // De-escalate situation slowly to avoid rapid switching
            if (level == AlarmLevel.RED
                    && instantaneous != AlarmLevel.RED
                    && isClearlyNotRed(value, b, cfg.hysteresis)) {
                next = AlarmLevel.AMBER;
            }

            if (level == AlarmLevel.AMBER
                    && instantaneous == AlarmLevel.GREEN
                    && isClearlyGreen(value, b, cfg.hysteresis)) {
                next = AlarmLevel.GREEN;
            }

            if (next != level) {
                level = next;
                since = t;
            }

            reason = buildReason(value, b, level);
            // Clear the reason and timestamp when the alarm is no longer active

            if (level == AlarmLevel.GREEN) {
                since = null;
                reason = "";
            }
        }

        private static AlarmLevel classify(double v, ThresholdBand b) {
            if (v <= b.lowRed || v >= b.highRed) return AlarmLevel.RED;
            if (v <= b.lowAmber || v >= b.highAmber) return AlarmLevel.AMBER;
            return AlarmLevel.GREEN;
        }

        private static boolean isClearlyGreen(double v, ThresholdBand b, double h) {
            return (v > b.lowAmber + h) && (v < b.highAmber - h);
        }

        private static boolean isClearlyNotRed(double v, ThresholdBand b, double h) {
            return (v > b.lowRed + h) && (v < b.highRed - h);
        }

        // Builds a short reason string for the UI
        private static String buildReason(double v, ThresholdBand b, AlarmLevel level) {
            if (level == AlarmLevel.GREEN) return "";

            boolean low = v <= b.lowAmber;
            boolean high = v >= b.highAmber;

            if (level == AlarmLevel.RED) {
                if (v <= b.lowRed) return "Critically low";
                if (v >= b.highRed) return "Critically high";
            }

            if (low) return "Low";
            if (high) return "High";
            return "Abnormal";
        }
    }
}
