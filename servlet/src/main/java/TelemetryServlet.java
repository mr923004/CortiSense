import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = "/telemetry", loadOnStartup = 1)
public class TelemetryServlet extends HttpServlet {
    private final ObjectMapper mapper = new ObjectMapper();
    private AbstractTelemetryStore store;

    @Override
    public void init() {

        store = TelemetryStores.create();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        if (body == null || body.isBlank()) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"empty body\"}");
            return;
        }

        Map<String, Object> payload;
        try {
            payload = mapper.readValue(body, Map.class);
        } catch (Exception e) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"invalid json\"}");
            return;
        }

        Object p = payload.get("patients");
        if (!(p instanceof Map)) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"missing patients\"}");
            return;
        }

        Map<String, Object> patients = (Map<String, Object>) p;

        try {
            for (Map.Entry<String, Object> entry : patients.entrySet()) {
                String bedId = entry.getKey();
                Object vitalsObj = entry.getValue();
                PatientTelemetry pt = mapper.convertValue(vitalsObj, PatientTelemetry.class);
                store.store(bedId, pt);
            }
        } catch (Exception e) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"bad patient payload\"}");
            return;
        }

        json(resp, HttpServletResponse.SC_OK, "{\"status\":\"stored\"}");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String bed = req.getParameter("bed");
        if (bed != null && !bed.isBlank()) {
            PatientTelemetry pt = store.get(bed);
            if (pt == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"unknown bed\"}");
                return;
            }
            mapper.writeValue(resp.getWriter(), pt);
            return;
        }

        Map<String, PatientTelemetry> sorted = new TreeMap<>(store.getAll());
        mapper.writeValue(resp.getWriter(), sorted);
    }

    private static void json(HttpServletResponse resp, int status, String body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(body);
    }




}
