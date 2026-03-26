package rpm.ui.patient.widgets;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.domain.VitalType;
import rpm.ui.app.AppContext;
import rpm.ui.bindings.VitalDisplay;

import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class HistorySearchPanel extends VBox {

    // Controls used to select the target date and time
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final Spinner<Integer> hour = new Spinner<>(0, 23, LocalTime.now().getHour());
    private final Spinner<Integer> minute = new Spinner<>(0, 59, LocalTime.now().getMinute());
    private final Spinner<Integer> second = new Spinner<>(0, 59, LocalTime.now().getSecond());

    // Displays the fetched history result
    private final Label result = new Label("History result: (none)");

    public HistorySearchPanel(AppContext ctx, PatientId patientId) {

        getStyleClass().add("panel-card");
        setSpacing(8);
        setPadding(new Insets(12));

        // Allow the panel height to resize naturally based on its content
        setMinHeight(Region.USE_COMPUTED_SIZE);
        setPrefHeight(Region.USE_COMPUTED_SIZE);
        setMaxHeight(Region.USE_COMPUTED_SIZE);

        result.setWrapText(true);
        result.setMaxWidth(Double.MAX_VALUE);

        Button fetch = new Button("Fetch snapshot");
        fetch.getStyleClass().add("btn-soft");
        fetch.setMaxWidth(Double.MAX_VALUE);

        // Make input controls stretch horizontally
        datePicker.setMaxWidth(Double.MAX_VALUE);
        hour.setMaxWidth(Double.MAX_VALUE);
        minute.setMaxWidth(Double.MAX_VALUE);
        second.setMaxWidth(Double.MAX_VALUE);

        fetch.setOnAction(e -> {

            // Convert selected date and time into an Instant
            Instant target = toInstant();

            // Search a small window around the target time
            Instant from = target.minusSeconds(120);
            Instant to = target.plusSeconds(120);
            List<VitalSnapshot> snaps = ctx.store.getVitals(patientId, from, to);

            // Find the snapshot closest in time to the target
            VitalSnapshot nearest = snaps.stream()
                    .min(Comparator.comparingLong(
                            s -> Math.abs(
                                    s.getTimestamp().toEpochMilli() -
                                            target.toEpochMilli()
                            )
                    ))
                    .orElse(null);

            if (nearest == null) {
                result.setText("History result: no data in memory window.");
            } else {
                Map<VitalType, Double> v = nearest.getValues();

                // Format vitals for display
                result.setText(
                        "At ~" + nearest.getTimestamp() +
                                " | HR " + VitalDisplay.fmt1(get(v, VitalType.HEART_RATE)) +
                                " | RR " + VitalDisplay.fmt1(get(v, VitalType.RESP_RATE)) +
                                " | BP " + VitalDisplay.fmt0(get(v, VitalType.BP_SYSTOLIC)) +
                                "/" + VitalDisplay.fmt0(get(v, VitalType.BP_DIASTOLIC)) +
                                " | Temp " + VitalDisplay.fmt1(get(v, VitalType.TEMPERATURE))
                );
            }

            // Trigger layout update after text size changes
            requestLayout();
        });

        // Time selection row
        HBox timeRow = new HBox(10, hour, minute, second);
        HBox.setHgrow(hour, Priority.ALWAYS);
        HBox.setHgrow(minute, Priority.ALWAYS);
        HBox.setHgrow(second, Priority.ALWAYS);

        getChildren().addAll(
                new Label("History lookup (date/time):"),
                datePicker,
                new Label("Time (H:M:S):"),
                timeRow,
                fetch,
                result
        );
    }

    // Convert the selected date and time fields into an Instant
    private Instant toInstant() {
        LocalDate d = datePicker.getValue();
        LocalTime t = LocalTime.of(hour.getValue(), minute.getValue(), second.getValue());
        return ZonedDateTime.of(d, t, ZoneId.systemDefault()).toInstant();
    }

    // Safe lookup of a vital value from the snapshot map
    private static double get(Map<VitalType, Double> m, VitalType t) {
        Double x = m.get(t);
        return x == null ? Double.NaN : x;
    }
}
