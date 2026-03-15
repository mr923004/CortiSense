package rpm.ui.alerts;

import rpm.domain.PatientId;
import rpm.domain.alarm.AlarmLevel;
import rpm.domain.alarm.AlarmState;
import rpm.simulation.PatientCard;
import rpm.ui.app.AlertDuration;
import rpm.ui.app.AppContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import rpm.domain.VitalType;
import rpm.domain.alarm.VitalAlarmStatus;


// UI for newer alert popup system

public final class AlertOverlayController {

    private final AppContext ctx;
    private final AlertOverlayView view;

    private final AlertAcknowledger acknowledger = new AlertAcknowledger();
    private final AudioAlertManager audio;

    // timed popup behaviour
    private boolean popupActive = false;
    private long popupEndsAtMs = 0;

    // avoid re-showing the same alert set every second
    private String lastSignature = "";

    private String reasonFor(PatientId id) {
        AlarmState s = ctx.alarms.getState(id);
        if (s == null) return "Out of range";

        var byVital = s.getByVital();
        if (byVital == null || byVital.isEmpty()) return "Out of range";

        VitalType bestType = null;
        AlarmLevel bestLevel = AlarmLevel.GREEN;

        for (var e : byVital.entrySet()) {
            VitalType vt = e.getKey();
            VitalAlarmStatus st = e.getValue();
            if (st == null) continue;

            AlarmLevel lvl = st.getLevel();
            if (lvl == null) continue;

            if (bestType == null || lvl.ordinal() > bestLevel.ordinal()) {
                bestType = vt;
                bestLevel = lvl;
            }
        }

        if (bestType == null) return "Out of range";
        return shortVital(bestType) + " out of range";
    }

    private String shortVital(VitalType t) {
        switch (t) {
            case HEART_RATE:
                return "HR";
            case RESP_RATE:
                return "RR";
            case BP_SYSTOLIC:
            case BP_DIASTOLIC:
                return "BP";
            case TEMPERATURE:
                return "Temp";
            default:
                return t.name();
        }
    }


    public AlertOverlayController(AppContext ctx, AlertOverlayView view) {
        this.ctx = ctx;
        this.view = view;
        this.audio = new AudioAlertManager(ctx.settings);

        view.setOnResolveOne(this::resolveOne);
        view.setOnResolveAll(this::resolveAll);
    }

    public void tick(long nowMs) {
        audio.tick(nowMs);

        List<PatientId> alerting = alertingPatients(nowMs);

        // none -> reset everything
        if (alerting.isEmpty()) {
            popupActive = false;
            popupEndsAtMs = 0;
            lastSignature = "";
            audio.stop();
            view.hide();
            return;
        }

        // Start audio when alerts exist, based on settings preference
        audio.startFor(nowMs);

        AlertDuration mode = ctx.settings.getAlertDuration();
        boolean untilResolved = (mode == AlertDuration.UNTIL_RESOLVED);

        // stable signature: "1,4,7"
        String signature = alerting.stream()
                .sorted(Comparator.comparingInt(PatientId::getValue))
                .map(id -> Integer.toString(id.getValue()))
                .collect(Collectors.joining(","));

        // UNTIL_RESOLVED: always visible and refreshed
        if (untilResolved) {
            view.showAlerts(buildItems(alerting), true);
            popupActive = true;
            popupEndsAtMs = Long.MAX_VALUE;
            lastSignature = signature;
            return;
        }

        // TIMED mode:
        boolean signatureChanged = !signature.equals(lastSignature);

        // if popup is not active, only show it when alert set changes
        if (!popupActive) {
            if (!signatureChanged) {
                view.hide();
                return;
            }

            Duration d = mode.toDurationOrNull();
            long durMs = (d == null) ? 10_000L : d.toMillis();

            popupEndsAtMs = nowMs + durMs;
            popupActive = true;
            lastSignature = signature;

            view.showAlerts(buildItems(alerting), true);
            return;
        }

        // popup is active: if expired then hide, otherwise keep visible
        if (nowMs >= popupEndsAtMs) {
            popupActive = false;
            view.hide();
            return;
        }

        view.showAlerts(buildItems(alerting), true);
    }

    private List<AlertOverlayView.AlertPopupItem> buildItems(List<PatientId> alerting) {
        List<AlertOverlayView.AlertPopupItem> items = new ArrayList<>();
        for (PatientId id : alerting) {
            items.add(new AlertOverlayView.AlertPopupItem(
                    id,
                    reasonFor(id)
            ));
        }
        return items;
    }

    private void resolveOne(PatientId id) {
        long nowMs = ctx.clock.getSimTime().toEpochMilli();
        acknowledger.acknowledge(id, AlertRules.resolveCooldown(ctx), nowMs);

        // let popup logic re-evaluate cleanly next tick
        popupActive = false;
        popupEndsAtMs = 0;
        lastSignature = "";
    }

    private void resolveAll() {
        long nowMs = ctx.clock.getSimTime().toEpochMilli();
        for (PatientId id : alertingPatients(nowMs)) {
            acknowledger.acknowledge(id, AlertRules.resolveCooldown(ctx), nowMs);
        }
        audio.stop();
        popupActive = false;
        popupEndsAtMs = 0;
        lastSignature = "";
    }

    private List<PatientId> alertingPatients(long nowMs) {
        List<PatientId> out = new ArrayList<>();
        for (PatientId id : ctx.ward.getPatientIds()) {
            AlarmState s = ctx.alarms.getState(id);
            boolean red = (s != null && s.getOverall() == AlarmLevel.RED);
            if (red && !acknowledger.isAcknowledged(id, nowMs)) out.add(id);
        }
        return out;
    }

    private String patientName(PatientId id) {
        PatientCard card = ctx.ward.getPatientCard(id);
        if (card != null && card.getLabel() != null && !card.getLabel().isBlank()) return card.getLabel();
        return "Patient";
    }

    public void forceStop() {
        popupActive = false;
        popupEndsAtMs = 0;
        lastSignature = "";
        audio.stop();
        view.hide();
    }

}
