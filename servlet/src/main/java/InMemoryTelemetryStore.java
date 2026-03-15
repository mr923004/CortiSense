import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTelemetryStore extends AbstractTelemetryStore {
    private final Map<String, PatientTelemetry> store = new ConcurrentHashMap<>();

    @Override
    public void store(String bedId, PatientTelemetry incoming) {
        if (bedId == null || incoming == null) return;
        store.compute(bedId, (id, existing) -> {
            if (existing == null) return incoming;
            existing.append(incoming);
            return existing;
        });
    }

    @Override
    public PatientTelemetry get(String bedId) {
        return bedId == null ? null : store.get(bedId);
    }

    @Override
    public Map<String, PatientTelemetry> getAll() {
        return store;
    }
}

