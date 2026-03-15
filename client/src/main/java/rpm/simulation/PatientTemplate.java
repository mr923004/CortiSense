package rpm.simulation;

import java.util.EnumSet;

public final class PatientTemplate {

    // Display label shown in the UI (e.g. "Hypertension", "Healthy")
    private final String label;

    // Clinical scenario that defines ranges and behaviour
    private final PatientScenario scenario;

    // Baseline values for this patient
    private final PatientProfile profile;

    // Any chronic conditions assigned to this patient
    private final EnumSet<ChronicCondition> conditions;

    public PatientTemplate(String label,
                           PatientScenario scenario,
                           PatientProfile profile,
                           EnumSet<ChronicCondition> conditions) {
        this.label = label;
        this.scenario = scenario;
        this.profile = profile;

        // If no conditions are provided, default to an empty set
        this.conditions =
                (conditions != null)
                        ? conditions
                        : EnumSet.noneOf(ChronicCondition.class);
    }

    public String getLabel() {
        return label;
    }

    public PatientScenario getScenario() {
        return scenario;
    }

    public PatientProfile getProfile() {
        return profile;
    }

    // Return a copy so callers cannot modify internal state
    public EnumSet<ChronicCondition> getConditions() {
        return EnumSet.copyOf(conditions);
    }
}
