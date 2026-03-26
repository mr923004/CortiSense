package rpm.ui.patient.widgets;

import javafx.scene.layout.BorderPane;
import rpm.ui.EcgCanvas;

public final class EcgPanel extends BorderPane {

    // Canvas responsible for drawing the ECG waveform
    private final EcgCanvas canvas = new EcgCanvas();

    public EcgPanel() {
        setCenter(canvas);

        // Let the canvas automatically resize with the panel
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
    }

    // Append new ECG samples to the canvas
    public void append(double[] segment) {
        if (segment == null || segment.length == 0) return;
        canvas.appendSamples(segment);
    }

    // Clear the ECG display
    public void reset() {
        canvas.reset();
    }
}
