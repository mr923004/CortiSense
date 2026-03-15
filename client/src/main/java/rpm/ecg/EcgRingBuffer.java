package rpm.ecg;

// Fixed-size ring buffer for ECG samples.
// Keeps only the most recent values up to the configured capacity.
public class EcgRingBuffer {

    private final double[] data;

    // Number of valid samples currently stored
    private int size = 0;

    // Index where the next sample will be written
    private int writePos = 0;

    public EcgRingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.data = new double[capacity];
    }

    // Adds new samples, overwriting the oldest data if the buffer is full
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

    // Returns a copy of the most recent samples currently in the buffer
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

    // Clears all stored samples
    public synchronized void clear() {
        size = 0;
        writePos = 0;
    }

    // Returns the maximum number of samples the buffer can hold
    public int getCapacity() {
        return data.length;
    }
}
