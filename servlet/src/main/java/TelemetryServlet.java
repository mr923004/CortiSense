import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = "/telemetry", loadOnStartup = 1)
public class TelemetryServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TelemetryServlet.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();

    private InMemoryTelemetryStore store;
    private CortisolPublisher publisher;

    @Override
    public void init() {
        store = new InMemoryTelemetryStore();

        // Seed 2 hours of history directly into the store.
        // No network calls
        CortisolSimulator seeder = new CortisolSimulator();
        int count = 120;
        long nowMs   = System.currentTimeMillis();
        long startMs = nowMs - (long) count * 60_000L;
        for (int i = 0; i < count; i++) {
            long ts = startMs + (long) i * 60_000L;
            store.store("user-1", seeder.tick(ts, 60.0));
        }
        log.info("CortiSense: seeded " + count + " readings. Store size: " + store.get("user-1").size());

        publisher = new CortisolPublisher(store);
        publisher.start();
        log.info("CortiSense: publisher started.");
    }

    @Override
    public void destroy() {
        if (publisher != null) {
            publisher.stop();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        corsHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String subject = req.getParameter("subject");
        if (subject != null && !subject.isBlank()) {
            CortisolReading reading = store.get(subject);
            if (reading == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"unknown subject\"}");
                return;
            }
            mapper.writeValue(resp.getWriter(), reading);
            return;
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        for (Map.Entry<String, CortisolReading> e : store.getAll().entrySet()) {
            CortisolReading r = e.getValue();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("count",  r.size());
            entry.put("latest", r.latest());
            entry.put("phase",  r.getPhase());
            summary.put(e.getKey(), entry);
        }
        mapper.writeValue(resp.getWriter(), summary);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        corsHeaders(resp);

        String body = req.getReader().lines().collect(Collectors.joining());
        if (body == null || body.isBlank()) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"empty body\"}");
            return;
        }

        Map<String, Object> payload;
        try {
            //noinspection unchecked
            payload = mapper.readValue(body, Map.class);
        } catch (Exception e) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"invalid json\"}");
            return;
        }

        String subject  = stringField(payload, "subject");
        Long   ts       = longField(payload,   "ts");
        Double cortisol = doubleField(payload, "cortisol");
        String phase    = stringField(payload, "phase");

        if (subject == null || ts == null || cortisol == null || !Double.isFinite(cortisol)) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"missing or invalid fields\"}");
            return;
        }

        CortisolReading reading = new CortisolReading();
        reading.addReading(ts, cortisol, phase != null ? phase : "baseline");
        store.store(subject, reading);
        json(resp, HttpServletResponse.SC_OK, "{\"status\":\"stored\"}");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        corsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private static void corsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void json(HttpServletResponse resp, int status, String body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(body);
    }

    private static String stringField(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof String ? (String) v : null;
    }

    private static Long longField(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number ? ((Number) v).longValue() : null;
    }

    private static Double doubleField(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number ? ((Number) v).doubleValue() : null;
    }
}
