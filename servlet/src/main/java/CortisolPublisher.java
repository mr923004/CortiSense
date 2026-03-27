import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CortisolPublisher {

    private static final Logger log = Logger.getLogger(CortisolPublisher.class.getName());

    private static final long   WALL_TICK_MS = 1_000L;
    private static final double SIM_TICK_SEC = 60.0;

    private final InMemoryTelemetryStore store;

    // Hold multiple simulators
    private final Map<String, CortisolSimulator> simulators = new HashMap<>();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "cortisol-publisher");
                t.setDaemon(true);
                return t;
            });

    public CortisolPublisher(InMemoryTelemetryStore store) {
        this.store = store;

        // Add many simulated patients
        simulators.put("user-1", new CortisolSimulator(42));
        simulators.put("user-2", new CortisolSimulator(99));
        simulators.put("user-3", new CortisolSimulator(123));
        simulators.put("user-4", new CortisolSimulator(225));
        simulators.put("user-5", new CortisolSimulator(397));
        simulators.put("user-6", new CortisolSimulator(487));
    }

    public void start() {
        // Seed 120 readings for EVERY patient so the graphs start full
        long seedStartMs = System.currentTimeMillis() - 120L * 60_000L;
        for (int i = 0; i < 120; i++) {
            long ts = seedStartMs + (long) i * 60_000L;
            for (Map.Entry<String, CortisolSimulator> entry : simulators.entrySet()) {
                CortisolReading reading = entry.getValue().tick(ts, SIM_TICK_SEC);
                store.store(entry.getKey(), reading);
            }
        }
        log.info("CortiSense: Seeded initial data for all patients.");

        scheduler.scheduleAtFixedRate(this::tick, 1L, WALL_TICK_MS, TimeUnit.MILLISECONDS);
        log.info("CortisolPublisher: ticking every " + WALL_TICK_MS + "ms");
    }

    public void stop() {
        scheduler.shutdownNow();
        log.info("CortisolPublisher: stopped.");
    }

    private void tick() {
        try {
            long nowMs = System.currentTimeMillis();
            // Loop through every patient and generate a reading for them
            for (Map.Entry<String, CortisolSimulator> entry : simulators.entrySet()) {
                CortisolReading reading = entry.getValue().tick(nowMs, SIM_TICK_SEC);
                if (!Double.isNaN(reading.latest())) {
                    store.store(entry.getKey(), reading);
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "tick error", e);
        }
    }
}