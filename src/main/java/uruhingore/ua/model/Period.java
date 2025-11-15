package uruhingore.ua.model;

public enum Period {
    PERIOD_1("Period 1"),
    PERIOD_2("Period 2"),
    PERIOD_3("Period 3"),
    FINAL_SEMESTER("Final Semester");

    private final String displayName;

    Period(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

