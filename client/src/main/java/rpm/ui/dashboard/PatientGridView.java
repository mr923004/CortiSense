package rpm.ui.dashboard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

import java.util.List;
import java.util.function.Consumer;

// Container to display paginated grid of patient cards. Handle geometry of
// the grid and bottom page number bar
public final class PatientGridView extends BorderPane {

    private static final int MIN_PER_SCREEN = 1;

    private final GridPane grid = new GridPane();
    private final ScrollPane scroll = new ScrollPane(grid);

    private final Button prevBtn = new Button("Prev");
    private final Button nextBtn = new Button("Next");
    private final Label pageLabel = new Label();

    private Consumer<rpm.domain.PatientId> onPatientClicked = id -> {};
    private Consumer<rpm.domain.PatientId> onResolve = id -> {};

    private Runnable onNextPage = () -> {};
    private Runnable onPrevPage = () -> {};

    private int columns = 2;
    private int rows = 2;
    private int capacity = 4;

    public PatientGridView() {
        getStyleClass().add("patient-grid");

        grid.getStyleClass().add("patient-grid-inner");
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPadding(new Insets(18));

        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(false);
        scroll.setStyle("-fx-background-color: transparent;");
        grid.setStyle("-fx-background-color: transparent;");

        setCenter(scroll);
        setBottom(buildPager());

        int[] cr = layoutFor(capacity);
        columns = cr[0];
        rows = cr[1];
        applyGridConstraints(columns, rows);
    }

    // Construct bottom navigation bar (Prev | Page Num/Total | Next)
    private HBox buildPager() {
        prevBtn.getStyleClass().add("pager-btn");
        nextBtn.getStyleClass().add("pager-btn");
        pageLabel.getStyleClass().add("pager-label");

        // Link buttons to actions
        prevBtn.setOnAction(e -> onPrevPage.run());
        nextBtn.setOnAction(e -> onNextPage.run());

        Region spacerL = new Region();
        Region spacerR = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS);
        HBox.setHgrow(spacerR, Priority.ALWAYS);

        HBox pager = new HBox(12, spacerL, prevBtn, pageLabel, nextBtn, spacerR);
        pager.getStyleClass().add("pager");
        pager.setAlignment(Pos.CENTER);
        pager.setPadding(new Insets(10, 12, 14, 12));
        return pager;
    }

    public void setOnPatientClicked(Consumer<rpm.domain.PatientId> handler) {
        this.onPatientClicked = (handler == null) ? id -> {} : handler;
    }

    public void setOnResolve(Consumer<rpm.domain.PatientId> handler) {
        this.onResolve = (handler == null) ? id -> {} : handler;
    }

    public void setOnNextPage(Runnable r) {
        onNextPage = (r == null) ? () -> {} : r;
    }

    public void setOnPrevPage(Runnable r) {
        onPrevPage = (r == null) ? () -> {} : r;
    }

    public void fireNextPage() {
        onNextPage.run();
    }

    public void firePrevPage() {
        onPrevPage.run();
    }

    public void setTiles(List<PatientTileModel> tiles, int pageIndex, int pageCount, boolean showResolve) {
        int perScreenGuess = (tiles == null) ? 4 : Math.max(MIN_PER_SCREEN, tiles.size());
        setTiles(tiles, pageIndex, pageCount, perScreenGuess, showResolve);
    }

    // Main render method. Clears grid and rebuild with provided patient tiles
    public void setTiles(List<PatientTileModel> tiles, int pageIndex, int pageCount, int perScreen, boolean showResolve) {
        capacity = Math.max(MIN_PER_SCREEN, perScreen);

        int[] cr = layoutFor(capacity);
        if (cr[0] != columns || cr[1] != rows) {
            columns = cr[0];
            rows = cr[1];
            applyGridConstraints(columns, rows);
        }

        grid.getChildren().clear();

        // Populate grid with patients
        if (tiles != null && !tiles.isEmpty()) {
            int c = 0, r = 0;
            for (PatientTileModel t : tiles) {
                PatientCardView card = new PatientCardView(t);
                card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                GridPane.setHgrow(card, Priority.ALWAYS);
                GridPane.setVgrow(card, Priority.ALWAYS);

                card.setOnMouseClicked(e -> onPatientClicked.accept(t.id));
                card.setOnResolve(() -> onResolve.accept(t.id));

                grid.add(card, c, r);

                c++;
                if (c >= columns) { c = 0; r++; }
                if (r >= rows) break;
            }

            // Fill remaining slops with invisible placeholders
            int filled = Math.min(tiles.size(), capacity);

            if (capacity > 1) {
                while (filled < capacity) {
                    int pc = filled % columns;
                    int pr = filled / columns;
                    if (pr >= rows) break;

                    Region ph = new Region();
                    ph.getStyleClass().add("patient-card-placeholder");
                    ph.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    GridPane.setHgrow(ph, Priority.ALWAYS);
                    GridPane.setVgrow(ph, Priority.ALWAYS);

                    grid.add(ph, pc, pr);
                    filled++;
                }
            }
        }

        // Update text and button states
        int current = pageIndex + 1;
        int total = Math.max(1, pageCount);
        pageLabel.setText("Page " + current + " / " + total);
        prevBtn.setDisable(pageIndex <= 0);
        nextBtn.setDisable(pageIndex >= total - 1);

        scroll.setVvalue(0);
    }

    // Determine optimal grid layout
    private static int[] layoutFor(int capacity) {
        int cols;
        int rows;

        if (capacity <= 1) { cols = 1; rows = 1; }
        else if (capacity == 2) { cols = 2; rows = 1; }
        else if (capacity <= 4) { cols = 2; rows = 2; }
        else if (capacity <= 6) { cols = 3; rows = 2; }
        else if (capacity <= 9) { cols = 3; rows = 3; }
        else { cols = 4; rows = (int) Math.ceil(capacity / 4.0); }

        return new int[]{ cols, rows };
    }

    // Apply JavaFX constraints to split grid space evenly
    private void applyGridConstraints(int cols, int rows) {
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        double colPercent = 100.0 / cols;
        double rowPercent = 100.0 / rows;

        for (int i = 0; i < cols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(colPercent);
            col.setFillWidth(true);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        for (int i = 0; i < rows; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(rowPercent);
            row.setFillHeight(true);
            row.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(row);
        }
    }
}
