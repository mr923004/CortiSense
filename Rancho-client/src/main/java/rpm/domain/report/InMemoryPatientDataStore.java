package rpm.domain.report;

import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.domain.alarm.AlarmListener;
import rpm.domain.alarm.AlarmTransition;
import rpm.simulation.PatientEvent;
import rpm.simulation.WardDataListener;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/*
 - Temporary in-memory store used for testing reports.
 - Keeps a rolling window of vital snapshots and alarm transitions.
 - This will be replaced with persistent storage once the servlet layer is ready.
 */
public final class InMemoryPatientDataStore
        implements PatientDataSource, WardDataListener, AlarmListener {

    // How long data should be kept in memory
    private final Duration retention;

    // Recent vital snapshots per patient
    private final Map<PatientId, Deque<VitalSnapshot>> vitalsByPatient = new HashMap<>();

    // Recent alarm transitions per patient
    private final Map<PatientId, Deque<AlarmTransition>> alarmsByPatient = new HashMap<>();

    public InMemoryPatientDataStore(Duration retention) {
        this.retention = retention;
    }

    // WardDataListener

    @Override
    public synchronized void onVitalsSnapshot(PatientId id, VitalSnapshot snapshot) {
        Deque<VitalSnapshot> q =
                vitalsByPatient.computeIfAbsent(id, k -> new ArrayDeque<>());
        q.addLast(snapshot);
        pruneVitals(q, snapshot.getTimestamp());
    }

    @Override
    public void onEcgSegment(PatientId id, Instant time, double[] segment) {
        // Not needed for the current reports
    }

    @Override
    public void onEventStarted(PatientId id, PatientEvent ev) {
        // Could store events here later if reporting needs it
    }

    @Override
    public void onEventEnded(PatientId id, PatientEvent ev) {
        // Optional
    }

    // AlarmListener

    @Override
    public synchronized void onAlarmTransition(AlarmTransition transition) {
        PatientId id = transition.getPatientId();
        Deque<AlarmTransition> q =
                alarmsByPatient.computeIfAbsent(id, k -> new ArrayDeque<>());
        q.addLast(transition);
        pruneAlarms(q, transition.getTime());
    }

    @Override
    public void onAlarmState(PatientId id, Instant time,
                             rpm.domain.alarm.AlarmState state) {
        // Only transitions are stored for reporting at the moment
    }

    // PatientDataSource

    @Override
    public synchronized List<VitalSnapshot> getVitals(
            PatientId id, Instant from, Instant to) {

        Deque<VitalSnapshot> q = vitalsByPatient.get(id);
        if (q == null) return Collections.emptyList();

        return q.stream()
                .filter(s -> !s.getTimestamp().isBefore(from)
                        && !s.getTimestamp().isAfter(to))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<AlarmTransition> getAlarmTransitions(
            PatientId id, Instant from, Instant to) {

        Deque<AlarmTransition> q = alarmsByPatient.get(id);
        if (q == null) return Collections.emptyList();

        return q.stream()
                .filter(a -> !a.getTime().isBefore(from)
                        && !a.getTime().isAfter(to))
                .collect(Collectors.toList());
    }

    // Remove old vital snapshots outside the retention window
    private void pruneVitals(Deque<VitalSnapshot> q, Instant now) {
        Instant cutoff = now.minus(retention);
        while (!q.isEmpty() && q.peekFirst().getTimestamp().isBefore(cutoff)) {
            q.removeFirst();
        }
    }

    // Remove old alarm transitions outside the retention window
    private void pruneAlarms(Deque<AlarmTransition> q, Instant now) {
        Instant cutoff = now.minus(retention);
        while (!q.isEmpty() && q.peekFirst().getTime().isBefore(cutoff)) {
            q.removeFirst();
        }
    }
}
