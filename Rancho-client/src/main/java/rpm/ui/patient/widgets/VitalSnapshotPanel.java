package rpm.ui.patient.widgets;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.domain.VitalType;
import rpm.ui.bindings.VitalDisplay;

import java.util.Map;

public final class VitalSnapshotPanel extends VBox {

    // UI labels for displaying the current patient and vitals
    private final Label title = new Label();
    private final Label hr = new Label();
    private final Label rr = new Label();
    private final Label bp = new Label();
    private final Label temp = new Label();

    public VitalSnapshotPanel() {
        getStyleClass().add("panel-card");
        setSpacing(8);
        setPadding(new Insets(12));

        // Style and layout setup
        title.getStyleClass().add("panel-title");
        getChildren().addAll(title, hr, rr, bp, temp);

        hr.getStyleClass().add("patient-vital");
        rr.getStyleClass().add("patient-vital");
        bp.getStyleClass().add("patient-vital");
        temp.getStyleClass().add("patient-vital");
    }

    public void setSnapshot(PatientId id, VitalSnapshot snap) {

        // Show which patient this snapshot belongs to
        title.setText("Patient " + id.getDisplayName());

        // If no snapshot is available yet, show placeholders
        if (snap == null) {
            hr.setText("HR: -- bpm");
            rr.setText("RR: -- br/min");
            bp.setText("BP: --/-- mmHg");
            temp.setText("Temp: -- °C");
            return;
        }

        // Extract vital values from the snapshot
        Map<VitalType, Double> v = snap.getValues();
        double dHr = get(v, VitalType.HEART_RATE);
        double dRr = get(v, VitalType.RESP_RATE);
        double dSys = get(v, VitalType.BP_SYSTOLIC);
        double dDia = get(v, VitalType.BP_DIASTOLIC);
        double dTemp = get(v, VitalType.TEMPERATURE);

        // Format and display each vital
        hr.setText("HR: " + VitalDisplay.fmt1(dHr) + " bpm");
        rr.setText("RR: " + VitalDisplay.fmt1(dRr) + " br/min");
        bp.setText("BP: " + VitalDisplay.fmt0(dSys) + "/" + VitalDisplay.fmt0(dDia) + " mmHg");
        temp.setText("Temp: " + VitalDisplay.fmt1(dTemp) + " °C");
    }

    // Safe lookup for a value in the vital map
    private static double get(Map<VitalType, Double> m, VitalType t) {
        Double x = m.get(t);
        return x == null ? Double.NaN : x;
    }
}
