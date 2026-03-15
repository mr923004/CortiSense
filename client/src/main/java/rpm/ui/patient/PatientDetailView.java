package rpm.ui.patient;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.ui.app.AppContext;
import rpm.ui.app.Router;
import rpm.ui.patient.widgets.EcgPanel;
import rpm.ui.patient.widgets.HistorySearchPanel;
import rpm.ui.patient.widgets.VitalSnapshotPanel;

public final class PatientDetailView extends BorderPane {

    private final AppContext ctx;
    private final PatientId patientId;

    // UI panels
    private final VitalSnapshotPanel snapshotPanel;
    private final EcgPanel ecgPanel;
    private final HistorySearchPanel historyPanel;

    // Timer for refreshing UI data
    private final Timeline uiTick;

    public PatientDetailView(AppContext ctx, Router router, PatientId patientId) {
        this.ctx = ctx;
        this.patientId = patientId;

        // Panel showing latest vitals
        snapshotPanel = new VitalSnapshotPanel();
        snapshotPanel.getStyleClass().add("panel-card");

        // ECG waveform display
        ecgPanel = new EcgPanel();
        ecgPanel.reset();

        // Panel for searching historical vitals
        historyPanel = new HistorySearchPanel(ctx, patientId);
        historyPanel.getStyleClass().add("panel-card");

        // Button that opens external report page
        Button reportBtn = new Button("Generate Report (external website)");
        reportBtn.getStyleClass().add("primary-btn");
        reportBtn.setMaxWidth(Double.MAX_VALUE);
        reportBtn.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop()
                        .browse(new java.net.URI("https://bioeng-rancho-app.impaas.uk/"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Left sidebar layout
        VBox left = new VBox(12, snapshotPanel, reportBtn, historyPanel);
        left.getStyleClass().add("patient-detail-view");
        left.setPrefWidth(380);

        // Main layout: sidebar + ECG view
        BorderPane content = new BorderPane();
        content.getStyleClass().add("patient-detail-bg");
        content.setLeft(left);
        content.setCenter(ecgPanel);

        setCenter(content);

        // Padding around the entire view
        setPadding(new Insets(18, 18, 18, 18));

        // Space between sidebar and ECG panel
        BorderPane.setMargin(ecgPanel, new Insets(0, 0, 0, 12));

        // Refresh UI frequently for ECG animation and live vitals
        uiTick = new Timeline(new KeyFrame(Duration.millis(40), e -> refresh()));
        uiTick.setCycleCount(Timeline.INDEFINITE);
        uiTick.play();

        // Stop timer when the view is removed from the scene
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) uiTick.stop();
        });

        // Initial paint
        refresh();
    }

    private void refresh() {

        // Pull latest vital snapshot for this patient
        VitalSnapshot snap = ctx.ward.getPatientLatestSnapshot(patientId);
        snapshotPanel.setSnapshot(patientId, snap);

        // Pull latest ECG segment and append it to the graph
        double[] seg = ctx.ward.getPatientLastEcgSegment(patientId);
        ecgPanel.append(seg);
    }
}
