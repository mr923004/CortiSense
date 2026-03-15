package rpm.ui.menu;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import rpm.ui.app.AppContext;
import rpm.ui.app.Router;

public final class MenuView extends BorderPane {

    public MenuView(AppContext ctx, Router router) {

        // Left column - display and alert related settings
        VBox left = new VBox(12,
                new ViewSettingsPanel(ctx),
                new RotationSettingsPanel(ctx),
                new AlertSettingsPanel(ctx)
        );
        left.setPadding(new Insets(15));

        // Right column - patient management controls
        VBox right = new VBox(12,
                new RemovePatientPanel(ctx),
                new AddPatientPanel(ctx)
        );
        right.setPadding(new Insets(15));

        // Main horizontal layout holding both columns
        HBox content = new HBox(15, left, right);
        content.setPadding(new Insets(16));
        content.getStyleClass().add("patient");   // background styling

        // Allow both columns to grow evenly with window resizing
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        // Keep both panels visually balanced
        left.setPrefWidth(520);
        right.setPrefWidth(520);

        setCenter(content);
    }
}
