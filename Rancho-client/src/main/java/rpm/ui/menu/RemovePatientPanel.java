package rpm.ui.menu;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import rpm.domain.PatientId;
import rpm.ui.app.AppContext;

import java.util.List;

public final class RemovePatientPanel extends VBox {

    // Dropdown used to select which patient to remove
    private final ComboBox<PatientId> selector = new ComboBox<>();

    public RemovePatientPanel(AppContext ctx) {

        getStyleClass().add("panel-card");
        setSpacing(8);
        setPadding(new Insets(12));

        Label title = new Label("View");
        title.getStyleClass().add("panel-title");

        // Load the initial list of patient IDs into the selector
        refreshIds(ctx);

        // Reload the patient list manually
        Button refresh = new Button("Refresh list");
        refresh.getStyleClass().add("banner-btn");
        refresh.setMaxWidth(Double.MAX_VALUE);
        refresh.setOnAction(e -> refreshIds(ctx));

        // Button label exists for layout consistency (not currently wired)
        Button add = new Button("Add patient");
        add.getStyleClass().add("banner-btn");
        add.setMaxWidth(Double.MAX_VALUE);

        // Remove the currently selected patient
        Button remove = new Button("Remove selected");
        remove.getStyleClass().add("btn-soft");
        remove.setMaxWidth(Double.MAX_VALUE);
        remove.setOnAction(e -> {
            PatientId id = selector.getValue();
            if (id == null) return;

            boolean ok = ctx.ward.removePatient(id);
            if (!ok) {
                // Removal can fail if trying to remove protected beds or minimum count
                System.out.println("Remove failed (may be <= 8 beds or minimum patients).");
            }

            // Refresh list after any removal attempt
            refreshIds(ctx);
        });

        getChildren().addAll(
                title,
                selector,
                remove,
                refresh,
                new Label("Note: Beds 01–08 cannot be removed.")
        );
    }

    // Reloads patient IDs from the ward into the dropdown
    private void refreshIds(AppContext ctx) {
        List<PatientId> ids = ctx.ward.getPatientIds();
        selector.getItems().setAll(ids);

        // Default to first item so something is always selected
        if (!ids.isEmpty()) selector.setValue(ids.get(0));
    }
}
