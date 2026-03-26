package rpm.ui.dashboard;

import javafx.scene.control.TextField;
import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.domain.alarm.AlarmLevel;
import rpm.domain.alarm.AlarmState;
import rpm.simulation.PatientCard;
import rpm.ui.alerts.AlertRules;
import rpm.ui.app.AppContext;
import rpm.ui.app.AlertDuration;
import rpm.ui.app.Router;
import rpm.ui.layout.TopBanner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Control the main dashboard view. Manage list of patients,
// search queries, and update UI when alarm occurs or data changes
public final class DashboardController {

    private final AppContext ctx;
    private final Router router;
    private final PatientGridView grid;
    private final TopBanner banner;

    // normal paging
    private List<PatientId> ids = new ArrayList<>();
    private int pageIndex = 0;

    private final rpm.ui.alerts.AlertAcknowledger acknowledger = new rpm.ui.alerts.AlertAcknowledger();

    public DashboardController(AppContext ctx, Router router, PatientGridView grid, TopBanner banner) {
        this.ctx = ctx;
        this.router = router;
        this.grid = grid;
        this.banner = banner;

        TextField search = banner.getSearchField();
        search.setOnAction(e -> {
            String q = search.getText().trim();
            if (q.isEmpty()) return;
            PatientId found = findPatient(q);
            if (found != null) router.showPatientDetail(found);
        });

        grid.setOnPatientClicked(router::showPatientDetail);
        grid.setOnResolve(this::resolve);

        grid.setOnNextPage(() -> {
            int perScreen = ctx.settings.getPatientsPerScreen();
            int maxPage = Math.max(0, pageCount(perScreen, ids.size()) - 1);
            pageIndex = (pageIndex >= maxPage) ? 0 : (pageIndex + 1);
            renderPage();
        });

        grid.setOnPrevPage(() -> {
            pageIndex = Math.max(0, pageIndex - 1);
            renderPage();
        });

        // Setup alarm listeners
        ctx.alarms.addListener(new rpm.domain.alarm.AlarmListener() {
            @Override
            public void onAlarmTransition(rpm.domain.alarm.AlarmTransition t) {
                if (t.getTo() == AlarmLevel.RED) {
                    javafx.application.Platform.runLater(() -> {
                        long nowMs = ctx.clock.getSimTime().toEpochMilli();

                        renderPage();
                    });
                }
            }

            @Override
            public void onAlarmState(PatientId id, java.time.Instant time, AlarmState state) {}
        });

    }

    // Refresh list of patients from simulation
    public void refreshPatients() {
        ids = ctx.ward.getPatientIds();
        int perScreen = ctx.settings.getPatientsPerScreen();
        int maxPage = Math.max(0, (ids.size() - 1) / Math.max(1, perScreen));
        if (pageIndex > maxPage) pageIndex = maxPage;
    }

    public void tickUi(long nowMs) {
    }

    // Rendering loop
    public void renderPage() {
        int perScreen = ctx.settings.getPatientsPerScreen();
        boolean showResolve = ctx.settings.getAlertDuration() == AlertDuration.UNTIL_RESOLVED;

        int from = pageIndex * perScreen;
        int to = Math.min(ids.size(), from + perScreen);

        List<PatientTileModel> tiles = ids.subList(from, to).stream()
                .map(this::buildTile)
                .collect(Collectors.toList());

        grid.setTiles(tiles, pageIndex, pageCount(perScreen, ids.size()), perScreen, showResolve);

    }

    private long nowMs() { return ctx.clock.getSimTime().toEpochMilli(); }

    // Handle the user clicking "Resolve" on an alerting patient
    public void resolve(PatientId id) {
        long nowMs = ctx.clock.getSimTime().toEpochMilli();

        acknowledger.acknowledge(id, AlertRules.resolveCooldown(ctx), nowMs);
        renderPage();
    }

    // Return currently alerting patients
    private List<PatientId> alertingPatients(long nowMs) {
        return ctx.ward.getPatientIds().stream()
                .filter(id -> {
                    AlarmState s = ctx.alarms.getState(id);
                    boolean red = (s != null && s.getOverall() == AlarmLevel.RED);
                    return red && !acknowledger.isAcknowledged(id, nowMs);
                })
                .collect(Collectors.toList());
    }

    // Build a single patient tile for display
    private PatientTileModel buildTile(PatientId id) {
        VitalSnapshot snap = ctx.ward.getPatientLatestSnapshot(id);
        PatientCard card = ctx.ward.getPatientCard(id);

        String name = (card != null && card.getLabel() != null && !card.getLabel().isEmpty())
                ? card.getLabel()
                : "Patient";

        AlarmState s = ctx.alarms.getState(id);
        boolean red = (s != null && s.getOverall() == AlarmLevel.RED);

        long nowMs = ctx.clock.getSimTime().toEpochMilli();
        boolean acknowledged = acknowledger.isAcknowledged(id, nowMs);
        boolean alerting = red && !acknowledged;

        boolean showResolve = alerting && ctx.settings.getAlertDuration() == AlertDuration.UNTIL_RESOLVED;

        return PatientTileModel.from(id, name, snap, alerting, showResolve);
    }

    private int pageCount(int perScreen, int total) {
        if (total <= 0) return 1;
        return (int) Math.ceil(total / (double) Math.max(1, perScreen));
    }

    // Search for a specific patient
    private PatientId findPatient(String q) {
        try {
            int bed = Integer.parseInt(q);
            PatientId id = new PatientId(bed);
            if (ctx.ward.getPatientIds().contains(id)) return id;
        } catch (NumberFormatException ignored) {}

        for (PatientId id : ctx.ward.getPatientIds()) {
            PatientCard c = ctx.ward.getPatientCard(id);
            if (c != null && c.getLabel() != null) {
                if (c.getLabel().toLowerCase().contains(q.toLowerCase())) return id;
            }
        }
        return null;
    }
}
