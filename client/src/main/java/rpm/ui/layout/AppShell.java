package rpm.ui.layout;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import rpm.ui.alerts.AlertOverlayController;
import rpm.ui.alerts.AlertOverlayView;
import rpm.ui.app.AppContext;
import rpm.ui.app.Router;

public final class AppShell extends BorderPane {

    private final TopBanner banner;
    private final StackPane stack = new StackPane();
    private final BorderPane contentPane = new BorderPane();

    private final AlertOverlayView overlayView = new AlertOverlayView();
    private final AlertOverlayController overlayController;

    private final Timeline alertTick;
    private boolean alertsEnabled = true;

    public AppShell(AppContext ctx, Router router) {
        // Top banner holds navigation, user info, etc.
        this.banner = new TopBanner(ctx, router);

        getStyleClass().add("app-bg");
        stack.getStyleClass().add("app-bg");
        contentPane.getStyleClass().add("app-bg");

        // Place banner at the top of the layout
        setTop(banner);

        // Controller manages alert behaviour and visibility
        overlayController = new AlertOverlayController(ctx, overlayView);

        // Stack main content and alert overlay on top of each other
        stack.getChildren().addAll(contentPane, overlayView);
        setCenter(stack);

        // Periodically update alert overlay
        alertTick = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (!alertsEnabled) return;

            long nowMs = ctx.clock.getSimTime().toEpochMilli();
            overlayController.tick(nowMs);
        }));
        alertTick.setCycleCount(Timeline.INDEFINITE);
        alertTick.play();
    }

    // Replace the main content area with a new view
    public void setContent(Node node) {
        contentPane.setCenter(node);
    }

    public TopBanner getBanner() {
        return banner;
    }

    // Enable or disable alert processing (used on login screen, etc.)
    public void setAlertsEnabled(boolean enabled) {
        this.alertsEnabled = enabled;
    }
}
