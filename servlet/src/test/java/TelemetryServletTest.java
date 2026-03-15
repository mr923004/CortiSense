import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TelemetryServletTest {

    private TelemetryServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseBody;

    // Sets up a fresh servlet instance with mocked HTTP objects and an in-memory store for isolated unit testing
    @BeforeEach
    void setUp() throws Exception {
        servlet = new TelemetryServlet();

        // Force in-memory store instead of DB - this section was AI generated
        Field storeField = TelemetryServlet.class.getDeclaredField("store");
        storeField.setAccessible(true);
        storeField.set(servlet, new InMemoryTelemetryStore());

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    // Test that invalid JSON sent via POST results in a 400 Bad Request
    @Test
    void doPost_invalidJson_returns400() throws Exception {
        when(request.getReader())
                .thenReturn(new BufferedReader(new StringReader("{invalid-json")));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(responseBody.toString().contains("invalid json"));
    }

    //Test valid telemetry data sent via POST is accepted and stored successfully
    @Test
    void doPost_validTelemetry_returns200() throws Exception {
        String json =
                "{"
                        + "  \"patients\": {"
                        + "    \"bed-1\": {"
                        + "      \"ts\": [1000],"
                        + "      \"hr\": [72.0],"
                        + "      \"rr\": [16.0],"
                        + "      \"sys\": [120.0],"
                        + "      \"dia\": [80.0],"
                        + "      \"temp\": [36.8]"
                        + "    }"
                        + "  }"
                        + "}";

        when(request.getReader())
                .thenReturn(new BufferedReader(new StringReader(json)));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseBody.toString().contains("stored"));
    }

    //Test telemetry data can be retrieved via GET for an existing patient bed.
    @Test
    void doGet_existingBed_returnsTelemetry() throws Exception {
        // preload telemetry
        String json =
                "{"
                        + "  \"patients\": {"
                        + "    \"bed-2\": {"
                        + "      \"ts\": [2000],"
                        + "      \"hr\": [80.0],"
                        + "      \"rr\": [18.0],"
                        + "      \"sys\": [130.0],"
                        + "      \"dia\": [85.0],"
                        + "      \"temp\": [37.1]"
                        + "    }"
                        + "  }"
                        + "}";

        when(request.getReader())
                .thenReturn(new BufferedReader(new StringReader(json)));
        servlet.doPost(request, response);

        // reset mocks for GET
        reset(request, response);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(request.getParameter("bed")).thenReturn("bed-2");

        servlet.doGet(request, response);

        assertTrue(responseBody.toString().contains("hr"));
        assertTrue(responseBody.toString().contains("80.0"));
    }
}
