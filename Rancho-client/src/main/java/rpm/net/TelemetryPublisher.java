/*
 * Reference [3] taken from https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html
 *
 * This class implements periodic telemetry publishing using the Java
 * standard HTTP client (java.net.http.HttpClient). It packages patient
 * vital signs and ECG samples into a JSON payload and sends them to a
 * configured remote endpoint.
 *
 * Networking reference:
 * Oracle Java Documentation.
 * "java.net.http.HttpClient API Specification."
 * Used for building and sending asynchronous HTTP POST requests.
 *
 * Data handling and buffering logic is implemented locally for the RPM
 * simulation environment.
 */

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
import java.util.Optional;

public final class TelemetryPublisher {

    private static final String ENV_URL = "RPM_TELEMETRY_URL";
    private static final String PROP_URL = "rpm.telemetry.url";

    private static final String ENV_PERIOD_MS = "RPM_TELEMETRY_PERIOD_MS";
    private static final String PROP_PERIOD_MS = "rpm.telemetry.period.ms";

    private final HttpClient client = HttpClient.newHttpClient();
    private final String url;
    private final long periodMs;

    private final int rawFsHz = 250;
    private final int downsampleFactor = 2;
    private final int targetFsHz = rawFsHz / downsampleFactor;

    // Send at most 1 second of ECG per upload
    private final int chunkSamples = targetFsHz;

    // Keep at most ~10 seconds of ECG buffered per patient
    private final int ecgBufferCapacitySamples = targetFsHz * 10;

    private final Map<PatientId, EcgBuf> ecg = new HashMap<>();
    private long lastSendMs = -1;

    public static Optional<TelemetryPublisher> tryCreateFromSystem() {
        String u = resolveUrlFromSystem();
        if (u == null) return Optional.empty();
        try {
            return Optional.of(new TelemetryPublisher(u, resolvePeriodMsFromSystem()));
        } catch (IllegalArgumentException ex) {
            System.out.println("[telemetry] " + ex.getMessage());
            return Optional.empty();
        }
    }

    private static String resolveUrlFromSystem() {
        String env = System.getenv(ENV_URL);
        if (env != null && !env.isBlank()) return env.trim();

        String prop = System.getProperty(PROP_URL);
        if (prop != null && !prop.isBlank()) return prop.trim();

        return null;
    }

    private static long resolvePeriodMsFromSystem() {
        String env = System.getenv(ENV_PERIOD_MS);
        if (env != null && !env.isBlank()) {
            Long parsed = parsePositiveLong(env.trim());
            if (parsed != null) return parsed;
        }

        String prop = System.getProperty(PROP_PERIOD_MS);
        if (prop != null && !prop.isBlank()) {
            Long parsed = parsePositiveLong(prop.trim());
            if (parsed != null) return parsed;
        }

        return 30_000L; // default: 30s
    }

    private static Long parsePositiveLong(String s) {
        try {
            long v = Long.parseLong(s);
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public TelemetryPublisher(String url) {
        this(url, 30_000L);
    }

    public TelemetryPublisher(String url, long periodMs) {
        this.url = validateUrl(url);
        this.periodMs = Math.max(1, periodMs);
    }

    public String getUrl() {
        return url;
    }

    public long getPeriodMs() {
        return periodMs;
    }

    private static String validateUrl(String url) {
        String u = url == null ? "" : url.trim();
        if (u.isEmpty()) throw new IllegalArgumentException("Telemetry URL is blank");
        if (!u.contains("://")) throw new IllegalArgumentException("Telemetry URL must be absolute (include https://...)");
        return u;
    }

    public void onTick(WardManager ward, Instant simTime) {
        if (ward == null) return;

        long nowMs = (simTime != null) ? simTime.toEpochMilli() : System.currentTimeMillis();

        List<PatientId> ids = ward.getPatientIds();
        for (PatientId id : ids) {
            double[] seg = ward.getPatientLastEcgSegment(id);
            if (seg == null || seg.length == 0) continue;

            EcgBuf b = ecg.computeIfAbsent(id, k -> new EcgBuf(ecgBufferCapacitySamples));
            b.append(seg, downsampleFactor, nowMs);
        }

        if (lastSendMs >= 0 && (nowMs - lastSendMs) < periodMs) return;
        lastSendMs = nowMs;

        String payload = buildPayload(ward, nowMs);
        if (payload == null) return;

        postAsync(payload);
    }

    private String buildPayload(WardManager ward, long nowMs) {
        List<PatientId> ids = ward.getPatientIds();
        if (ids == null || ids.isEmpty()) return null;

        StringBuilder sb = new StringBuilder(64 * Math.max(1, ward.getPatientCount()));
        sb.append("{\"patients\":{");

        boolean first = true;

        for (PatientId id : ids) {
            VitalSnapshot snap = ward.getPatientLatestSnapshot(id);
            if (snap == null) continue;

            Double hr = val(snap, VitalType.HEART_RATE);
            Double rr = val(snap, VitalType.RESP_RATE);
            Double sys = val(snap, VitalType.BP_SYSTOLIC);
            Double dia = val(snap, VitalType.BP_DIASTOLIC);
            Double temp = val(snap, VitalType.TEMPERATURE);

            EcgBuf b = ecg.get(id);
            EcgBuf.EcgChunk chunk = (b != null) ? b.takeLatestChunk(chunkSamples, targetFsHz) : null;

            if (!first) sb.append(",");
            first = false;

            String bed = id.getDisplayName();

            sb.append("\"").append(escape(bed)).append("\":{");
            sb.append("\"ts\":[").append(nowMs).append("],");
            sb.append("\"hr\":[").append(numOrNull(hr)).append("],");
            sb.append("\"rr\":[").append(numOrNull(rr)).append("],");
            sb.append("\"sys\":[").append(numOrNull(sys)).append("],");
            sb.append("\"dia\":[").append(numOrNull(dia)).append("],");
            sb.append("\"temp\":[").append(numOrNull(temp)).append("]");

            if (chunk != null) {
                sb.append(",\"ecgTsStart\":").append(chunk.startMs);
                sb.append(",\"ecgFs\":").append(targetFsHz);
                sb.append(",\"ecg\":[");
                for (int i = 0; i < chunk.samples.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append(chunk.samples[i]);
                }
                sb.append("]");
            }

            sb.append("}");
        }

        sb.append("}}");

        return first ? null : sb.toString();
    }

    private void postAsync(String json) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(ex -> {
                        System.out.println("[telemetry] POST error: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception ex) {
            System.out.println("[telemetry] POST build error: " + ex.getMessage());
        }
    }

    private static Double val(VitalSnapshot snap, VitalType type) {
        if (snap == null || snap.getValues() == null) return null;
        Double v = snap.getValues().get(type);
        if (v == null || v.isNaN() || v.isInfinite()) return null;
        return v;
    }

    private static String numOrNull(Double v) {
        return (v == null) ? "null" : Double.toString(v);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class EcgBuf {
        private final double[] ring;
        private int size = 0;
        private int head = 0;
        private long lastEndMs = -1;

        EcgBuf(int capacity) {
            this.ring = new double[Math.max(32, capacity)];
        }

        void append(double[] seg, int factor, long endMs) {
            if (seg == null || seg.length == 0) return;
            lastEndMs = endMs;

            for (int i = 0; i < seg.length; i += Math.max(1, factor)) {
                double v = seg[i];
                if (Double.isNaN(v) || Double.isInfinite(v)) continue;

                if (size < ring.length) {
                    ring[(head + size) % ring.length] = v;
                    size++;
                } else {
                    // overwrite oldest
                    ring[head] = v;
                    head = (head + 1) % ring.length;
                }
            }
        }

        EcgChunk takeLatestChunk(int n, int fsHz) {
            if (size < n || lastEndMs < 0) return null;

            double[] out = new double[n];

            int startIndex = (head + (size - n)) % ring.length;
            for (int i = 0; i < n; i++) {
                out[i] = ring[(startIndex + i) % ring.length];
            }

            long durMs = (long) (1000.0 * n / fsHz);
            long startMs = lastEndMs - durMs;

            return new EcgChunk(startMs, out);
        }

        static final class EcgChunk {
            final long startMs;
            final double[] samples;

            EcgChunk(long startMs, double[] samples) {
                this.startMs = startMs;
                this.samples = samples;
            }
        }
    }
}
/* end of Reference [3] */