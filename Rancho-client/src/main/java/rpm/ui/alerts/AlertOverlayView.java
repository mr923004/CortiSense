package rpm.ui.alerts;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import rpm.domain.PatientId;

import java.util.List;
import java.util.function.Consumer;

/*
- Visual overlay used to display active alerts as a popup.
- Shows a list of alert rows and optional resolve buttons.
- Handles basic layout and user interaction, but not alert logic.
*/
public final class AlertOverlayView extends StackPane {

    // Main popup container and list of alert rows
    private final VBox box = new VBox(10);
    private final VBox list = new VBox(8);

    // Header controls
    private final Label title = new Label("ALERT");
    private final Button dismissAll = new Button("Resolve all");

    // Callbacks supplied by the controller
    private Consumer<PatientId> onResolveOne = id -> {};
    private Runnable onResolveAll = () -> {};

    public AlertOverlayView() {

        // Only capture mouse input on the popup itself
        setPickOnBounds(false);
        setMouseTransparent(true);
        box.setMouseTransparent(false);

        StackPane.setAlignment(box, Pos.CENTER);
        StackPane.setMargin(box, new Insets(12));

        box.getStyleClass().add("alert-popup");
        title.getStyleClass().add("alert-title");
        dismissAll.getStyleClass().add("resolve-btn");

        // Clicking "Resolve all" forwards to the registered handler
        dismissAll.setOnAction(e -> onResolveAll.run());

        // Header layout: title on the left, spacer in the middle, button on the right
        HBox header = new HBox(10, title, new Region(), dismissAll);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        box.setPadding(new Insets(12));
        box.setMaxWidth(520);

        list.setFillWidth(true);

        box.getChildren().addAll(header, list);
        getChildren().add(box);

        // Hidden by default
        setVisible(false);
        setManaged(false);
    }

    public void setOnResolveOne(Consumer<PatientId> handler) {
        this.onResolveOne = handler != null ? handler : (id -> {});
    }

    public void setOnResolveAll(Runnable r) {
        this.onResolveAll = r != null ? r : () -> {};
    }

    // Display alerts on the screen
    public void showAlerts(List<AlertPopupItem> items, boolean showResolveButtons) {
        list.getChildren().clear();

        if (items == null || items.isEmpty()) {
            hide();
            return;
        }

        // Update title with number of active alerts
        title.setText("ALERT (" + items.size() + ")");

        // Show or hide the "Resolve all" button
        dismissAll.setVisible(showResolveButtons);
        dismissAll.setManaged(showResolveButtons);

        // Build a row for each alert
        for (AlertPopupItem it : items) {
            list.getChildren().add(row(it, showResolveButtons));
        }

        setVisible(true);
        setManaged(true);

        // Enable mouse interaction when visible
        setMouseTransparent(false);
        box.setMouseTransparent(false);
        toFront();
    }

    // Hide the popup completely
    public void hide() {
        setVisible(false);
        setManaged(false);
        setMouseTransparent(true);
    }

    // Build one alert row with optional resolve button
    private HBox row(AlertPopupItem it, boolean showResolveButtons) {
        Label l = new Label(it.text());
        l.setWrapText(true);

        Button resolve = new Button("Resolve");
        resolve.getStyleClass().add("resolve-btn");
        resolve.setVisible(showResolveButtons);
        resolve.setManaged(showResolveButtons);

        // Clicking resolve notifies the controller for this patient
        resolve.setOnAction(e -> onResolveOne.accept(it.id));

        HBox row = new HBox(10, l, new Region(), resolve);
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));

        row.getStyleClass().add("alert-row");
        l.getStyleClass().add("alert-row-text");

        // Size limits for the popup
        box.setMaxWidth(320);
        box.setMaxHeight(220);
        list.setFillWidth(true);
        box.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        return row;
    }

    // Simple data holder for one alert entry in the popup
    public static final class AlertPopupItem {
        public final PatientId id;
        public final String reason;

        public AlertPopupItem(PatientId id, String reason) {
            this.id = id;
            this.reason = reason;
        }

        // Build the display text shown in the UI
        public String text() {
            String bed = id.getDisplayName();
            String r = (reason == null || reason.isBlank()) ? "Out of range" : reason;
            return bed + " \u2013 " + r; // en dash
        }
    }
}
