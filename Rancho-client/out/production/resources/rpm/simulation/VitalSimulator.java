package rpm.simulation;

import java.time.Instant;

/**
 * Interface for a simulator that produces values for a single vital sign
 * at discrete points in time.
 */

public interface VitalSimulator {

    double nextValue(Instant time);
}
