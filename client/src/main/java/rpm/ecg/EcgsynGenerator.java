package rpm.ecg;

import rpm.external.ecgsyn.ecgApplet;
import javax.swing.table.DefaultTableModel;

public class EcgsynGenerator implements EcgGenerator {

    // Length of the internally generated ECG buffer in seconds
    private static final double LONG_SEGMENT_SECONDS = 20.0;

    // Cached ECG samples from the last generated trace
    private double[] buffer = new double[0];
    private int bufferIndex = 0;

    // Settings used to generate the current buffer
    private double bufferHrBpm = Double.NaN;
    private int bufferSamplingHz = 0;

    @Override
    public synchronized double[] generateSegment(double durationSeconds,
                                                 double meanHeartRateBpm,
                                                 double samplingFrequencyHz) {

        int fs = (int) Math.round(samplingFrequencyHz);
        int neededSamples = (int) Math.round(durationSeconds * fs);

        // Regenerate the buffer if the heart rate changed significantly,
        // the sampling rate changed, or there are not enough samples left.
        boolean hrChangedTooMuch =
                Double.isNaN(bufferHrBpm) ||
                        Math.abs(bufferHrBpm - meanHeartRateBpm) > 5.0;

        boolean fsChanged = (bufferSamplingHz != fs);
        boolean notEnoughLeft = (bufferIndex + neededSamples > buffer.length);

        if (hrChangedTooMuch || fsChanged || notEnoughLeft) {
            buffer = generateLongTrace(LONG_SEGMENT_SECONDS, meanHeartRateBpm, fs);
            bufferIndex = 0;
            bufferHrBpm = meanHeartRateBpm;
            bufferSamplingHz = fs;
        }

        if (buffer.length == 0 || neededSamples <= 0) {
            return new double[0];
        }

        // Copy the next portion of samples from the buffer
        int available = buffer.length - bufferIndex;
        int actual = Math.min(neededSamples, available);
        double[] segment = new double[actual];
        System.arraycopy(buffer, bufferIndex, segment, 0, actual);
        bufferIndex += actual;

        return segment;
    }

    // Generates a longer continuous ECG trace using the underlying applet
    private double[] generateLongTrace(double durationSeconds,
                                       double meanHeartRateBpm,
                                       int fs) {

        // Estimate how many beats are needed to cover the duration
        int numberOfBeats = (int) Math.ceil(
                meanHeartRateBpm * durationSeconds / 60.0
        ) + 2;

        ecgApplet applet = new ecgApplet();
        applet.init();

        applet.setHeartRateMean(meanHeartRateBpm);
        applet.setSamplingFrequencyHz(fs);
        applet.setNumberOfBeats(numberOfBeats);
        applet.setNoiseAmplitude(0.02);

        boolean success = applet.ecgFunction();
        if (!success) {
            return new double[0];
        }

        DefaultTableModel model = applet.getTableValuesModel();
        int availableSamples = model.getRowCount();

        int targetSamples = (int) Math.round(durationSeconds * fs);
        int sampleCount = Math.min(targetSamples, availableSamples);

        // Extract voltage values from the applet output table
        double[] ecgSamples = new double[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            Object v = model.getValueAt(i, 1);   // column 1 contains voltage values
            ecgSamples[i] = Double.parseDouble(v.toString());
        }

        return ecgSamples;
    }
}
