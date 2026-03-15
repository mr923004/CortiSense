package rpm.domain.alarm;

import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.simulation.WardDataListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class AlarmService implements WardDataListener {

    private final AlarmEngine engine;
    private final List<AlarmListener> listeners = new ArrayList<>();

    public AlarmService(AlarmEngine engine) {
        this.engine = engine;
    }
    public AlarmState getState(PatientId id) { return engine.getState(id); }

    public void addListener(AlarmListener l) {
        if (l != null) listeners.add(l);
    }

    public void removeListener(AlarmListener l) {
        listeners.remove(l);
    }

    @Override
    public void onVitalsSnapshot(PatientId id, VitalSnapshot snapshot) {
        List<AlarmTransition> transitions = engine.update(id, snapshot);
        if (!transitions.isEmpty()) {
            for (AlarmListener l : listeners) {
                for (AlarmTransition t : transitions) {
                    l.onAlarmTransition(t);
                }
            }
        }
        // always publish current state - probs optional
        AlarmState state = engine.getState(id);
        if (state != null) {
            for (AlarmListener l : listeners) {
                l.onAlarmState(id, snapshot.getTimestamp(), state);
            }
        }
    }

    // not used for alarm logic rn, but required by interface:
    @Override public void onEcgSegment(PatientId id, Instant time, double[] segment) {}
    @Override public void onEventStarted(PatientId id, rpm.simulation.PatientEvent ev) {}
    @Override public void onEventEnded(PatientId id, rpm.simulation.PatientEvent ev) {}
    // currently identical to super so redundant, but change for UI
    // - sb
}
