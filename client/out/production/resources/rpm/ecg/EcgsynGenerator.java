package rpm.ecg;

import rpm.external.ecgsyn.ecgApplet;
import javax.swing.table.DefaultTableModel;

public class EcgsynGenerator implements EcgGenerator {

    private static final double LONG_SEGMENT_SECONDS = 20.0;

    private double[] buffer = new double[0];
    private int bufferIndex = 0;
    private double bufferHrBpm = Double.NaN;
    private int bufferSamplingHz = 0;

    @Override
    public synchronized double[] generateSegment(double durationSeconds,
                                                 double meanHeartRateBpm,
                                                 double samplingFrequencyHz) {

        int fs = (int) Math.round(samplingFrequencyHz);
        int neededSamples = (int) Math.round(durationSeconds * fs);

        boolean hrChangedTooMuch =
                Double.isNaN(bufferHrBpm) ||
                        Math.abs(bufferHrBpm - meanHeartRateBpm) > 5.0; // bpm threshold

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

        int available = buffer.length - bufferIndex;
        int actual = Math.min(neededSamples, available);
        double[] segment = new double[actual];
        System.arraycopy(buffer, bufferIndex, segment, 0, actual);
        bufferIndex += actual;

        return segment;
    }

    /** Generate a continuous ECG trace using the underlying applet. */
    private double[] generateLongTrace(double durationSeconds,
                                       double meanHeartRateBpm,
                                       int fs) {

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

        double[] ecgSamples = new double[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            Object v = model.getValueAt(i, 1); // column 1 = voltage
            ecgSamples[i] = Double.parseDouble(v.toString());
        }
        return ecgSamples;
    }
}
