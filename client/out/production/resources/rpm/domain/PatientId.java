package rpm.domain;

import java.util.Objects;

public final class PatientId {
    private final int value;

    public PatientId(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("PatientId must be positive");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return String.format("Bed %02d", value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatientId)) return false;
        PatientId patientId = (PatientId) o;
        return value == patientId.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
