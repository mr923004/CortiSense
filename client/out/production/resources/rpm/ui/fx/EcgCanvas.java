package rpm.ui.fx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class EcgCanvas extends Canvas {
    private static final double CALIBRATION_MAX_MV = 1.5;

    private double[] screenValues;
    private int sweepPos;

    public EcgCanvas() {
        widthProperty().addListener((obs, oldV, newV) -> resizeBuffer());
        heightProperty().addListener((obs, oldV, newV) -> render());
    }

    public void reset() {
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

        if (screenValues == null || screenValues.length != width) {
            screenValues = new double[width];
            sweepPos = 0;
        }

        for (double v : samples) {
            screenValues[sweepPos] = v;
            sweepPos++;
            if (sweepPos >= screenValues.length) {
                sweepPos = 0;
            }
        }

        render();
    }

    private void resizeBuffer() {
        int width = (int) Math.floor(getWidth());
        if (width <= 0) {
            screenValues = null;
            sweepPos = 0;
            return;
        }

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

        g.setFill(Color.BLACK);
        g.fillRect(0, 0, width, height);

        g.setStroke(Color.rgb(40, 40, 40));
        int smallStep = 10;
        for (int x = 0; x < width; x += smallStep) {
            g.strokeLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += smallStep) {
            g.strokeLine(0, y, width, y);
        }

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

        int baselineY = height / 2;
        double yScale = (height * 0.4) / CALIBRATION_MAX_MV;

        g.setStroke(Color.rgb(120, 120, 120));
        g.strokeLine(0, baselineY, width, baselineY);

        g.setStroke(Color.rgb(0, 255, 80));
        g.beginPath();

        double y0 = baselineY - (screenValues[0] * yScale);
        g.moveTo(0, y0);

        for (int x = 1; x < screenValues.length; x++) {
            double y = baselineY - (screenValues[x] * yScale);
            g.lineTo(x, y);
        }

        g.stroke();

        int gapWidth = 6;
        int gapX = sweepPos;

        g.setFill(Color.BLACK);
        g.fillRect(gapX, 0, gapWidth, height);

        int cursorX = gapX + gapWidth / 2;
        g.setStroke(Color.YELLOW);
        g.strokeLine(cursorX, 0, cursorX, height);
    }
}
