package rpm.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class EcgCanvas extends Canvas {

    // Max ECG voltage used for vertical scaling
    private static final double CALIBRATION_MAX_MV = 1.5;

    // Buffer of samples currently visible on screen (one value per pixel)
    private double[] screenValues;

    // Current write position for the sweep animation
    private int sweepPos;

    public EcgCanvas() {

        // Rebuild buffer when width changes
        widthProperty().addListener((obs, oldV, newV) -> resizeBuffer());

        // Redraw when height changes
        heightProperty().addListener((obs, oldV, newV) -> render());
    }

    public void reset() {

        // Clear waveform data and restart sweep
        screenValues = null;
        sweepPos = 0;
        render();
    }

    public void appendSamples(double[] samples) {
        if (samples == null || samples.length == 0) {
            return;
        }

        int width = (int) Math.floor(getWidth());
        if (width <= 0) {
            return;
        }

        // Allocate buffer if size changed or first time
        if (screenValues == null || screenValues.length != width) {
            screenValues = new double[width];
            sweepPos = 0;
        }

        // Write incoming samples into circular buffer
        for (double v : samples) {
            screenValues[sweepPos] = v;
            sweepPos++;
            if (sweepPos >= screenValues.length) {
                sweepPos = 0;
            }
        }

        // Redraw after adding samples
        render();
    }

    private void resizeBuffer() {

        int width = (int) Math.floor(getWidth());
        if (width <= 0) {

            // Canvas not visible yet
            screenValues = null;
            sweepPos = 0;
            return;
        }

        // Recreate buffer if width changed
        if (screenValues == null || screenValues.length != width) {
            screenValues = new double[width];
            sweepPos = 0;
        }

        render();
    }

    private void render() {

        int width = (int) Math.floor(getWidth());
        int height = (int) Math.floor(getHeight());
        if (width <= 0 || height <= 0) {
            return;
        }

        GraphicsContext g = getGraphicsContext2D();

        // Clear background
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, width, height);

        // Draw small grid lines
        g.setStroke(Color.rgb(40, 40, 40));
        int smallStep = 10;
        for (int x = 0; x < width; x += smallStep) {
            g.strokeLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += smallStep) {
            g.strokeLine(0, y, width, y);
        }

        // Draw large grid lines
        g.setStroke(Color.rgb(70, 70, 70));
        int bigStep = 50;
        for (int x = 0; x < width; x += bigStep) {
            g.strokeLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += bigStep) {
            g.strokeLine(0, y, width, y);
        }

        if (screenValues == null || screenValues.length < 2) {
            return;
        }

        // ECG baseline (middle of screen)
        int baselineY = height / 2;

        // Convert millivolts to pixels
        double yScale = (height * 0.4) / CALIBRATION_MAX_MV;

        // Draw baseline reference line
        g.setStroke(Color.rgb(120, 120, 120));
        g.strokeLine(0, baselineY, width, baselineY);

        // Draw ECG waveform
        g.setStroke(Color.rgb(0, 255, 80));
        g.beginPath();

        double y0 = baselineY - (screenValues[0] * yScale);
        g.moveTo(0, y0);

        for (int x = 1; x < screenValues.length; x++) {
            double y = baselineY - (screenValues[x] * yScale);
            g.lineTo(x, y);
        }

        g.stroke();

        // Draw sweep gap (simulates moving cursor)
        int gapWidth = 6;
        int gapX = sweepPos;

        g.setFill(Color.BLACK);
        g.fillRect(gapX, 0, gapWidth, height);

        // Draw cursor line
        int cursorX = gapX + gapWidth / 2;
        g.setStroke(Color.YELLOW);
        g.strokeLine(cursorX, 0, cursorX, height);
    }
}
