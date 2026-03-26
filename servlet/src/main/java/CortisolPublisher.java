import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ticks the cortisol simulator every real second and writes readings
 * directly into the shared InMemoryTelemetryStore.
 *
 * Wall-clock tick  : 1 second
 * Simulated advance: 60 seconds per tick  (x60 acceleration)
 */
public class CortisolPublisher {

    private static final Logger log = Logger.getLogger(CortisolPublisher.class.getName());

    private static final long   WALL_TICK_MS = 1_000L;
    private static final double SIM_TICK_SEC = 60.0;
    private static final String SUBJECT_ID   = "user-1";

    private final InMemoryTelemetryStore store;
    private final CortisolSimulator      simulator = new CortisolSimulator();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "cortisol-publisher");
                t.setDaemon(true);
                return t;
            });

    public CortisolPublisher(InMemoryTelemetryStore store) {
        this.store = store;
    }

    public void start() {
        // Advance the simulator's internal state to match the 120 readings
        long seedStartMs = System.currentTimeMillis() - 120L * 60_000L;
        for (int i = 0; i < 120; i++) {
            simulator.tick(seedStartMs + (long) i * 60_000L, SIM_TICK_SEC);
        }

        scheduler.scheduleAtFixedRate(this::tick, 1L, WALL_TICK_MS, TimeUnit.MILLISECONDS);
        log.info("CortisolPublisher: ticking every " + WALL_TICK_MS + "ms (direct store writes)");
    }

    public void stop() {
        scheduler.shutdownNow();
        log.info("CortisolPublisher: stopped.");
    }

    private void tick() {
        try {
            long nowMs = System.currentTimeMillis();
            CortisolReading reading = simulator.tick(nowMs, SIM_TICK_SEC);
            if (Double.isNaN(reading.latest())) return;
            store.store(SUBJECT_ID, reading);
        } catch (Exception e) {
            log.log(Level.WARNING, "tick error", e);
        }
    }
}