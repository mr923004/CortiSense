package rpm.ui.layout;

import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Control;
import rpm.ui.app.AppContext;
import rpm.ui.app.Router;

public final class SettingsPopup {

    public static void show(Control anchor, AppContext ctx, Router router) {
        ContextMenu menu = new ContextMenu();

        // Log out of the current session
        MenuItem logout = new MenuItem("Log out");
        logout.setOnAction(e -> router.logout());

        // Restart UI and reset settings
        MenuItem restart = new MenuItem("Restart app");
        restart.setOnAction(e -> router.restartAppUiOnly());

        // Exit the application completely
        MenuItem powerOff = new MenuItem("Power off");
        powerOff.setOnAction(e -> router.powerOffApp());

        menu.getItems().addAll(logout, restart, powerOff);

        // Show popup below the anchor control
        menu.show(anchor, Side.BOTTOM, 0, 0);
    }

    private SettingsPopup() {}

    public static void showLoginOnlyPowerOff(Button anchor) {
        // Only allow power off on the login screen
        MenuItem powerOff = new MenuItem("Power off");
        powerOff.setOnAction(e -> Platform.exit());

        ContextMenu menu = new ContextMenu(powerOff);
        menu.show(anchor, Side.BOTTOM, 0, 0);
    }
}
