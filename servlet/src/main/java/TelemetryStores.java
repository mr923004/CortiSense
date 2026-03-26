
public final class TelemetryStores {

    private TelemetryStores() {}

    public static AbstractTelemetryStore create() {
        return new InMemoryTelemetryStore();
    }
}