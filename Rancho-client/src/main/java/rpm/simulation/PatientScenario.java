package rpm.simulation;

/*
 Defines high-level clinical patient scenarios.

 Each scenario encodes baseline values and safe physiological ranges
 for the vital sign simulators. These values are used to initialize
 patient profiles and constrain randomly generated vital signs so
 that they remain medically plausible.

 Scenarios represent simplified clinical archetypes rather than
 full diagnostic models.
 */
public enum PatientScenario {

    NORMAL_ADULT(
            75.0, 60.0, 100.0,         // HR baseline / min / max (bpm)
            16.0, 12.0, 20.0,          // RR baseline / min / max (breaths/min)
            120.0, 80.0, 90.0, 140.0,  // BP baseline sys / dia + min / max systolic (mmHg)
            36.8, 36.5, 37.5           // Temperature baseline / min / max (°C)
    ),

    BRADYCARDIA(
            50.0, 40.0, 70.0,
            14.0, 10.0, 18.0,
            115.0, 75.0, 90.0, 130.0,
            36.7, 36.4, 37.3
    ),

    TACHYCARDIA(
            110.0, 90.0, 140.0,
            22.0, 16.0, 30.0,
            130.0, 85.0, 100.0, 160.0,
            37.1, 36.8, 37.8
    ),

    HYPERTENSION(
            78.0, 60.0, 100.0,
            16.0, 12.0, 22.0,
            150.0, 95.0, 110.0, 190.0,
            36.9, 36.5, 37.7
    );

    // Heart rate (beats per minute)
    private final double hrBaseline;
    private final double hrMin;
    private final double hrMax;

    // Respiratory rate (breaths per minute)
    private final double rrBaseline;
    private final double rrMin;
    private final double rrMax;

    // Blood pressure (mmHg)
    private final double bpBaselineSystolic;
    private final double bpBaselineDiastolic;
    private final double bpMinSystolic;
    private final double bpMaxSystolic;

    // Body temperature (°C)
    private final double tempBaseline;
    private final double tempMin;
    private final double tempMax;

    PatientScenario(double hrBaseline, double hrMin, double hrMax,
                    double rrBaseline, double rrMin, double rrMax,
                    double bpBaselineSystolic, double bpBaselineDiastolic,
                    double bpMinSystolic, double bpMaxSystolic,
                    double tempBaseline, double tempMin, double tempMax) {

        this.hrBaseline = hrBaseline;
        this.hrMin = hrMin;
        this.hrMax = hrMax;

        this.rrBaseline = rrBaseline;
        this.rrMin = rrMin;
        this.rrMax = rrMax;

        this.bpBaselineSystolic = bpBaselineSystolic;
        this.bpBaselineDiastolic = bpBaselineDiastolic;
        this.bpMinSystolic = bpMinSystolic;
        this.bpMaxSystolic = bpMaxSystolic;

        this.tempBaseline = tempBaseline;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
    }

    public double getHrBaseline()          { return hrBaseline; }
    public double getHrMin()               { return hrMin; }
    public double getHrMax()               { return hrMax; }

    public double getRrBaseline()          { return rrBaseline; }
    public double getRrMin()               { return rrMin; }
    public double getRrMax()               { return rrMax; }

    public double getBpBaselineSystolic()  { return bpBaselineSystolic; }
    public double getBpBaselineDiastolic() { return bpBaselineDiastolic; }
    public double getBpMinSystolic()       { return bpMinSystolic; }
    public double getBpMaxSystolic()       { return bpMaxSystolic; }

    public double getTempBaseline()        { return tempBaseline; }
    public double getTempMin()             { return tempMin; }
    public double getTempMax()             { return tempMax; }
}
