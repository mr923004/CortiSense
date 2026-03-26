package alarmtests;

import org.junit.jupiter.api.Test;
import rpm.domain.VitalType;
import rpm.domain.alarm.AlarmLevel;
import rpm.domain.alarm.VitalAlarmStatus;
import rpm.domain.alarm.AlarmState;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlarmStateTest {

    //Verifies that getOverall() returns the correct overall alarm when only a single vital is present
    @Test
    void getOverall_returnsOverallAlarmLevel_forSingleVital() {
        //Arrange: single vital alarm
        VitalAlarmStatus heartRateStatus = new VitalAlarmStatus(
                VitalType.HEART_RATE,
                AlarmLevel.GREEN,
                "Heart rate normal",
                Instant.now()
        );

        Map<VitalType, VitalAlarmStatus> vitals = Map.of(
                VitalType.HEART_RATE, heartRateStatus
        );

        AlarmState state = new AlarmState(
                AlarmLevel.AMBER,  // simulate overall alarm level
                vitals
        );

        //Act
        AlarmLevel overall = state.getOverall();

        //Assert
        assertEquals(AlarmLevel.AMBER, overall);
    }


    //Verifies that getOverall() returns the correct overall alarm when multiple vitals have different levels
    @Test
    void getOverall_returnsOverallAlarmLevel_forMultipleVitals() {
        //Arrange: multiple vital alarms
        VitalAlarmStatus heartRateStatus = new VitalAlarmStatus(
                VitalType.HEART_RATE,
                AlarmLevel.GREEN,
                "Heart rate normal",
                Instant.now()
        );

        VitalAlarmStatus systolicBPStatus = new VitalAlarmStatus(
                VitalType.BP_SYSTOLIC,
                AlarmLevel.RED,
                "High systolic BP detected",
                Instant.now()
        );

        Map<VitalType, VitalAlarmStatus> vitals = Map.of(
                VitalType.HEART_RATE, heartRateStatus,
                VitalType.BP_SYSTOLIC, systolicBPStatus
        );

        AlarmState state = new AlarmState(
                AlarmLevel.RED,  // simulate overall level
                vitals
        );

        //Act
        AlarmLevel overall = state.getOverall();

        //Assert
        assertEquals(AlarmLevel.RED, overall);
    }


}

