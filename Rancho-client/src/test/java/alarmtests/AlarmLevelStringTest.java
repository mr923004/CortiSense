package alarmtests;

import org.junit.jupiter.api.Test;
import rpm.domain.alarm.AlarmLevel;

import static org.junit.jupiter.api.Assertions.*;

//verifies that alarm levels produce correct string values
class AlarmLevelStringTest {

    @Test
    void redAlarmLevel_shouldHaveCorrectString() {
        assertEquals("RED", AlarmLevel.RED.name());
    }

    @Test
    void amberAlarmLevel_shouldHaveCorrectString() {
        assertEquals("AMBER", AlarmLevel.AMBER.name());
    }

    @Test
    void normalAlarmLevel_shouldHaveCorrectString() {
        assertEquals("GREEN", AlarmLevel.GREEN.name());
    }
}
