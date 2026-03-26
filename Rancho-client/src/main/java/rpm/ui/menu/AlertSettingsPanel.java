package rpm.ui.menu;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import rpm.ui.app.AlertDuration;
import rpm.ui.app.AlertPreference;
import rpm.ui.app.AppContext;

public final class AlertSettingsPanel extends VBox {

    public AlertSettingsPanel(AppContext ctx) {
        getStyleClass().add("panel-card");
        setSpacing(8);
        setPadding(new Insets(12));

        // Panel title
        Label title = new Label("Alert");
        title.getStyleClass().add("panel-title");

        // Dropdown for choosing how alerts are presented (visual only vs audio + visual)
        ComboBox<AlertPreference> pref = new ComboBox<>();
        pref.getItems().addAll(
                AlertPreference.VISUAL_ONLY,
                AlertPreference.AUDIO_AND_VISUAL
        );

        // Initialize with the current user setting
        pref.setValue(ctx.settings.getAlertPreference());

        // Convert enum values into user-friendly labels in the dropdown
        pref.setConverter(new javafx.util.StringConverter<AlertPreference>() {
            @Override
            public String toString(AlertPreference p) {
                if (p == null) return "";
                switch (p) {
                    case VISUAL_ONLY:
                        return "Visual only";
                    case AUDIO_AND_VISUAL:
                        return "Audio + visual";
                    default:
                        return "";
                }
            }

            @Override
            public AlertPreference fromString(String s) {
                return null; // not needed for this UI
            }
        });

        // Save the selected alert preference back into settings
        pref.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) ctx.settings.setAlertPreference(newV);
        });

        // Dropdown for choosing how long alerts remain active
        ComboBox<AlertDuration> dur = new ComboBox<>();
        dur.getItems().addAll(
                AlertDuration.SEC_10,
                AlertDuration.SEC_30,
                AlertDuration.MIN_1,
                AlertDuration.UNTIL_RESOLVED
        );

        // Initialize with the current duration setting
        dur.setValue(ctx.settings.getAlertDuration());

        // Convert enum values into readable labels for display
        dur.setConverter(new javafx.util.StringConverter<AlertDuration>() {
            @Override
            public String toString(AlertDuration d) {
                if (d == null) return "";
                switch (d) {
                    case SEC_10:
                        return "10 seconds";
                    case SEC_30:
                        return "30 seconds";
                    case MIN_1:
                        return "1 minute";
                    case UNTIL_RESOLVED:
                        return "Until resolved";
                    default:
                        return "";
                }
            }

            @Override
            public AlertDuration fromString(String s) {
                return null; // not needed for this UI
            }
        });

        // Save the selected alert duration back into settings
        dur.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) ctx.settings.setAlertDuration(newV);
        });

        // Layout rows for the two settings
        HBox row1 = new HBox(10, new Label("Type:"), pref);
        HBox row2 = new HBox(10, new Label("Duration:"), dur);

        getChildren().addAll(title, row1, row2);
    }
}
