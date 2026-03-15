import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class Sql {
    private Sql() {}

    public static void initSchema(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS patients (" +
                            " bed_id TEXT PRIMARY KEY" +
                            ")"
            );

            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS vital_snapshots (" +
                            " id BIGSERIAL PRIMARY KEY," +
                            " bed_id TEXT NOT NULL REFERENCES patients(bed_id) ON DELETE CASCADE," +
                            " ts_ms BIGINT NOT NULL," +
                            " hr DOUBLE PRECISION," +
                            " rr DOUBLE PRECISION," +
                            " sys DOUBLE PRECISION," +
                            " dia DOUBLE PRECISION," +
                            " temp DOUBLE PRECISION" +
                            ")"
            );

            s.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_vital_bed_ts " +
                            "ON vital_snapshots(bed_id, ts_ms)"
            );

            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ecg_segments (" +
                            " id BIGSERIAL PRIMARY KEY," +
                            " bed_id TEXT NOT NULL REFERENCES patients(bed_id) ON DELETE CASCADE," +
                            " ts_start_ms BIGINT NOT NULL," +
                            " fs_hz INT NOT NULL," +
                            " samples TEXT NOT NULL" +
                            ")"
            );

            s.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_ecg_bed_ts " +
                            "ON ecg_segments(bed_id, ts_start_ms DESC)"
            );
        }
    }
}

