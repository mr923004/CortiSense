package rpm.simulation;

import java.util.Random;

public final class PatientProfile {
    private final double hrBaseline;
    private final double rrBaseline;
    private final double bpSysBaseline;
    private final double bpDiaBaseline;
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

    public static PatientProfile generateNormal(PatientScenario scenario, Random random) {
        double hr = clamp(scenario.getHrBaseline() + random.nextGaussian() * 3.0,
                scenario.getHrMin() + 1.0, scenario.getHrMax() - 1.0);

        double rr = clamp(scenario.getRrBaseline() + random.nextGaussian() * 0.5,
                scenario.getRrMin() + 0.2, scenario.getRrMax() - 0.2);

        double sys = clamp(scenario.getBpBaselineSystolic() + random.nextGaussian() * 4.0,
                scenario.getBpMinSystolic() + 2.0, scenario.getBpMaxSystolic() - 2.0);

        double dia = clamp(scenario.getBpBaselineDiastolic() + random.nextGaussian() * 2.0,
                55.0, 100.0);

        double temp = clamp(scenario.getTempBaseline() + random.nextGaussian() * 0.15,
                scenario.getTempMin() + 0.02, scenario.getTempMax() - 0.02);

        return new PatientProfile(hr, rr, sys, dia, temp);
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
