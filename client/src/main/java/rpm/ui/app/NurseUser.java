package rpm.ui.app;

public final class NurseUser {

    /*
    - Represents a logged-in nurse user.
    - Stores basic identity and display information.
    */

    private final String username;
    private final String name;
    private final String surname;
    private final String nurseId;

    public NurseUser(String username, String name, String surname, String nurseId) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.nurseId = nurseId;
    }

    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getNurseId() { return nurseId; }
}
