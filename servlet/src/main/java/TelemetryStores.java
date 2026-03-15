import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TelemetryStores {
    private static final Logger log = Logger.getLogger(TelemetryStores.class.getName());

    private TelemetryStores() {}

    public static AbstractTelemetryStore create() {
        String backend = System.getenv("TELEMETRY_BACKEND");
        if (backend == null || backend.isBlank()) backend = "memory";
        backend = backend.trim().toLowerCase(Locale.ROOT);

        if ("db".equals(backend)) {
            DbConfig cfg = DbConfig.fromEnv();
            if (cfg == null) {
                log.warning("Telemetry backend=db but DB config missing (need DB_URL or PG* vars). Falling back to memory.");
                return new InMemoryTelemetryStore();
            }

            try {
                log.info("Telemetry backend=db (" + cfg.sanitized() + ")");
                return new DbTelemetryStore(cfg);
            } catch (RuntimeException e) {
                log.log(Level.WARNING, "DB init failed; falling back to memory", e);
                return new InMemoryTelemetryStore();
            }
        }

        log.info("Telemetry backend=memory");
        return new InMemoryTelemetryStore();
    }
}
