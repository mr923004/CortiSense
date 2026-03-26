package rpm.ui.dashboard;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import rpm.ui.app.AppContext;
import rpm.ui.app.Router;
import rpm.ui.layout.TopBanner;

// Visual container for dashboard screen. Connect grid of patients with
// controller. Manage background timers for data refreshing and auto-page rotation
public final class DashboardView extends BorderPane {

    private final AppContext ctx;

    private final DashboardController controller;
    private final PatientGridView grid;

    private final Timeline uiTick;
    private final Timeline rotateTick;

    public DashboardView(AppContext ctx, Router router, TopBanner banner) {
        this.ctx = ctx;

        getStyleClass().add("app-bg");

        this.grid = new PatientGridView();
        this.controller = new DashboardController(ctx, router, grid, banner);

        setCenter(grid);

        controller.refreshPatients();
        controller.renderPage();

        // Setup primary update loop. Run every second to fetch new simulation data and display
        uiTick = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            controller.refreshPatients();
            long nowMs = ctx.clock.getSimTime().toEpochMilli();
            controller.tickUi(nowMs);
            controller.renderPage();
        }));
        uiTick.setCycleCount(Timeline.INDEFINITE);
        uiTick.play();

        // Setup page rotation
        rotateTick = new Timeline();
        rotateTick.setCycleCount(Timeline.INDEFINITE);
        configureRotation();

        // If no longer on dashboard, stop timers to save resources
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                uiTick.stop();
                rotateTick.stop();
            } else {
                // If back on dashboard check rotation settings
                configureRotation();
            }
        });
    }

    // Configure automatic page turner based on user settings
    private void configureRotation() {
        rotateTick.stop();
        rotateTick.getKeyFrames().clear();

        if (!ctx.settings.isRotationEnabled()) return;

        int secs = ctx.settings.getRotationSeconds();
        if (secs <= 0) secs = 10;

        rotateTick.getKeyFrames().add(
                new KeyFrame(Duration.seconds(secs), e -> grid.fireNextPage())
        );
        rotateTick.play();
    }

}
