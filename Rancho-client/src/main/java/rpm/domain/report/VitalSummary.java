package rpm.domain.report;

public final class VitalSummary {

    // Number of samples included in the summary
    private final int n;

    // Minimum recorded value
    private final double min;

    // Maximum recorded value
    private final double max;

    // Average value across all samples
    private final double mean;

    public VitalSummary(int n, double min, double max, double mean) {
        this.n = n;
        this.min = min;
        this.max = max;
        this.mean = mean;
    }

    public int getN() {
        return n;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getMean() {
        return mean;
    }
}
