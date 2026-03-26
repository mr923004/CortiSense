import java.util.Map;

/**
 * Abstract base for all telemetry storage backends.
 * The only implementation used in CortiSense is {@link InMemoryTelemetryStore}.
 */
public abstract class AbstractTelemetryStore {
    public abstract void store(String subjectId, CortisolReading data);
    public abstract CortisolReading get(String subjectId);
    public abstract Map<String, CortisolReading> getAll();
}