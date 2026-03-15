package rpm.ui.alerts;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import rpm.ui.bindings.VitalDisplay;

// UI component for a single patient card in dashboard
// Displays vital signs and flashes red during a medical event
public final class AlertCardView extends VBox {

    private final Label title = new Label();
    private final Label hr = new Label();
    private final Label rr = new Label();
    private final Label bp = new Label();
    private final Label temp = new Label();
    private final Button resolve = new Button("Resolve");

    private Timeline flasher;
    private boolean flashOn = false;

    public interface ResolveHandler {
        void onResolve();
    }

    public AlertCardView() {
        setSpacing(8);
        setPadding(new Insets(12));
        setStyle(baseStyle());

        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        resolve.setVisible(false);

        getChildren().addAll(title, hr, rr, bp, temp, resolve);
    }

    // Assigns action to perform when "Resolve" button clicked
    public void setResolveHandler(ResolveHandler handler) {
        resolve.setOnAction(e -> {
            if (handler != null) handler.onResolve();
        });
    }

    // Updates UI with new data from simulation
    public void setModel(AlertTileModel t) {
        title.setText(t.name + " (" + t.id.getDisplayName() + ")");
        hr.setText("HR: " + VitalDisplay.fmt1(t.hr) + " bpm");
        rr.setText("RR: " + VitalDisplay.fmt1(t.rr) + " br/min");
        bp.setText("BP: " + VitalDisplay.fmt0(t.sys) + "/" + VitalDisplay.fmt0(t.dia) + " mmHg");
        temp.setText("Temp: " + VitalDisplay.fmt1(t.temp) + " °C");

        boolean shouldFlash = t.alerting && !t.acknowledged;
        resolve.setVisible(t.alerting); // show resolve whenever alerting

        if (shouldFlash) startFlash();
        else stopFlashAndReset();
    }

    // Starts flashing animation loop
    private void startFlash() {
        if (flasher != null) return;
        flasher = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            flashOn = !flashOn;
            setStyle(flashOn ? alertStyle() : baseStyle());
        }));
        flasher.setCycleCount(Timeline.INDEFINITE);
        flasher.play();
    }

    // Stops animation loop and resets to normal style
    private void stopFlashAndReset() {
        if (flasher != null) {
            flasher.stop();
            flasher = null;
        }
        flashOn = false;
        setStyle(baseStyle());
    }

    // CSS for normal style
    private static String baseStyle() {
        return "-fx-border-color: #cccccc; -fx-border-radius: 10; -fx-background-radius: 10;";
    }

    // CSS for alert style
    private static String alertStyle() {
        return "-fx-border-color: red; -fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: #ffcccc;";
    }
}
