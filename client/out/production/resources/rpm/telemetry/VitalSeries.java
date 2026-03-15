package rpm.telemetry;

import java.util.ArrayList;
import java.util.List;

//This class is a running list of values for a single vital sign

public class VitalSeries {

    //Internal mutable list
    private final List<Double> values = new ArrayList<>();

    //Add a new value
    public void add(double v) {
        values.add(v);
    }

    //Return an immutable copy
    public List<Double> getValues() {
        return List.copyOf(values);
    }

    //Clear after successful upload
    public void clear() {
        values.clear();
    }
}
