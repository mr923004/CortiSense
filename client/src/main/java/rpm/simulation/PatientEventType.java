package rpm.simulation;

// Enumerates the different types of simulated patient events.
// These events temporarily influence vital signs during the simulation.
public enum PatientEventType {

    // Sudden rise in body temperature (fever episode)
    FEVER_SPIKE,

    // Temporary rapid heart rate episode (tachycardia)
    TACHY_EPISODE,

    // Sudden increase in blood pressure
    BP_SPIKE,

    // Sudden decrease in blood pressure
    BP_DROP,

    // Breathing difficulty or respiratory stress episode
    RESP_DISTRESS,

    // Progressive worsening of heart failure symptoms
    HEART_FAILURE_DECOMP,

    // Myocardial infarction–like event (simulated cardiac ischemia)
    MI_LIKE,

    // Stroke-like neurological event
    STROKE_LIKE
}
