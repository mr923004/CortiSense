import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbTelemetryStore extends AbstractTelemetryStore {
    private static final Logger log = Logger.getLogger(DbTelemetryStore.class.getName());
    private static final int DEFAULT_VITAL_LIMIT = 600;

    private final DbConfig cfg;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getLogger(DbTelemetryStore.class.getName())
                    .warning("PostgreSQL JDBC driver not found on classpath (org.postgresql.Driver).");
        }
    }

    public DbTelemetryStore(DbConfig cfg) {
        this.cfg = cfg;
        try (Connection c = open()) {
            Sql.initSchema(c);
            log.info("DB schema init OK");
        } catch (SQLException e) {
            throw new RuntimeException("DB schema init failed", e);
        }
    }

    private Connection open() throws SQLException {
        DriverManager.setLoginTimeout(5);

        if (cfg.user == null || cfg.user.isBlank()) {
            return DriverManager.getConnection(cfg.url);
        }
        return DriverManager.getConnection(cfg.url, cfg.user, cfg.password);
    }

    @Override
    public void store(String bedId, PatientTelemetry data) {
        if (bedId == null || bedId.isBlank() || data == null) return;
        long now = System.currentTimeMillis();

        try (Connection c = open()) {
            c.setAutoCommit(false);
            ensurePatient(c, bedId);
            insertVitals(c, bedId, data, now);
            insertEcg(c, bedId, data, now);
            c.commit();
        } catch (SQLException e) {
            log.log(Level.WARNING, "DB store failed for " + bedId, e);
        }
    }

    private void ensurePatient(Connection c, String bedId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO patients(bed_id) VALUES (?) ON CONFLICT (bed_id) DO NOTHING"
        )) {
            ps.setString(1, bedId);
            ps.executeUpdate();
        }
    }

    private void insertVitals(Connection c, String bedId, PatientTelemetry data, long now) throws SQLException {
        List<Long> ts = data.getTs();
        int n = maxSize(ts, data.getHr(), data.getRr(), data.getSys(), data.getDia(), data.getTemp());
        if (n <= 0) return;

        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO vital_snapshots(bed_id, ts_ms, hr, rr, sys, dia, temp) VALUES (?,?,?,?,?,?,?)"
        )) {
            for (int i = 0; i < n; i++) {
                long t = getOr(ts, i, now);
                ps.setString(1, bedId);
                ps.setLong(2, t);
                setNullableDouble(ps, 3, getOr(data.getHr(), i, null));
                setNullableDouble(ps, 4, getOr(data.getRr(), i, null));
                setNullableDouble(ps, 5, getOr(data.getSys(), i, null));
                setNullableDouble(ps, 6, getOr(data.getDia(), i, null));
                setNullableDouble(ps, 7, getOr(data.getTemp(), i, null));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertEcg(Connection c, String bedId, PatientTelemetry data, long now) throws SQLException {
        if (data.getEcg() == null || data.getEcg().isEmpty()) return;

        Long startMs = data.getEcgTsStart();
        Integer fs = data.getEcgFs();
        if (startMs == null) startMs = now;
        if (fs == null) fs = 250;

        String samples = joinDoubles(data.getEcg());

        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO ecg_segments(bed_id, ts_start_ms, fs_hz, samples) VALUES (?,?,?,?)"
        )) {
            ps.setString(1, bedId);
            ps.setLong(2, startMs);
            ps.setInt(3, fs);
            ps.setString(4, samples);
            ps.executeUpdate();
        }
    }

    @Override
    public PatientTelemetry get(String bedId) {
        if (bedId == null || bedId.isBlank()) return null;

        PatientTelemetry out = new PatientTelemetry();
        try (Connection c = open()) {
            List<Row> rows = new ArrayList<>();

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT ts_ms, hr, rr, sys, dia, temp " +
                            "FROM vital_snapshots WHERE bed_id=? " +
                            "ORDER BY ts_ms DESC LIMIT ?"
            )) {
                ps.setString(1, bedId);
                ps.setInt(2, DEFAULT_VITAL_LIMIT);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new Row(
                                rs.getLong(1),
                                (Double) rs.getObject(2),
                                (Double) rs.getObject(3),
                                (Double) rs.getObject(4),
                                (Double) rs.getObject(5),
                                (Double) rs.getObject(6)
                        ));
                    }
                }
            }

            Collections.reverse(rows);
            for (Row r : rows) {
                out.getTs().add(r.tsMs);
                out.getHr().add(r.hr);
                out.getRr().add(r.rr);
                out.getSys().add(r.sys);
                out.getDia().add(r.dia);
                out.getTemp().add(r.temp);
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT ts_start_ms, fs_hz, samples " +
                            "FROM ecg_segments WHERE bed_id=? " +
                            "ORDER BY ts_start_ms DESC LIMIT 1"
            )) {
                ps.setString(1, bedId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long start = rs.getLong(1);
                        int fs = rs.getInt(2);
                        String samples = rs.getString(3);
                        setField(out, start, fs, samples);
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "DB get failed for " + bedId, e);
            return null;
        }

        return out;
    }

    private void setField(PatientTelemetry out, long start, int fs, String samples) {
        try {
            java.lang.reflect.Field fStart = PatientTelemetry.class.getDeclaredField("ecgTsStart");
            java.lang.reflect.Field fFs = PatientTelemetry.class.getDeclaredField("ecgFs");
            fStart.setAccessible(true);
            fFs.setAccessible(true);
            fStart.set(out, start);
            fFs.set(out, fs);
        } catch (Exception ignored) {}

        if (samples == null || samples.isBlank()) return;
        String[] parts = samples.split(",");
        for (String p : parts) {
            String t = p.trim();
            if (t.isEmpty()) continue;
            try { out.getEcg().add(Double.parseDouble(t)); }
            catch (NumberFormatException ignored) {}
        }
    }

    @Override
    public Map<String, PatientTelemetry> getAll() {
        Map<String, PatientTelemetry> out = new TreeMap<>();
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement("SELECT bed_id FROM patients ORDER BY bed_id");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String bed = rs.getString(1);
                PatientTelemetry t = get(bed);
                if (t != null) out.put(bed, t);
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "DB getAll failed", e);
        }
        return out;
    }

    private static int maxSize(List<?>... lists) {
        int m = 0;
        for (List<?> l : lists) {
            if (l != null) m = Math.max(m, l.size());
        }
        return m;
    }

    private static <T> T getOr(List<T> list, int i, T fallback) {
        if (list == null) return fallback;
        if (i < 0 || i >= list.size()) return fallback;
        return list.get(i);
    }

    private static long getOr(List<Long> list, int i, long fallback) {
        if (list == null) return fallback;
        if (i < 0 || i >= list.size()) return fallback;
        Long v = list.get(i);
        return v != null ? v : fallback;
    }

    private static void setNullableDouble(PreparedStatement ps, int idx, Double v) throws SQLException {
        if (v == null || v.isNaN()) ps.setNull(idx, Types.DOUBLE);
        else ps.setDouble(idx, v);
    }

    private static String joinDoubles(List<Double> xs) {
        StringBuilder sb = new StringBuilder(xs.size() * 8);
        for (int i = 0; i < xs.size(); i++) {
            Double v = xs.get(i);
            if (i > 0) sb.append(',');
            if (v == null || v.isNaN()) sb.append("0.0");
            else sb.append(v);
        }
        return sb.toString();
    }

    private static final class Row {
        final long tsMs;
        final Double hr, rr, sys, dia, temp;

        Row(long tsMs, Double hr, Double rr, Double sys, Double dia, Double temp) {
            this.tsMs = tsMs;
            this.hr = hr;
            this.rr = rr;
            this.sys = sys;
            this.dia = dia;
            this.temp = temp;
        }
    }
}
