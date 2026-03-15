import java.util.Map;

public abstract class AbstractTelemetryStore {
    public abstract void store(String bedId, PatientTelemetry data);
    public abstract PatientTelemetry get(String bedId);
    public abstract Map<String, PatientTelemetry> getAll();
}
