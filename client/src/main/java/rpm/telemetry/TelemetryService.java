package rpm.telemetry;

import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import com.fasterxml.jackson.databind.ObjectMapper;

//This class sends telemetry data every 10 seconds, (POSTS to the servlet)

public class TelemetryService {

    private final Telemetrable source;

    // Background scheduler
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public TelemetryService(Telemetrable source) {
        this.source = source;
    }

    //Start periodic uploads
    public void start() {
        scheduler.scheduleAtFixedRate(
                this::send,
                10, 10, //adjust as needed
                TimeUnit.SECONDS
        );
    }

    //Build payload and POST to servlet
    private void send() {
        try {
            Map<String, PatientTelemetrySnapshot> data =
                    source.snapshot();

            Map<String, Object> payload = new HashMap<>();
            payload.put("timestamp", Instant.now().toString());
            payload.put("patients", data);

            String json =
                    mapper.writeValueAsString(payload);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(
                                    "https://bioeng-rancho-app.impaas.uk/telemetry"))
                            .header("Content-Type",
                                    "application/json")
                            .POST(HttpRequest.BodyPublishers
                                    .ofString(json))
                            .build();

            // Non-blocking send
            client.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
