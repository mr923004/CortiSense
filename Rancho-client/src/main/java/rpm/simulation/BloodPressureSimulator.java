package rpm.simulation;

import java.time.Instant;
import java.util.Objects;
import java.util.Random;

/*
 * Simulates arterial blood pressure.
 * Internally tracks both systolic and diastolic values.
 */
public class BloodPressureSimulator implements VitalSimulator {

    private final Random random;
    private final double baselineSystolic;
    private final double baselineDiastolic;
    private final double minSystolic;
    private final double maxSystolic;

    private double currentSystolic;
    private double currentDiastolic;

    public BloodPressureSimulator() {
        this(120.0, 80.0, 90.0, 140.0, new Random());
    }

    public BloodPressureSimulator(double baselineSystolic,
                                  double baselineDiastolic,
                                  double minSystolic,
                                  double maxSystolic) {
        this(baselineSystolic, baselineDiastolic, minSystolic, maxSystolic, new Random());
    }

    public BloodPressureSimulator(double baselineSystolic,
                                  double baselineDiastolic,
                                  double minSystolic,
                                  double maxSystolic,
                                  Random random) {
        this.random = Objects.requireNonNull(random, "random");
        this.baselineSystolic = baselineSystolic;
        this.baselineDiastolic = baselineDiastolic;
        this.minSystolic = minSystolic;
        this.maxSystolic = maxSystolic;
        this.currentSystolic = baselineSystolic;
        this.currentDiastolic = baselineDiastolic;
    }

    @Override
    public double nextValue(Instant time) {
        // Advance the simulated blood pressure value

        double randomStep = random.nextGaussian() * 2.0;

        // Pull the systolic value gently back toward the baseline
        double pullToBaseline = (baselineSystolic - currentSystolic) * 0.05;

        currentSystolic = currentSystolic + randomStep + pullToBaseline;

        // Clamp systolic pressure to configured limits
        if (currentSystolic < minSystolic) {
            currentSystolic = minSystolic;
        } else if (currentSystolic > maxSystolic) {
            currentSystolic = maxSystolic;
        }

        // Estimate diastolic pressure based on systolic movement
        double targetDiastolic =
                baselineDiastolic + (currentSystolic - baselineSystolic) * 0.5;

        // Add noise to avoid a perfectly fixed relationship
        double diastolicNoise = random.nextGaussian() * 1.5;
        currentDiastolic = targetDiastolic + diastolicNoise;

        return currentSystolic;
    }

    public double getCurrentSystolic() {
        return currentSystolic;
    }

    public double getCurrentDiastolic() {
        return currentDiastolic;
    }
}
