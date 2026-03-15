package rpm.ui.app;

import rpm.domain.report.InMemoryPatientDataStore;
import rpm.domain.report.ReportGenerator;
import rpm.simulation.WardManager;
import rpm.domain.alarm.AlarmService;

public final class AppContext {

    /*
    - Central container that holds shared application services.
    - Passed around so UI components can access the same state and systems.
    */

    public final WardManager ward;
    public final AlarmService alarms;
    public final InMemoryPatientDataStore store;
    public final ReportGenerator reports;
    public final Session session;
    public final UISettings settings;
    public final SimulationClock clock;

    public AppContext(WardManager ward,
                      AlarmService alarms,
                      InMemoryPatientDataStore store,
                      ReportGenerator reports,
                      Session session,
                      UISettings settings,
                      SimulationClock clock) {
        this.ward = ward;
        this.alarms = alarms;
        this.store = store;
        this.reports = reports;
        this.session = session;
        this.settings = settings;
        this.clock = clock;
    }
}
