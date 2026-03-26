package rpm.simulation;

import java.util.EnumSet;

public final class PatientTemplate {
    private final String label;
    private final PatientScenario scenario;
    private final PatientProfile profile;
    private final EnumSet<ChronicCondition> conditions;

    public PatientTemplate(String label,
                           PatientScenario scenario,
                           PatientProfile profile,
                           EnumSet<ChronicCondition> conditions) {
        this.label = label;
        this.scenario = scenario;
        this.profile = profile;
        this.conditions = conditions != null ? conditions : EnumSet.noneOf(ChronicCondition.class);
    }

    public String getLabel() { return label; }
    public PatientScenario getScenario() { return scenario; }
    public PatientProfile getProfile() { return profile; }
    public EnumSet<ChronicCondition> getConditions() { return EnumSet.copyOf(conditions); }
}

