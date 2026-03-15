package rpm.simulation;

import java.util.Random;

/*
 - Represents a baseline physiological profile for a simulated patient.

 - Stores normal baseline values for core vital signs such as heart rate,
 respiratory rate, blood pressure, and temperature. Profiles are used
 to initialize patient simulators and generate realistic starting values
 within medically plausible ranges.
 */
public final class PatientProfile {

    // Baseline heart rate (beats per minute)
    private final double hrBaseline;

    // Baseline respiratory rate (breaths per minute)
    private final double rrBaseline;

    // Baseline systolic blood pressure (mmHg)
    private final double bpSysBaseline;

    // Baseline diastolic blood pressure (mmHg)
    private final double bpDiaBaseline;

    // Baseline body temperature (°C)
    private final double tempBaseline;

    public PatientProfile(double hrBaseline,
                          double rrBaseline,
                          double bpSysBaseline,
                          double bpDiaBaseline,
                          double tempBaseline) {
        this.hrBaseline = hrBaseline;
        this.rrBaseline = rrBaseline;
        this.bpSysBaseline = bpSysBaseline;
        this.bpDiaBaseline = bpDiaBaseline;
        this.tempBaseline = tempBaseline;
    }

    public double getHrBaseline() { return hrBaseline; }
    public double getRrBaseline() { return rrBaseline; }
    public double getBpSysBaseline() { return bpSysBaseline; }
    public double getBpDiaBaseline() { return bpDiaBaseline; }
    public double getTempBaseline() { return tempBaseline; }

    /*
     - Generates a randomized but clinically realistic patient profile
     - based on a predefined scenario and Gaussian noise.

     - Values are clamped to ensure they remain within safe physiological
     - limits defined by the scenario.
     */
    public static PatientProfile generateNormal(PatientScenario scenario, Random random) {

        double hr = clamp(
                scenario.getHrBaseline() + random.nextGaussian() * 3.0,
                scenario.getHrMin() + 1.0,
                scenario.getHrMax() - 1.0
        );

        double rr = clamp(
                scenario.getRrBaseline() + random.nextGaussian() * 0.5,
                scenario.getRrMin() + 0.2,
                scenario.getRrMax() - 0.2
        );

        double sys = clamp(
                scenario.getBpBaselineSystolic() + random.nextGaussian() * 4.0,
                scenario.getBpMinSystolic() + 2.0,
                scenario.getBpMaxSystolic() - 2.0
        );

        double dia = clamp(
                scenario.getBpBaselineDiastolic() + random.nextGaussian() * 2.0,
                55.0,
                100.0
        );

        double temp = clamp(
                scenario.getTempBaseline() + random.nextGaussian() * 0.15,
                scenario.getTempMin() + 0.02,
                scenario.getTempMax() - 0.02
        );

        return new PatientProfile(hr, rr, sys, dia, temp);
    }

    /*
     * Fixes a value between a minimum and maximum bound.
     */
    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
