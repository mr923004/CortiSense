package rpm.ui.menu;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import rpm.ui.app.AppContext;

public final class RotationSettingsPanel extends VBox {

    public RotationSettingsPanel(AppContext ctx) {

        getStyleClass().add("panel-card");
        setSpacing(8);
        setPadding(new Insets(12));

        Label title = new Label("Rotation Settings");
        title.getStyleClass().add("panel-title");

        // Toggle to enable or disable automatic page rotation
        CheckBox enabled = new CheckBox("Enable auto-rotation between pages");
        enabled.setSelected(ctx.settings.isRotationEnabled());
        enabled.selectedProperty().addListener(
                (obs, oldV, newV) -> ctx.settings.setRotationEnabled(newV)
        );

        // Dropdown for how often the view should rotate
        ComboBox<Integer> seconds = new ComboBox<>();
        seconds.getItems().addAll(5, 10, 15, 30, 60);
        seconds.setValue(ctx.settings.getRotationSeconds());
        seconds.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) ctx.settings.setRotationSeconds(newV);
        });

        // Row holding the rotation interval controls
        HBox row = new HBox(10,
                new Label("Rotate every:"),
                seconds,
                new Label("seconds")
        );

        // Disable the interval controls when rotation is turned off
        row.setDisable(!enabled.isSelected());
        enabled.selectedProperty().addListener((obs, oldV, newV) -> row.setDisable(!newV));

        getChildren().addAll(title, enabled, row);
    }
}
