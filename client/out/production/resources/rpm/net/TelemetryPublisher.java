package rpm.net;

import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.domain.VitalType;
import rpm.simulation.WardManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelemetryPublisher {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String url;
    private final int rawFsHz = 250;
    private final int targetFsHz = 125;
    private final int downsampleFactor = 2;
    private final int chunkSamples = 125;
    private final Map<PatientId, EcgBuf> ecg = new HashMap<>();
    private long lastSendMs = -1;

    public TelemetryPublisher() {
        this(System.getProperty("rpm.telemetry.url", "http://localhost:8080/servlet/telemetry"));
    }

    public TelemetryPublisher(String url) {
        this.url = url;
    }

    public void onTick(WardManager ward, Instant simTime) {
        long nowMs = simTime.toEpochMilli();
        for (PatientId id : ward.getPatientIds()) {
            double[] seg = ward.getPatientLastEcgSegment(id);
            if (seg == null || seg.length == 0) continue;
            EcgBuf b = ecg.computeIfAbsent(id, k -> new EcgBuf());
            b.append(seg, downsampleFactor, nowMs);
        }

        if (lastSendMs >= 0 && (nowMs - lastSendMs) < 1000) return;
        lastSendMs = nowMs;

        String json = buildPayload(ward, nowMs);
        postAsync(json);
    }

    private String buildPayload(WardManager ward, long nowMs) {
        StringBuilder sb = new StringBuilder(64 * ward.getPatientCount());
        sb.append("{\"patients\":{");

        List<PatientId> ids = ward.getPatientIds();
        for (int i = 0; i < ids.size(); i++) {
            PatientId id = ids.get(i);
            String bed = id.getDisplayName();
            VitalSnapshot snap = ward.getPatientLatestSnapshot(id);

            Double hr = val(snap, VitalType.HEART_RATE);
            Double rr = val(snap, VitalType.RESP_RATE);
            Double sys = val(snap, VitalType.BP_SYSTOLIC);
            Double dia = val(snap, VitalType.BP_DIASTOLIC);
            Double temp = val(snap, VitalType.TEMPERATURE);

            EcgBuf b = ecg.get(id);
            double[] chunk = (b != null) ? b.takeChunk(chunkSamples) : null;
            Long chunkStart = (b != null && chunk != null) ? b.lastChunkStartMs : null;

            if (i > 0) sb.append(",");
            sb.append("\"").append(bed).append("\":{");

            sb.append("\"ts\":[").append(nowMs).append("],");
            sb.append("\"hr\":[").append(numOrNull(hr)).append("],");
            sb.append("\"rr\":[").append(numOrNull(rr)).append("],");
            sb.append("\"sys\":[").append(numOrNull(sys)).append("],");
            sb.append("\"dia\":[").append(numOrNull(dia)).append("],");
            sb.append("\"temp\":[").append(numOrNull(temp)).append("]");

            if (chunk != null && chunk.length > 0 && chunkStart != null) {
                sb.append(",\"ecgTsStart\":").append(chunkStart);
                sb.append(",\"ecgFs\":").append(targetFsHz);
                sb.append(",\"ecg\":[");
                for (int k = 0; k < chunk.length; k++) {
                    if (k > 0) sb.append(",");
                    sb.append(chunk[k]);
                }
                sb.append("]");
            }

            sb.append("}");
        }

        sb.append("}}");
        return sb.toString();
    }

    private void postAsync(String json) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(req, HttpResponse.BodyHandlers.discarding());
    }

    private static Double val(VitalSnapshot snap, VitalType type) {
        if (snap == null || snap.getValues() == null) return null;
        Double v = snap.getValues().get(type);
        if (v == null || v.isNaN()) return null;
        return v;
    }

    private static String numOrNull(Double v) {
        return v == null ? "null" : Double.toString(v);
    }

    private static final class EcgBuf {
        private double[] buf = new double[512];
        private int size = 0;
        private long startMs = -1;
        long lastChunkStartMs;

        void append(double[] seg, int factor, long nowMs) {
            if (seg == null || seg.length == 0) return;
            if (startMs < 0) startMs = nowMs;

            for (int i = 0; i < seg.length; i += factor) {
                ensure(size + 1);
                buf[size++] = seg[i];
            }
        }

        double[] takeChunk(int n) {
            if (size < n) return null;
            double[] out = new double[n];
            System.arraycopy(buf, 0, out, 0, n);

            int remain = size - n;
            if (remain > 0) {
                System.arraycopy(buf, n, buf, 0, remain);
            }
            size = remain;

            lastChunkStartMs = startMs;
            startMs = -1;

            return out;
        }

        private void ensure(int need) {
            if (need <= buf.length) return;
            int cap = buf.length;
            while (cap < need) cap *= 2;
            double[] nbuf = new double[cap];
            System.arraycopy(buf, 0, nbuf, 0, size);
            buf = nbuf;
        }
    }
}
