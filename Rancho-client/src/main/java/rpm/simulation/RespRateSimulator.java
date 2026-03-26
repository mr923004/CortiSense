package rpm.simulation;

import java.time.Instant;
import java.util.Objects;
import java.util.Random;

public class RespRateSimulator implements VitalSimulator {

    private final Random random;
    private final double baselineRespPerMin;
    private final double minRespPerMin;
    private final double maxRespPerMin;

    private double currentRespPerMin;

    public RespRateSimulator() {
        this(16.0, 12.0, 20.0, new Random());
    }

    public RespRateSimulator(double baselineRespPerMin,
                             double minRespPerMin,
                             double maxRespPerMin) {
        this(baselineRespPerMin, minRespPerMin, maxRespPerMin, new Random());
    }

    public RespRateSimulator(double baselineRespPerMin,
                             double minRespPerMin,
                             double maxRespPerMin,
                             Random random) {
        this.random = Objects.requireNonNull(random, "random");
        this.baselineRespPerMin = baselineRespPerMin;
        this.minRespPerMin = minRespPerMin;
        this.maxRespPerMin = maxRespPerMin;
        this.currentRespPerMin = baselineRespPerMin;
    }

    @Override
    public double nextValue(Instant time) {
        // Small random variation each step
        double randomStep = random.nextGaussian() * 0.4;

        // Pull the value gently back toward the baseline
        double pullToBaseline = (baselineRespPerMin - currentRespPerMin) * 0.08;

        currentRespPerMin = currentRespPerMin + randomStep + pullToBaseline;

        // Fix the value within configured limits
        if (currentRespPerMin < minRespPerMin) {
            currentRespPerMin = minRespPerMin;
        } else if (currentRespPerMin > maxRespPerMin) {
            currentRespPerMin = maxRespPerMin;
        }

        return currentRespPerMin;
    }
}
