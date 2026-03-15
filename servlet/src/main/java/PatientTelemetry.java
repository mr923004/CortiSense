import java.util.ArrayList;
import java.util.List;

public class PatientTelemetry {
    private final List<Long> ts = new ArrayList<>();
    private final List<Double> hr = new ArrayList<>();
    private final List<Double> rr = new ArrayList<>();
    private final List<Double> sys = new ArrayList<>();
    private final List<Double> dia = new ArrayList<>();
    private final List<Double> temp = new ArrayList<>();
    private final List<Double> ecg = new ArrayList<>();

    private Long ecgTsStart;
    private Integer ecgFs;

    public synchronized void append(PatientTelemetry incoming) {
        if (incoming == null) return;
        ts.addAll(incoming.ts);
        hr.addAll(incoming.hr);
        rr.addAll(incoming.rr);
        sys.addAll(incoming.sys);
        dia.addAll(incoming.dia);
        temp.addAll(incoming.temp);
        ecg.addAll(incoming.ecg);
        if (incoming.ecgTsStart != null) ecgTsStart = incoming.ecgTsStart;
        if (incoming.ecgFs != null) ecgFs = incoming.ecgFs;
    }

    public List<Long> getTs() { return ts; }
    public List<Double> getHr() { return hr; }
    public List<Double> getRr() { return rr; }
    public List<Double> getSys() { return sys; }
    public List<Double> getDia() { return dia; }
    public List<Double> getTemp() { return temp; }
    public List<Double> getEcg() { return ecg; }

    public Long getEcgTsStart() { return ecgTsStart; }
    public Integer getEcgFs() { return ecgFs; }
}

