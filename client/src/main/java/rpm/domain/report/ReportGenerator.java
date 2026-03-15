package rpm.domain.report;

import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.domain.VitalType;
import rpm.domain.alarm.AlarmTransition;

import java.time.Instant;
import java.util.*;

public final class ReportGenerator {

    // Builds a report for a patient over the given time range
    public PatientReport generate(PatientId patientId,
                                  Instant from,
                                  Instant to,
                                  PatientDataSource src) {

        // Load raw data from the data source
        List<VitalSnapshot> snaps = src.getVitals(patientId, from, to);
        List<AlarmTransition> alarms = src.getAlarmTransitions(patientId, from, to);

        // Accumulators used to calculate statistics for each vital
        EnumMap<VitalType, StatsAccumulator> acc =
                new EnumMap<>(VitalType.class);
        for (VitalType vt : VitalType.values()) {
            acc.put(vt, new StatsAccumulator());
        }

        // Feed all snapshot values into the accumulators
        for (VitalSnapshot s : snaps) {
            Map<VitalType, Double> vals = s.getValues();
            for (Map.Entry<VitalType, Double> e : vals.entrySet()) {
                VitalType vt = e.getKey();
                Double v = e.getValue();
                if (v == null || Double.isNaN(v) || Double.isInfinite(v)) continue;

                StatsAccumulator a = acc.get(vt);
                if (a != null) a.add(v);
            }
        }

        // Convert accumulators into final summaries
        EnumMap<VitalType, VitalSummary> summaries =
                new EnumMap<>(VitalType.class);
        for (Map.Entry<VitalType, StatsAccumulator> e : acc.entrySet()) {
            VitalSummary s = e.getValue().toSummary();
            if (s != null) summaries.put(e.getKey(), s);
        }

        return new PatientReport(patientId, from, to, summaries, alarms);
    }

    // Collects basic statistics for a single vital
    private static final class StatsAccumulator {
        int n = 0;
        double sum = 0.0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        void add(double v) {
            // Number of samples used for the report (around 60 for a 1 minute window)
            n++;
            sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
        }

        VitalSummary toSummary() {
            if (n == 0) return null;
            return new VitalSummary(n, min, max, sum / n);
        }
    }
}
