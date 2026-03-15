package alarmtests;

import org.junit.jupiter.api.Test;
import rpm.domain.alarm.ThresholdBand;

import static org.junit.jupiter.api.Assertions.*;

//verifies that alarm threshold values provided by configuration are stored exactly, ensuring the alarm engine operates on correct safety limits
class ThresholdBandTest {

    @Test
    void constructor_setsAllThresholdFields() {
        // Arrange
        ThresholdBand band = new ThresholdBand(70, 60, 100, 120);

        // Assert
        assertEquals(70, band.lowAmber);
        assertEquals(60, band.lowRed);
        assertEquals(100, band.highAmber);
        assertEquals(120, band.highRed);
    }


}
