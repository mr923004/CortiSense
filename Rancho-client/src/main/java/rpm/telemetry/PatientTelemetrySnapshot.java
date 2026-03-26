package rpm.telemetry;

import java.util.ArrayList;
import java.util.List;

//This class holds the telemetry data for 1 patient over the set time interval
//We do not want to send data for every simulation tick, so this will allow to send in batches

public class PatientTelemetrySnapshot {

    private final VitalSeries hr   = new VitalSeries();
    private final VitalSeries rr   = new VitalSeries();
    private final VitalSeries sys  = new VitalSeries();
    private final VitalSeries dia  = new VitalSeries();
    private final VitalSeries temp = new VitalSeries();

    private final List<Double> ecg = new ArrayList<>();


    public VitalSeries hr()   { return hr; }
    public VitalSeries rr()   { return rr; }
    public VitalSeries sys()  { return sys; }
    public VitalSeries dia()  { return dia; }
    public VitalSeries temp() { return temp; }
    public List<Double> ecg() { return ecg; }


    public List<Double> getHr() {
        return hr.getValues();
    }

    public List<Double> getRr() {
        return rr.getValues();
    }

    public List<Double> getSys() {
        return sys.getValues();
    }

    public List<Double> getDia() {
        return dia.getValues();
    }

    public List<Double> getTemp() {
        return temp.getValues();
    }

    public List<Double> getEcg() {
        return ecg;
    }

    //Clear after successful upload
    public void clear() {
        hr.clear();
        rr.clear();
        sys.clear();
        dia.clear();
        temp.clear();
        ecg.clear();
    }
}
