package rpm.ecg;

// Generates ECG waveform samples
public interface EcgGenerator {

    // Returns a block of ECG samples based on the given settings
    double[] generateSegment(double durationSeconds,
                             double meanHeartRateBpm,
                             double samplingFrequencyHz);
}
