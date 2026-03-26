package rpm.ui.menu;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import rpm.ui.app.AppContext;

public final class ViewSettingsPanel extends VBox {

    public ViewSettingsPanel(AppContext ctx) {

        getStyleClass().add("panel-card");
        setSpacing(8);
        setPadding(new Insets(12));

        Label title = new Label("View");
        title.getStyleClass().add("panel-title");

        Label current = new Label();

        // Slider controls how many patients are shown per screen
        Slider slider = new Slider(1, 16, ctx.settings.getPatientsPerScreen());
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);

        // Show the initial value
        current.setText("Patients per screen: " + ctx.settings.getPatientsPerScreen());

        // Update settings and label whenever the slider moves
        slider.valueProperty().addListener((obs, oldV, newV) -> {
            int v = (int) Math.round(newV.doubleValue());
            ctx.settings.setPatientsPerScreen(v);
            current.setText("Patients per screen: " + v);
        });

        getChildren().addAll(title, current, slider);
    }
}
