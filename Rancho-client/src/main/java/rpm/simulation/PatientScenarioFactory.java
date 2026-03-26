package rpm.simulation;

/*
- Creates PatientSimulator objects from predefined scenarios.
- Keeps all simulator setup in one place so it is easy to change later
  without touching the rest of the code.
*/
public final class PatientScenarioFactory {

    private PatientScenarioFactory() {
        // Utility class – should not be created
    }

    /*
    - Build a patient using the default baseline values and limits
      defined in the scenario.
    */
    public static PatientSimulator create(PatientScenario scenario) {

        HeartRateSimulator hrSim = new HeartRateSimulator(
                scenario.getHrBaseline(),
                scenario.getHrMin(),
                scenario.getHrMax()
        );

        RespRateSimulator rrSim = new RespRateSimulator(
                scenario.getRrBaseline(),
                scenario.getRrMin(),
                scenario.getRrMax()
        );

        BloodPressureSimulator bpSim = new BloodPressureSimulator(
                scenario.getBpBaselineSystolic(),
                scenario.getBpBaselineDiastolic(),
                scenario.getBpMinSystolic(),
                scenario.getBpMaxSystolic()
        );

        TemperatureSimulator tempSim = new TemperatureSimulator(
                scenario.getTempBaseline(),
                scenario.getTempMin(),
                scenario.getTempMax()
        );

        return new PatientSimulator(hrSim, rrSim, bpSim, tempSim);
    }

    /*
    - Build a patient using a custom profile for baseline values.
    - Safety limits still come from the scenario so values stay realistic.
    - This lets patients behave slightly differently even if they share
      the same scenario.
    */
    public static PatientSimulator create(PatientScenario scenario, PatientProfile profile) {

        HeartRateSimulator hrSim = new HeartRateSimulator(
                profile.getHrBaseline(),
                scenario.getHrMin(),
                scenario.getHrMax()
        );

        RespRateSimulator rrSim = new RespRateSimulator(
                profile.getRrBaseline(),
                scenario.getRrMin(),
                scenario.getRrMax()
        );

        BloodPressureSimulator bpSim = new BloodPressureSimulator(
                profile.getBpSysBaseline(),
                profile.getBpDiaBaseline(),
                scenario.getBpMinSystolic(),
                scenario.getBpMaxSystolic()
        );

        TemperatureSimulator tempSim = new TemperatureSimulator(
                profile.getTempBaseline(),
                scenario.getTempMin(),
                scenario.getTempMax()
        );

        return new PatientSimulator(hrSim, rrSim, bpSim, tempSim);
    }
}
