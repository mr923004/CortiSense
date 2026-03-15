public final class DbConfig {
    public final String url;
    public final String user;
    public final String password;

    public DbConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String sanitized() {
        return "url=" + url + ", user=" + (user == null ? "(null)" : user);
    }

    public static DbConfig fromEnv() {
        // Preferred: explicit DB_* vars
        String url = getenv("DB_URL");
        String user = getenv("DB_USER");
        String pw  = getenv("DB_PASSWORD");

        // Fallback: Tsuru postgres service vars
        if (url == null || url.isBlank()) {
            String host = getenv("PGHOST");
            String port = getenv("PGPORT");
            String db   = getenv("PGDATABASE");
            String pgUser = getenv("PGUSER");
            String pgPw   = getenv("PGPASSWORD");

            if (host != null && !host.isBlank()
                    && port != null && !port.isBlank()
                    && db != null && !db.isBlank()) {
                url = "jdbc:postgresql://" + host.trim() + ":" + port.trim() + "/" + db.trim();
                if (user == null || user.isBlank()) user = pgUser;
                if (pw == null || pw.isBlank()) pw = pgPw;
            }
        }

        if (url == null || url.isBlank()) return null;
        return new DbConfig(url, user, pw);
    }

    private static String getenv(String k) {
        String v = System.getenv(k);
        return v == null ? null : v.trim();
    }
}
