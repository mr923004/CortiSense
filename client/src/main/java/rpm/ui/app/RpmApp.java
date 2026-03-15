package rpm.ui.app;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.stage.Stage;
import rpm.domain.alarm.AlarmConfig;
import rpm.domain.alarm.AlarmEngine;
import rpm.domain.alarm.AlarmService;
import rpm.domain.alarm.ConsoleAlarmListener;
import rpm.domain.report.InMemoryPatientDataStore;
import rpm.domain.report.ReportGenerator;
import rpm.net.TelemetryPublisher;
import rpm.simulation.WardManager;

import java.time.Duration;

/*
- Main entry point for the RPM application.
- Responsible for wiring together simulation, alarms, reporting, telemetry, and UI.
*/
public final class RpmApp extends Application {

    private Timeline telemetryTimeline;

    @Override
    public void start(Stage stage) {
        // Basic window sizing
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        // Log telemetry configuration for debugging
        System.out.println("RPM_TELEMETRY_URL=" + System.getenv("RPM_TELEMETRY_URL"));
        System.out.println("rpm.telemetry.url=" + System.getProperty("rpm.telemetry.url"));

        // Create ward simulation with initial patients
        WardManager ward = new WardManager(8);

        // Set up alarm engine and service
        AlarmEngine alarmEngine = new AlarmEngine(AlarmConfig.defaultAdult());
        AlarmService alarmService = new AlarmService(alarmEngine);
        ward.addListener(alarmService);

        // Console output listener used for testing
        alarmService.addListener(new ConsoleAlarmListener());

        // In-memory data store for reports
        InMemoryPatientDataStore store =
                new InMemoryPatientDataStore(Duration.ofMinutes(10));
        ward.addListener(store);
        alarmService.addListener(store);

        ReportGenerator reportGenerator = new ReportGenerator();

        // User session and UI preferences
        Session session = new Session();
        UISettings settings = new UISettings();

        // Simulation clock drives time progression
        SimulationClock clock = new SimulationClock(ward);

        // Optional telemetry publishing if configured
        TelemetryPublisher telemetry =
                TelemetryPublisher.tryCreateFromSystem().orElse(null);

        if (telemetry != null) {
            System.out.println("[telemetry] enabled: " + telemetry.getUrl());

            // Send telemetry regularly (every ~200ms)
            telemetryTimeline = new Timeline(
                    new KeyFrame(javafx.util.Duration.millis(200),
                            e -> telemetry.onTick(ward, clock.getSimTime()))
            );
            telemetryTimeline.setCycleCount(Timeline.INDEFINITE);
            telemetryTimeline.play();
        } else {
            System.out.println("[telemetry] disabled (set RPM_TELEMETRY_URL or -Drpm.telemetry.url)");
        }

        // Start the simulation clock
        clock.start();

        // Build application context and router
        AppContext ctx =
                new AppContext(ward, alarmService, store, reportGenerator, session, settings, clock);

        Router router = new Router(stage, ctx);

        // Initial screen
        router.showLogin();

        stage.show();
    }

    @Override
    public void stop() {
        // Stop background telemetry when application exits
        if (telemetryTimeline != null) telemetryTimeline.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
