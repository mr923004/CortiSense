package rpm.simulation;

import rpm.domain.PatientId;

import java.util.EnumSet;

// Holds basic display and profile information for a patient in the ward
public final class PatientCard {

    private final PatientId patientId;
    private final String label;
    private final EnumSet<ChronicCondition> conditions;

    public PatientCard(PatientId patientId,
                       String label,
                       EnumSet<ChronicCondition> conditions) {
        this.patientId = patientId;
        this.label = label;
        this.conditions =
                (conditions != null)
                        ? conditions
                        : EnumSet.noneOf(ChronicCondition.class);
    }

    public PatientId getPatientId() {
        return patientId;
    }

    public String getLabel() {
        return label;
    }

    // Returns a defensive copy so callers cannot modify internal state
    public EnumSet<ChronicCondition> getConditions() {
        return EnumSet.copyOf(conditions);
    }
}
