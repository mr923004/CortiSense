import java.util.Random;

/**
 * Simulates a physiologically plausible 24-hour cortisol profile.
 *
 * ── Circadian baseline
 *   Modelled as a cosine curve (standard approximation of the HPA axis rhythm):
 *     peak  ≈ 540 nmol/L  at 08:00  (28 800 s)
 *     nadir ≈ 100 nmol/L  at 20:00  (72 000 s)
 *   amplitude = 220 nmol/L,  midpoint = 320 nmol/L
 *
 * ── Stress-rise event
 *   One deterministic transient per simulated day, centred at 14:00.
 *   Shape  : symmetric triangle, ±30 min half-width
 *   Height : +220 nmol/L above the circadian baseline at the peak
 *   This represents an acute psychosocial stressor (e.g. work deadline).
 *
 * ── Noise
 *   Gaussian noise σ = 18 nmol/L added each tick (within-assay biological
 *   variability for a wearable biosensor proof-of-concept).
 *
 * ── Clamp
 *   Output is clamped to [50, 800] nmol/L.
 *
 * ── Tick rate
 *   Intended for one reading every 5 simulated minutes (SIM_TICK_SEC in
 *   CortisolPublisher).  Simulation starts at 06:00 so the first readings
 *   show a recognisable morning rise during a demo session.
 *
 * References:
 *   Smyth et al. (1997), Psychoneuroendocrinology — diurnal cortisol profiles.
 *   Pruessner et al. (1997), Psychoneuroendocrinology — cortisol awakening response.
 */

public class CortisolSimulator {

    // ── Circadian model
    private static final double PEAK_NMOL   = 540.0;
    private static final double NADIR_NMOL  = 100.0;
    private static final double AMPLITUDE   = (PEAK_NMOL - NADIR_NMOL) / 2.0;   // 220
    private static final double MIDPOINT    = (PEAK_NMOL + NADIR_NMOL) / 2.0;   // 320
    private static final double DAY_SECONDS = 86_400.0;
    private static final double OMEGA       = 2.0 * Math.PI / DAY_SECONDS;
    // Shift cosine so it peaks at 08:00 (28 800 s after midnight)
    private static final double PHASE_SHIFT = OMEGA * 28_800.0;

    // ── Stress event
    private static final double STRESS_CENTRE = 50_400.0;   // 14:00
    private static final double STRESS_HALF   =  1_800.0;   // ±30 min
    private static final double STRESS_HEIGHT =   220.0;    // nmol/L peak elevation

    // ── Noise & clamp
    private static final double NOISE_SD  = 18.0;
    private static final double MIN_NMOL  = 50.0;
    private static final double MAX_NMOL  = 800.0;

    // ── Phase boundary constants (seconds of day)
    private static final double MORNING_PEAK_START =  25_200.0;  // 07:00
    private static final double MORNING_PEAK_END   =  36_000.0;  // 10:00
    private static final double DECLINING_END      =  64_800.0;  // 18:00
    private static final double NADIR_START        =  68_400.0;  // 19:00
    private static final double NADIR_END          =  79_200.0;  // 22:00

    // ── State
    /** Elapsed simulated seconds since the simulation was created. */
    private double simElapsedSeconds;

    private final Random rng;

    // ── Constructor

    /**
     * Start simulation at 06:00 with a fixed random seed so every demo
     * run produces the same arc (reproducible for marking / demonstration).
     */

    public CortisolSimulator() {
        this.simElapsedSeconds = 21_600.0;  // 06:00
        this.rng = new Random(42);
    }

    // ── Public API

    /**
     * Advance the simulation by {@code tickSeconds} simulated seconds and
     * return a single-point {@link CortisolReading}.
     *
     * @param wallClockMs  real wall-clock epoch-ms for this reading
     * @param tickSeconds  simulated seconds to advance per call
     *                     (CortisolPublisher uses 300 s = 5 simulated minutes)
     */
    public CortisolReading tick(long wallClockMs, double tickSeconds) {
        simElapsedSeconds += tickSeconds;
        double secondOfDay = simElapsedSeconds % DAY_SECONDS;

        double baseline    = circadianBaseline(secondOfDay);
        double stressDelta = stressRise(secondOfDay);
        double noise       = rng.nextGaussian() * NOISE_SD;
        double value       = clamp(baseline + stressDelta + noise, MIN_NMOL, MAX_NMOL);
        String phaseLabel  = classifyPhase(secondOfDay, stressDelta);

        CortisolReading reading = new CortisolReading();
        reading.addReading(wallClockMs, value, phaseLabel);
        return reading;
    }

    /**
     * Returns the current simulated time of day in seconds since midnight.
     */
    public double currentSecondOfDay() {
        return simElapsedSeconds % DAY_SECONDS;
    }

    // ── Private helpers

    private static double circadianBaseline(double s) {
        return MIDPOINT + AMPLITUDE * Math.cos(OMEGA * s - PHASE_SHIFT);
    }

    private static double stressRise(double s) {
        double dist = Math.abs(s - STRESS_CENTRE);
        if (dist >= STRESS_HALF) return 0.0;
        return STRESS_HEIGHT * (1.0 - dist / STRESS_HALF);
    }

    private static String classifyPhase(double s, double stressDelta) {
        if (stressDelta > 80.0)                                    return "stress_rise";
        if (s >= MORNING_PEAK_START && s <= MORNING_PEAK_END)      return "morning_peak";
        if (s >= NADIR_START        && s <= NADIR_END)             return "nadir";
        if (s >= MORNING_PEAK_END   && s <= DECLINING_END)         return "declining";
        return "baseline";
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}