package rpm.ecg;

/**
 * Fixed-size ring buffer for ECG samples.
 * Keeps only the most recent N samples, where N is the capacity.
 */
public class EcgRingBuffer {

    private final double[] data;
    private int size = 0;      // how many valid samples we currently have (0..data.length)
    private int writePos = 0;  // index where the next sample will be written

    public EcgRingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.data = new double[capacity];
    }

    /**
     * Appends an array of samples, overwriting the oldest data if necessary.
     */
    public synchronized void append(double[] samples) {
        if (samples == null || samples.length == 0) {
            return;
        }
        for (double v : samples) {
            data[writePos] = v;
            writePos = (writePos + 1) % data.length;
            if (size < data.length) {
                size++;
            }
        }
    }

    /**
     * Returns a copy of the latest window of samples.
     * Length is between 0 and capacity.
     */
    public synchronized double[] getLatestWindow() {
        double[] result = new double[size];
        if (size == 0) {
            return result;
        }

        int start = (writePos - size + data.length) % data.length;
        for (int i = 0; i < size; i++) {
            int index = (start + i) % data.length;
            result[i] = data[index];
        }
        return result;
    }

    public synchronized void clear() {
        size = 0;
        writePos = 0;
    }

    public int getCapacity() {
        return data.length;
    }
}
