package rpm.telemetry;

import java.util.Map;

//Any telemetry provider (simulation, real device, replay, etc.) must implement this interface.

public interface Telemetrable {

    //@return snapshot of telemetry data for all patients keyed by bed ID (e.g. Bed 01)
    Map<String, PatientTelemetrySnapshot> snapshot();
}