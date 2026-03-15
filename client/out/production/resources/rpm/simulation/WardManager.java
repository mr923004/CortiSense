package rpm.simulation;

import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.domain.VitalType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WardManager {
    public static final int MIN_PATIENTS = 8;

    private final Map<PatientId, PatientSimulator> patients = new LinkedHashMap<>();
    private final Map<PatientId, VitalSnapshot> latestSnapshots = new LinkedHashMap<>();
    private final Map<PatientId, double[]> lastEcgSegments = new LinkedHashMap<>();
    private final Map<PatientId, PatientCard> cards = new LinkedHashMap<>();

    private final List<WardDataListener> listeners = new ArrayList<>();

    private final Random random = new Random();

    private int nextIdValue = 1;
    private PatientId selectedPatientId = new PatientId(1);
    private double secondsSinceLastVitals = 0.0;

    public WardManager(int initialPatients) {
        int n = Math.max(MIN_PATIENTS, initialPatients);
        Instant now = Instant.now();
        for (int i = 0; i < n; i++) {
            addPatientInternal(now);
        }
        selectedPatientId = new PatientId(1);
    }

    public synchronized void addListener(WardDataListener listener) {
        if (listener == null) return;
        listeners.add(listener);
    }

    public synchronized void removeListener(WardDataListener listener) {
        listeners.remove(listener);
    }

    public synchronized List<PatientId> getPatientIds() {
        return new ArrayList<>(patients.keySet());
    }

    public synchronized int getPatientCount() {
        return patients.size();
    }

    public synchronized PatientId getSelectedPatientId() {
        return selectedPatientId;
    }

    public synchronized void setSelectedPatientId(PatientId id) {
        if (id == null || !patients.containsKey(id)) {
            selectedPatientId = new PatientId(1);
            return;
        }
        selectedPatientId = id;
    }

    public synchronized PatientCard getPatientCard(PatientId id) {
        return cards.get(id);
    }

    public synchronized PatientId addPatient() {
        return addPatientInternal(Instant.now());
    }

    public synchronized boolean removePatient(PatientId id) {
        if (id == null || !patients.containsKey(id)) return false;
        if (id.getValue() <= MIN_PATIENTS) return false;
        if (patients.size() <= MIN_PATIENTS) return false;

        patients.remove(id);
        latestSnapshots.remove(id);
        lastEcgSegments.remove(id);
        cards.remove(id);

        if (id.equals(selectedPatientId)) {
            selectedPatientId = new PatientId(1);
        }
        return true;
    }

    public synchronized void triggerEvent(PatientId id, PatientEventType type) {
        triggerEvent(id, type, Instant.now());
    }

    public synchronized void triggerEvent(PatientId id, PatientEventType type, Instant time) {
        PatientSimulator sim = patients.get(id);
        if (sim == null) return;
        sim.triggerEvent(type, time);
    }

    public synchronized void tick(Instant time, double dtSeconds) {
        List<WardDataListener> ls = new ArrayList<>(listeners);

        for (Map.Entry<PatientId, PatientSimulator> e : patients.entrySet()) {
            PatientId id = e.getKey();
            PatientSimulator sim = e.getValue();

            sim.advanceEcg(dtSeconds);
            double[] seg = sim.getLastEcgSegment();
            lastEcgSegments.put(id, seg);

            if (seg != null && seg.length > 0) {
                for (WardDataListener l : ls) {
                    l.onEcgSegment(id, time, seg);
                }
            }
        }

        secondsSinceLastVitals += dtSeconds;
        if (secondsSinceLastVitals < 1.0) return;

        secondsSinceLastVitals -= 1.0;

        for (Map.Entry<PatientId, PatientSimulator> entry : patients.entrySet()) {
            PatientId id = entry.getKey();
            PatientSimulator sim = entry.getValue();

            VitalSnapshot snap = sim.nextSnapshot(time);
            latestSnapshots.put(id, snap);

            for (WardDataListener l : ls) {
                l.onVitalsSnapshot(id, snap);
            }

            for (PatientEvent ev : sim.drainStartedEvents()) {
                for (WardDataListener l : ls) {
                    l.onEventStarted(id, ev);
                }
            }

            for (PatientEvent ev : sim.drainEndedEvents()) {
                for (WardDataListener l : ls) {
                    l.onEventEnded(id, ev);
                }
            }
        }
    }

    public synchronized List<PatientVitalsRow> getLatestVitalsTable() {
        List<PatientVitalsRow> rows = new ArrayList<>(patients.size());

        for (Map.Entry<PatientId, VitalSnapshot> entry : latestSnapshots.entrySet()) {
            PatientId id = entry.getKey();
            VitalSnapshot snap = entry.getValue();
            Map<VitalType, Double> values = snap.getValues();

            String label = "";
            PatientCard card = cards.get(id);
            if (card != null) label = card.getLabel();

            rows.add(new PatientVitalsRow(
                    id,
                    snap.getTimestamp(),
                    label,
                    safeGet(values, VitalType.HEART_RATE),
                    safeGet(values, VitalType.RESP_RATE),
                    safeGet(values, VitalType.BP_SYSTOLIC),
                    safeGet(values, VitalType.BP_DIASTOLIC),
                    safeGet(values, VitalType.TEMPERATURE)
            ));
        }

        return rows;
    }

    public synchronized VitalSnapshot getPatientLatestSnapshot(PatientId id) {
        return latestSnapshots.get(id);
    }

    public synchronized VitalSnapshot getSelectedPatientLatestSnapshot() {
        return latestSnapshots.get(selectedPatientId);
    }

    public synchronized double[] getPatientLastEcgSegment(PatientId id) {
        double[] seg = lastEcgSegments.get(id);
        return seg != null ? seg : new double[0];
    }

    public synchronized double[] getSelectedPatientLastEcgSegment() {
        return getPatientLastEcgSegment(selectedPatientId);
    }

    private PatientId addPatientInternal(Instant now) {
        PatientId id = new PatientId(nextIdValue++);
        int bed = id.getValue();

        PatientScenario scenario;
        PatientProfile profile;
        PatientCard card;

        if (bed <= MIN_PATIENTS) {
            PatientTemplate template = MainPatientCatalog.templateForBed(bed);
            if (template == null) {
                scenario = PatientScenario.NORMAL_ADULT;
                profile = new PatientProfile(
                        scenario.getHrBaseline(),
                        scenario.getRrBaseline(),
                        scenario.getBpBaselineSystolic(),
                        scenario.getBpBaselineDiastolic(),
                        scenario.getTempBaseline()
                );
                card = new PatientCard(id, "Main patient", null);
            } else {
                scenario = template.getScenario();
                profile = template.getProfile();
                card = new PatientCard(id, template.getLabel(), template.getConditions());
            }
        } else {
            scenario = PatientScenario.NORMAL_ADULT;
            profile = PatientProfile.generateNormal(scenario, random);
            card = new PatientCard(id, "Healthy", null);
        }

        PatientSimulator simulator = PatientScenarioFactory.create(scenario, profile);
        simulator.setChronicConditions(card.getConditions());

        patients.put(id, simulator);
        cards.put(id, card);

        VitalSnapshot first = simulator.nextSnapshot(now);
        latestSnapshots.put(id, first);
        lastEcgSegments.put(id, simulator.getLastEcgSegment());

        return id;
    }

    private static double safeGet(Map<VitalType, Double> values, VitalType type) {
        Double v = values.get(type);
        return v != null ? v : Double.NaN;
    }
}
