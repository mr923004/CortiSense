package rpm.telemetry;

import java.util.HashMap;
import java.util.Map;
import rpm.simulation.WardManager;

//Adapts the WardManager to the Telemetrable interface

public class WardTelemetryAdapter implements Telemetrable {

    private final WardManager ward;

    // Per-bed telemetry buffer
    private final Map<String, PatientTelemetrySnapshot> buffer =
            new HashMap<>();

    private double round1dp(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public WardTelemetryAdapter(WardManager ward) {
        this.ward = ward;
    }

    //Collect latest vitals for ALL beds and append to the buffer.

    @Override
    public Map<String, PatientTelemetrySnapshot> snapshot() {

        ward.getLatestVitalsTable().forEach(row -> {

            String bedId =
                    row.getPatientId().getDisplayName();

            PatientTelemetrySnapshot snap =
                    buffer.computeIfAbsent(
                            bedId,
                            id -> new PatientTelemetrySnapshot()
                    );

            snap.hr().add(round1dp(row.getHr()));
            snap.rr().add(round1dp(row.getRr()));
            snap.sys().add(round1dp(row.getSys()));
            snap.dia().add(round1dp(row.getDia()));
            snap.temp().add(round1dp(row.getTemp()));
        });

        return buffer;
    }
}