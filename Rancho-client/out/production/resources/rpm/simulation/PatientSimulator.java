package rpm.simulation;

import rpm.domain.VitalSnapshot;
import rpm.domain.VitalType;
import rpm.ecg.EcgGenerator;
import rpm.ecg.EcgsynGenerator;
import rpm.ecg.EcgRingBuffer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PatientSimulator {
    private final HeartRateSimulator heartRateSimulator;
    private final RespRateSimulator respRateSimulator;
    private final BloodPressureSimulator bloodPressureSimulator;
    private final TemperatureSimulator temperatureSimulator;

    private final double ecgSamplingFrequencyHz = 250.0;
    private final double ecgWindowSeconds = 4.0;

    private final EcgGenerator ecgGenerator;
    private final EcgRingBuffer ecgRingBuffer;

    private double[] lastEcgSegment = new double[0];
    private double lastHeartRateBpm = 75.0;

    private EnumSet<ChronicCondition> chronicConditions = EnumSet.noneOf(ChronicCondition.class);

    private final Random modifierRandom = new Random();
    private final List<PatientEvent> activeEvents = new ArrayList<>();
    private final List<PatientEvent> startedEvents = new ArrayList<>();
    private final List<PatientEvent> endedEvents = new ArrayList<>();

    public PatientSimulator() {
        this.heartRateSimulator = new HeartRateSimulator();
        this.respRateSimulator = new RespRateSimulator();
        this.bloodPressureSimulator = new BloodPressureSimulator();
        this.temperatureSimulator = new TemperatureSimulator();

        this.ecgGenerator = new EcgsynGenerator();
        int ecgWindowSamples = (int) Math.round(ecgSamplingFrequencyHz * ecgWindowSeconds);
        this.ecgRingBuffer = new EcgRingBuffer(ecgWindowSamples);
    }

    public PatientSimulator(HeartRateSimulator heartRateSimulator,
                            RespRateSimulator respRateSimulator,
                            BloodPressureSimulator bloodPressureSimulator,
                            TemperatureSimulator temperatureSimulator) {
        this.heartRateSimulator = heartRateSimulator;
        this.respRateSimulator = respRateSimulator;
        this.bloodPressureSimulator = bloodPressureSimulator;
        this.temperatureSimulator = temperatureSimulator;

        this.ecgGenerator = new EcgsynGenerator();
        int ecgWindowSamples = (int) Math.round(ecgSamplingFrequencyHz * ecgWindowSeconds);
        this.ecgRingBuffer = new EcgRingBuffer(ecgWindowSamples);
    }

    public void setChronicConditions(EnumSet<ChronicCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            this.chronicConditions = EnumSet.noneOf(ChronicCondition.class);
            return;
        }
        this.chronicConditions = EnumSet.copyOf(conditions);
    }

    public EnumSet<ChronicCondition> getChronicConditions() {
        return EnumSet.copyOf(chronicConditions);
    }

    public void triggerEvent(PatientEventType type, Instant startTime) {
        if (type == null || startTime == null) return;
        if (activeEvents.size() >= 2) return;

        PatientEvent event = PatientEvent.create(type, startTime);
        activeEvents.add(event);
        startedEvents.add(event);
    }

    public List<PatientEvent> drainStartedEvents() {
        List<PatientEvent> out = new ArrayList<>(startedEvents);
        startedEvents.clear();
        return out;
    }

    public List<PatientEvent> drainEndedEvents() {
        List<PatientEvent> out = new ArrayList<>(endedEvents);
        endedEvents.clear();
        return out;
    }

    public VitalSnapshot nextSnapshot(Instant time) {
        removeFinishedEvents(time);
        maybeStartRandomEvent(time);

        double heartRate = heartRateSimulator.nextValue(time);
        double respRate = respRateSimulator.nextValue(time);
        double systolic = bloodPressureSimulator.nextValue(time);
        double diastolic = bloodPressureSimulator.getCurrentDiastolic();
        double temperature = temperatureSimulator.nextValue(time);

        VitalDelta delta = new VitalDelta();
        applyChronicModifiers(delta);
        applyEventModifiers(time, delta);

        heartRate += delta.hr;
        respRate += delta.rr;
        systolic += delta.sys;
        diastolic += delta.dia;
        temperature += delta.temp;

        heartRate = clamp(heartRate, 30.0, 220.0);
        respRate = clamp(respRate, 4.0, 60.0);
        systolic = clamp(systolic, 60.0, 240.0);
        diastolic = clamp(diastolic, 30.0, 160.0);
        temperature = clamp(temperature, 33.0, 41.5);

        lastHeartRateBpm = heartRate;

        Map<VitalType, Double> values = new EnumMap<>(VitalType.class);
        values.put(VitalType.HEART_RATE, heartRate);
        values.put(VitalType.RESP_RATE, respRate);
        values.put(VitalType.BP_SYSTOLIC, systolic);
        values.put(VitalType.BP_DIASTOLIC, diastolic);
        values.put(VitalType.TEMPERATURE, temperature);

        return new VitalSnapshot(time, values);
    }

    public void advanceEcg(double durationSeconds) {
        if (durationSeconds <= 0.0) return;

        double[] segment = ecgGenerator.generateSegment(
                durationSeconds,
                lastHeartRateBpm,
                ecgSamplingFrequencyHz
        );

        if (segment == null || segment.length == 0) return;

        lastEcgSegment = segment;
        ecgRingBuffer.append(segment);
    }

    public double[] getCurrentEcgWindow() {
        return ecgRingBuffer.getLatestWindow();
    }

    public double[] getLastEcgSegment() {
        return lastEcgSegment;
    }

    public double getEcgSamplingFrequencyHz() {
        return ecgSamplingFrequencyHz;
    }

    private void applyChronicModifiers(VitalDelta d) {
        if (chronicConditions.contains(ChronicCondition.HYPERTENSION)) {
            d.sys += 6.0;
            d.dia += 3.0;
            d.sys += modifierRandom.nextGaussian() * 0.8;
        }
        if (chronicConditions.contains(ChronicCondition.COPD_TENDENCY)) {
            d.rr += 1.5;
            d.rr += modifierRandom.nextGaussian() * 0.3;
        }
        if (chronicConditions.contains(ChronicCondition.BRADYCARDIA_TENDENCY)) {
            d.hr -= 4.0;
        }
        if (chronicConditions.contains(ChronicCondition.HEART_FAILURE_RISK)) {
            d.rr += 1.2;
            d.sys -= 3.0;
        }
        if (chronicConditions.contains(ChronicCondition.INFECTION_RISK)) {
            d.temp += 0.05;
        }
        if (chronicConditions.contains(ChronicCondition.ARRHYTHMIA_TENDENCY)) {
            d.hr += modifierRandom.nextGaussian() * 1.2;
        }
    }

    private void applyEventModifiers(Instant time, VitalDelta d) {
        for (PatientEvent e : activeEvents) {
            double k = e.intensityAt(time);
            if (k <= 0.0) continue;

            VitalDelta peak = peakDeltaFor(e.getType());
            d.hr += peak.hr * k;
            d.rr += peak.rr * k;
            d.sys += peak.sys * k;
            d.dia += peak.dia * k;
            d.temp += peak.temp * k;
        }
    }

    private VitalDelta peakDeltaFor(PatientEventType type) {
        VitalDelta d = new VitalDelta();
        switch (type) {
            case FEVER_SPIKE:
                d.temp = 1.8;
                d.hr = 12.0;
                d.rr = 4.0;
                return d;

            case TACHY_EPISODE:
                d.hr = 35.0;
                return d;

            case BP_SPIKE:
                d.sys = 30.0;
                d.dia = 15.0;
                d.hr = 6.0;
                return d;

            case BP_DROP:
                d.sys = -25.0;
                d.dia = -12.0;
                d.hr = 10.0;
                return d;

            case RESP_DISTRESS:
                d.rr = 12.0;
                d.hr = 10.0;
                return d;

            case HEART_FAILURE_DECOMP:
                d.rr = 18.0;
                d.hr = 22.0;
                d.sys = -18.0;
                return d;

            case MI_LIKE:
                d.hr = 28.0;
                d.sys = -12.0;
                d.rr = 6.0;
                return d;

            case STROKE_LIKE:
                d.sys = 20.0;
                d.hr = 10.0;
                return d;

            default:
                return d;
        }
    }

    private void removeFinishedEvents(Instant time) {
        for (Iterator<PatientEvent> it = activeEvents.iterator(); it.hasNext();) {
            PatientEvent e = it.next();
            if (e.isFinished(time)) {
                it.remove();
                endedEvents.add(e);
            }
        }
    }

    private void maybeStartRandomEvent(Instant time) {
        if (activeEvents.size() >= 2) return;

        double chance = 0.004;

        if (chronicConditions.contains(ChronicCondition.INFECTION_RISK)) chance += 0.006;
        if (chronicConditions.contains(ChronicCondition.ARRHYTHMIA_TENDENCY)) chance += 0.005;
        if (chronicConditions.contains(ChronicCondition.COPD_TENDENCY)) chance += 0.004;
        if (chronicConditions.contains(ChronicCondition.HYPERTENSION)) chance += 0.003;
        if (chronicConditions.contains(ChronicCondition.HEART_FAILURE_RISK)) chance += 0.003;

        if (modifierRandom.nextDouble() >= chance) return;

        PatientEventType type = chooseEventType();
        triggerEvent(type, time);
    }

    private PatientEventType chooseEventType() {
        double r = modifierRandom.nextDouble();

        if (chronicConditions.contains(ChronicCondition.INFECTION_RISK) && r < 0.55) {
            return PatientEventType.FEVER_SPIKE;
        }
        if (chronicConditions.contains(ChronicCondition.ARRHYTHMIA_TENDENCY) && r < 0.55) {
            return PatientEventType.TACHY_EPISODE;
        }
        if (chronicConditions.contains(ChronicCondition.COPD_TENDENCY) && r < 0.55) {
            return PatientEventType.RESP_DISTRESS;
        }
        if (chronicConditions.contains(ChronicCondition.HYPERTENSION) && r < 0.55) {
            return PatientEventType.BP_SPIKE;
        }

        return r < 0.5 ? PatientEventType.TACHY_EPISODE : PatientEventType.BP_DROP;
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private static final class VitalDelta {
        double hr;
        double rr;
        double sys;
        double dia;
        double temp;
    }
}
