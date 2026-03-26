package rpm.ui.layout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import rpm.ui.app.AppContext;
import rpm.ui.app.Router;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import rpm.domain.PatientId;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public final class TopBanner extends HBox {

    // Path to the application logo in resources
    private static final String LOGO_RESOURCE = "/rpm/ui/assets/rancho-logo.png";

    private final Button homeBtn = new Button();
    private final Label userLabel = new Label();
    private final TextField searchField = new TextField();
    private final Button settingsBtn = new Button("Settings");
    private final Button powerBtn = new Button("⏻");
    private final ContextMenu searchMenu = new ContextMenu();

    public TopBanner(AppContext ctx, Router router) {
        getStyleClass().add("top-banner");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        setPadding(new Insets(10, 14, 10, 14));

        // Home button with logo and app name
        homeBtn.getStyleClass().add("banner-home");
        homeBtn.setOnAction(e -> router.showDashboard());
        homeBtn.setGraphic(buildLogo(34));
        homeBtn.setText("Rancho");
        homeBtn.setGraphicTextGap(10);
        homeBtn.setContentDisplay(ContentDisplay.LEFT);
        homeBtn.setMinHeight(40);

        // Logged-in user label shown on the banner
        userLabel.getStyleClass().add("banner-user");
        userLabel.setText("");

        // Search box for finding patients by bed or label
        searchField.getStyleClass().add("banner-search");
        searchField.setPromptText("Search patient / bed…");
        searchField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Dropdown menu for search results
        searchMenu.setAutoHide(true);

        // Update search results whenever the user types
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String q = (newV == null) ? "" : newV.trim().toLowerCase();
            if (q.isEmpty()) {
                searchMenu.hide();
                return;
            }

            // Build a list of matching patients based on bed number or label
            List<Object[]> matches = ctx.ward.getPatientIds().stream()
                    .map(id -> {
                        var card = ctx.ward.getPatientCard(id);
                        String label = (card != null && card.getLabel() != null)
                                ? card.getLabel()
                                : "Patient";
                        String bed = id.getDisplayName();          // "Bed 04"
                        String full = bed + " • " + label;         // shown in dropdown
                        String hay = (bed + " " + label).toLowerCase();
                        return new Object[]{ id, full, hay };
                    })
                    .filter(arr -> ((String) arr[2]).contains(q))
                    .limit(8)
                    .collect(Collectors.toList());   // compatibility across all Java versions

            if (matches.isEmpty()) {
                searchMenu.hide();
                return;
            }

            // Populate the dropdown with matching patients
            searchMenu.getItems().clear();
            for (Object[] m : matches) {
                PatientId id = (PatientId) m[0];
                String text = (String) m[1];
                MenuItem item = new MenuItem(text);
                item.setOnAction(e -> {
                    searchMenu.hide();
                    searchField.clear();
                    router.showPatientDetail(id);
                });
                searchMenu.getItems().add(item);
            }

            // Show dropdown below the search field
            if (!searchMenu.isShowing()) {
                searchMenu.show(searchField, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        });

        // Spacer pushes buttons to the right side
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Settings button opens the settings screen
        settingsBtn.getStyleClass().add("banner-btn");
        settingsBtn.setOnAction(e -> router.showSettings());
        settingsBtn.setMinHeight(38);

        // Power button opens the power menu
        powerBtn.getStyleClass().add("banner-btn");
        powerBtn.setOnAction(e -> SettingsPopup.show(powerBtn, ctx, router));
        powerBtn.setMinHeight(38);

        getChildren().addAll(homeBtn, userLabel, searchField, spacer, settingsBtn, powerBtn);
    }

    public TextField getSearchField() {
        return searchField;
    }

    public void setUserText(String text) {
        userLabel.setText(text == null ? "" : text);
    }

    // Load and scale the logo image for the banner button
    private ImageView buildLogo(double width) {
        URL url = getClass().getResource(LOGO_RESOURCE);
        if (url == null) return new ImageView();

        ImageView iv = new ImageView(new Image(url.toExternalForm(), true));
        iv.setPreserveRatio(true);
        iv.setFitWidth(width);
        iv.setSmooth(true);
        return iv;
    }
}
