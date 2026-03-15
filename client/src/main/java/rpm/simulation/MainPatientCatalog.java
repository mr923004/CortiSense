package rpm.simulation;

import java.util.EnumSet;

public final class MainPatientCatalog {

    private MainPatientCatalog() {
        // Utility class; not intended to be instantiated
    }

    // Returns a predefined patient template for the given bed number.
    // Used to initialise a fixed set of demo patients in the simulation.
    public static PatientTemplate templateForBed(int bedNumber) {
        switch (bedNumber) {

            case 1:
                return new PatientTemplate(
                        "Healthy (stable)",
                        PatientScenario.NORMAL_ADULT,
                        new PatientProfile(72.0, 15.0, 118.0, 78.0, 36.70),
                        EnumSet.noneOf(ChronicCondition.class)
                );

            case 2:
                return new PatientTemplate(
                        "Hypertension",
                        PatientScenario.HYPERTENSION,
                        new PatientProfile(78.0, 16.0, 165.0, 98.0, 36.90),
                        EnumSet.of(ChronicCondition.HYPERTENSION)
                );

            case 3:
                return new PatientTemplate(
                        "COPD tendency",
                        PatientScenario.NORMAL_ADULT,
                        new PatientProfile(78.0, 19.0, 122.0, 82.0, 36.80),
                        EnumSet.of(ChronicCondition.COPD_TENDENCY)
                );

            case 4:
                return new PatientTemplate(
                        "Athletic bradycardia",
                        PatientScenario.BRADYCARDIA,
                        new PatientProfile(48.0, 13.0, 112.0, 72.0, 36.60),
                        EnumSet.of(ChronicCondition.BRADYCARDIA_TENDENCY)
                );

            case 5:
                return new PatientTemplate(
                        "Heart failure risk",
                        PatientScenario.NORMAL_ADULT,
                        new PatientProfile(82.0, 18.0, 110.0, 70.0, 36.80),
                        EnumSet.of(ChronicCondition.HEART_FAILURE_RISK)
                );

            case 6:
                return new PatientTemplate(
                        "Infection risk",
                        PatientScenario.NORMAL_ADULT,
                        new PatientProfile(76.0, 16.0, 120.0, 80.0, 37.10),
                        EnumSet.of(ChronicCondition.INFECTION_RISK)
                );

            case 7:
                return new PatientTemplate(
                        "Arrhythmia tendency",
                        PatientScenario.NORMAL_ADULT,
                        new PatientProfile(85.0, 16.0, 118.0, 76.0, 36.80),
                        EnumSet.of(ChronicCondition.ARRHYTHMIA_TENDENCY)
                );

            case 8:
                return new PatientTemplate(
                        "Post-op / pain",
                        PatientScenario.NORMAL_ADULT,
                        new PatientProfile(92.0, 18.0, 125.0, 84.0, 36.90),
                        EnumSet.noneOf(ChronicCondition.class)
                );

            default:
                return null;
        }
    }
}
