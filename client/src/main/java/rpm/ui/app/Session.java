package rpm.ui.app;

public final class Session {

    /*
    - Tracks the currently logged-in user for the application.
    - Used by the UI to determine authentication state and display user info.
    */

    private NurseUser currentUser;

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public NurseUser getUser() {
        return currentUser;
    }

    public void setUser(NurseUser user) {
        this.currentUser = user;
    }

    public void clear() {
        this.currentUser = null;
    }
}
