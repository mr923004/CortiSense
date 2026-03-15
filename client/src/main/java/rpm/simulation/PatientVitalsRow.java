package rpm.simulation;

import rpm.domain.PatientId;

import java.time.Instant;

public final class PatientVitalsRow {

    // Identifies which patient this row belongs to
    private final PatientId patientId;

    // Time when the vitals were recorded
    private final Instant timestamp;

    // Optional display label (e.g. patient name or bed label)
    private final String label;

    // Vital values
    private final double hr;    // heart rate
    private final double rr;    // respiratory rate
    private final double sys;   // systolic blood pressure
    private final double dia;   // diastolic blood pressure
    private final double temp;  // body temperature

    public PatientVitalsRow(PatientId patientId,
                            Instant timestamp,
                            double hr,
                            double rr,
                            double sys,
                            double dia,
                            double temp) {
        // Default constructor without a label
        this(patientId, timestamp, "", hr, rr, sys, dia, temp);
    }

    public PatientVitalsRow(PatientId patientId,
                            Instant timestamp,
                            String label,
                            double hr,
                            double rr,
                            double sys,
                            double dia,
                            double temp) {
        this.patientId = patientId;
        this.timestamp = timestamp;

        // Avoid storing null labels
        this.label = label != null ? label : "";

        this.hr = hr;
        this.rr = rr;
        this.sys = sys;
        this.dia = dia;
        this.temp = temp;
    }

    public PatientId getPatientId() { return patientId; }
    public Instant getTimestamp()   { return timestamp; }
    public String getLabel()        { return label; }
    public double getHr()           { return hr; }
    public double getRr()           { return rr; }
    public double getSys()          { return sys; }
    public double getDia()          { return dia; }
    public double getTemp()         { return temp; }
}
