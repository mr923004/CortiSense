package rpm.simulation;

import java.time.Instant;
import java.util.Objects;
import java.util.Random;

public class TemperatureSimulator implements VitalSimulator {

    private final Random random;
    private final double baselineTempC;
    private final double minTempC;
    private final double maxTempC;

    private double currentTempC;

    public TemperatureSimulator() {
        this(36.8, 36.5, 37.5, new Random());
    }

    public TemperatureSimulator(double baselineTempC,
                                double minTempC,
                                double maxTempC) {
        this(baselineTempC, minTempC, maxTempC, new Random());
    }

    public TemperatureSimulator(double baselineTempC,
                                double minTempC,
                                double maxTempC,
                                Random random) {
        this.random = Objects.requireNonNull(random, "random");
        this.baselineTempC = baselineTempC;
        this.minTempC = minTempC;
        this.maxTempC = maxTempC;
        this.currentTempC = baselineTempC;
    }

    @Override
    public double nextValue(Instant time) {
        // Very small random variation each step
        double randomStep = random.nextGaussian() * 0.02;

        // Pull the value gently back toward the baseline
        double pullToBaseline = (baselineTempC - currentTempC) * 0.02;

        currentTempC = currentTempC + randomStep + pullToBaseline;

        // Fix the temperature within configured limits
        if (currentTempC < minTempC) {
            currentTempC = minTempC;
        } else if (currentTempC > maxTempC) {
            currentTempC = maxTempC;
        }

        return currentTempC;
    }
}
