package rpm.simulation;

public final class PatientScenarioFactory {
    private PatientScenarioFactory() {
    }

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
