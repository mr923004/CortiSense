package rpm.ecg;

/**
 * Interface for an ECG waveform generator.
 */
public interface EcgGenerator {

    /**
     * Generates a segment of ECG samples.
     *
     * @param durationSeconds      length of the segment in seconds
     * @param meanHeartRateBpm     mean heart rate for this segment (beats per minute)
     * @param samplingFrequencyHz  ECG sampling frequency (samples per second)
     * @return array of ECG voltages in millivolts
     */
    double[] generateSegment(double durationSeconds,
                             double meanHeartRateBpm,
                             double samplingFrequencyHz);
}
