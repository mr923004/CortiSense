package rpm.ui.alerts;

import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.domain.VitalType;

import java.util.Map;

/*
- Lightweight data model used by the alert UI tiles.
- Holds the latest vital values and alert state for one patient.
*/
public final class AlertTileModel {

    // Identity and display info
    public final PatientId id;
    public final String name;

    // Latest vital values
    public final double hr, rr, sys, dia, temp;

    // Alert state flags
    public final boolean alerting;
    public final boolean acknowledged;

    private AlertTileModel(PatientId id, String name,
                           double hr, double rr, double sys, double dia, double temp,
                           boolean alerting, boolean acknowledged) {
        this.id = id;
        this.name = name;
        this.hr = hr;
        this.rr = rr;
        this.sys = sys;
        this.dia = dia;
        this.temp = temp;
        this.alerting = alerting;
        this.acknowledged = acknowledged;
    }

    // Build a tile model from a snapshot and current alert state
    public static AlertTileModel from(PatientId id, String name, VitalSnapshot snap,
                                      boolean alerting, boolean acknowledged) {

        // Safely extract the values map (snapshot may be null)
        Map<VitalType, Double> v =
                (snap == null) ? java.util.Collections.emptyMap() : snap.getValues();

        return new AlertTileModel(
                id, name,
                get(v, VitalType.HEART_RATE),
                get(v, VitalType.RESP_RATE),
                get(v, VitalType.BP_SYSTOLIC),
                get(v, VitalType.BP_DIASTOLIC),
                get(v, VitalType.TEMPERATURE),
                alerting, acknowledged
        );
    }

    // Read a vital value safely, returning NaN if missing
    private static double get(Map<VitalType, Double> m, VitalType t) {
        Double x = m.get(t);
        return x == null ? Double.NaN : x;
    }
}
