package rpm.ui.dashboard;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

// UI component for single patient card on main dashboard
public final class PatientCardView extends VBox {

    private static final PseudoClass PC_ALERT = PseudoClass.getPseudoClass("alert");
    private static final PseudoClass PC_FLASH = PseudoClass.getPseudoClass("flash");

    private final Label bedLabel = new Label();
    private final Label nameLabel = new Label();

    private final Label pill = new Label();

    private final Label hrValue = new Label();
    private final Label rrValue = new Label();
    private final Label bpValue = new Label();
    private final Label tempValue = new Label();

    private final Button resolveBtn = new Button("Resolve");

    private Runnable onResolve = () -> {};

    private Timeline flasher;
    private boolean flashOn = false;

    public PatientCardView(PatientTileModel tile) {
        getStyleClass().add("patient-card");
        setPadding(new Insets(18));
        setSpacing(14);

        buildHeader();
        buildVitals();

        setTile(tile);
    }

    // Register action for resolve button
    public void setOnResolve(Runnable r) {
        onResolve = (r == null) ? () -> {} : r;
    }

    // Update card UI with new data from simulation
    public void setTile(PatientTileModel t) {
        String bed = (t.id == null) ? "" : t.id.getDisplayName();
        bedLabel.setText(bed);
        nameLabel.setText(safe(t.displayName, "Patient"));

        hrValue.setText(fmt(t.hr, "bpm", 1));
        rrValue.setText(fmt(t.rr, "br/min", 1));
        bpValue.setText(fmtBp(t.sys, t.dia));
        tempValue.setText(fmt(t.temp, "°C", 1));

        boolean alerting = t.alerting;
        pseudoClassStateChanged(PC_ALERT, alerting);

        boolean showResolve = t.showResolve;
        resolveBtn.setVisible(showResolve);
        resolveBtn.setManaged(showResolve);

        pill.getStyleClass().removeAll("pill-ok", "pill-alert");
        pill.getStyleClass().add(alerting ? "pill-alert" : "pill-ok");
        pill.setText(alerting ? "ALERT" : "Stable");

        if (alerting) startFlash();
        else stopFlash();
    }

    // Construct top section of card (bed number, name, status, and resolve button)
    private void buildHeader() {
        bedLabel.getStyleClass().add("patient-bed");
        nameLabel.getStyleClass().add("patient-name");

        VBox left = new VBox(2, bedLabel, nameLabel);

        pill.getStyleClass().add("pill");
        resolveBtn.getStyleClass().add("resolve-btn");
        resolveBtn.setOnAction(e -> onResolve.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox right = new HBox(10, pill, resolveBtn);
        right.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(12, left, spacer, right);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("patient-header");

        getChildren().add(header);
    }

    // Construct 2x2 grid for vital signs
    private void buildVitals() {
        GridPane g = new GridPane();
        g.getStyleClass().add("vitals-grid");
        g.setHgap(14);
        g.setVgap(12);

        g.add(vitalBox("HR", hrValue), 0, 0);
        g.add(vitalBox("RR", rrValue), 1, 0);
        g.add(vitalBox("BP", bpValue), 0, 1);
        g.add(vitalBox("Temp", tempValue), 1, 1);

        GridPane.setHgrow(g, Priority.ALWAYS);
        GridPane.setVgrow(g, Priority.ALWAYS);

        getChildren().add(g);
        VBox.setVgrow(g, Priority.ALWAYS);
    }

    // Helper to make standardised box for a single vital sign
    private VBox vitalBox(String label, Label value) {
        Label l = new Label(label);
        l.getStyleClass().add("vital-label");

        value.getStyleClass().add("vital-value");

        VBox box = new VBox(4, l, value);
        box.getStyleClass().add("vital-box");
        box.setFillWidth(true);
        return box;
    }

    // Start flashing alert animation loop if not already running
    private void startFlash() {
        if (flasher != null) return;
        flasher = new Timeline(new KeyFrame(Duration.millis(450), e -> {
            flashOn = !flashOn;
            pseudoClassStateChanged(PC_FLASH, flashOn);
        }));
        flasher.setCycleCount(Timeline.INDEFINITE);
        flasher.play();
    }

    // Stop flashing animation and rest to normal
    private void stopFlash() {
        if (flasher != null) {
            flasher.stop();
            flasher = null;
        }
        flashOn = false;
        pseudoClassStateChanged(PC_FLASH, false);
    }

    private static String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    // Format doubles with the unit suffix
    private static String fmt(double v, String unit, int decimals) {
        if (Double.isNaN(v)) return "--";
        String f = "%." + decimals + "f";
        return String.format(f, v) + " " + unit;
    }

    // Format blood pressure (Sys/Dia)
    private static String fmtBp(double sys, double dia) {
        if (Double.isNaN(sys) || Double.isNaN(dia)) return "--";
        return String.format("%.0f/%.0f mmHg", sys, dia);
    }
}
