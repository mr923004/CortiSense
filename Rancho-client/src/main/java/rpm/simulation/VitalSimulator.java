package rpm.simulation;

import java.time.Instant;

/*
- Interface for anything that generates values for a single vital sign.
- Implementations advance the simulation one step each time nextValue is called.
*/
public interface VitalSimulator {

    double nextValue(Instant time);
}
