package rpm.ui.authentication;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import rpm.ui.app.AppContext;
import rpm.ui.app.NurseUser;
import rpm.ui.app.Router;

import java.util.HashMap;
import java.util.Map;

/*
- Builds the login screen and displays a username / password form.
- Credentials are checked against a simple in-memory mock database.
- On successful login, a session is created and the user is routed
  to the dashboard.
*/
public final class LoginView extends VBox {

    private static final double CARD_WIDTH = 400;
    private static final String LOGO_RESOURCE = "/rpm/ui/assets/rancho-logo.png";

    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Button loginButton;
    private final Label errorLabel;

    // Simple mock database for demo/testing purposes
    private final Map<String, String> userDatabase = new HashMap<>();

    public LoginView(AppContext ctx, Router router) {

        // Populate mock users
        userDatabase.put("juan", "abc");
        userDatabase.put("Holloway", "Nettles");
        userDatabase.put("nurse1", "securePassword1");
        userDatabase.put("nurse2", "securePassword2");
        userDatabase.put("nurse3", "securePassword3");
        userDatabase.put("doctor", "superSecurePassword");
        userDatabase.put("admin", "superDuperSecurePassword");

        // Layout configuration
        setMaxWidth(CARD_WIDTH);
        setAlignment(Pos.TOP_LEFT);
        setSpacing(10);
        getStyleClass().add("login-card");

        ImageView logo = buildLogo();

        Label titleLabel = new Label("Sign in");
        titleLabel.getStyleClass().add("login-title");

        Label subtitle = new Label("Rancho • Remote Patient Monitoring");
        subtitle.getStyleClass().add("login-subtitle");

        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("login-label");

        usernameField = new TextField();
        usernameField.setPromptText("e.g. nurse1");
        usernameField.getStyleClass().add("login-input");

        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("login-label");

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.getStyleClass().add("login-input");

        loginButton = new Button("Login");
        loginButton.getStyleClass().add("login-button");
        loginButton.setDisable(true);
        loginButton.setMaxWidth(Double.MAX_VALUE);

        errorLabel = new Label("Invalid credentials");
        errorLabel.getStyleClass().add("login-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Enable login button only when both fields contain text
        usernameField.textProperty().addListener((obs, o, n) -> updateLoginButtonState());
        passwordField.textProperty().addListener((obs, o, n) -> updateLoginButtonState());

        // Button click attempts login
        loginButton.setOnAction(e -> handleLogin(ctx, router));

        // Pressing Enter in either field also attempts login
        usernameField.setOnAction(e -> tryLogin());
        passwordField.setOnAction(e -> tryLogin());

        getChildren().addAll(
                logo,
                titleLabel,
                subtitle,
                usernameLabel,
                usernameField,
                passwordLabel,
                passwordField,
                loginButton,
                errorLabel
        );
    }

    // Load logo image from resources and scale it to fit the card
    private ImageView buildLogo() {
        var url = getClass().getResource(LOGO_RESOURCE);
        if (url == null) return new ImageView();

        ImageView iv = new ImageView(new Image(url.toExternalForm(), true));
        iv.setPreserveRatio(true);
        iv.setFitWidth(CARD_WIDTH - 40);
        iv.setSmooth(true);
        return iv;
    }

    // Disable login button when either field is empty
    private void updateLoginButtonState() {
        boolean filled =
                !usernameField.getText().trim().isEmpty() &&
                        !passwordField.getText().trim().isEmpty();

        loginButton.setDisable(!filled);
    }

    private void tryLogin() {
        if (!loginButton.isDisable()) loginButton.fire();
    }

    // Validate credentials and start a session if successful
    private void handleLogin(AppContext ctx, Router router) {
        if (validateLogin()) {
            String user = usernameField.getText();
            ctx.session.setUser(new NurseUser(user, "Nurse", "User", "ID123"));
            router.showDashboard();
        }
    }

    // Check credentials against the mock database
    private boolean validateLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        boolean ok =
                userDatabase.containsKey(user) &&
                        userDatabase.get(user).equals(pass);

        if (ok) {
            setErrorVisible(false);
            return true;
        }

        setErrorVisible(true);
        passwordField.clear();
        return false;
    }

    private void setErrorVisible(boolean on) {
        errorLabel.setVisible(on);
        errorLabel.setManaged(on);
    }
}
