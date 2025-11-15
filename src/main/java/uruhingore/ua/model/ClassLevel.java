package uruhingore.ua.model;

public enum ClassLevel {
    NURSERY_1("Nursery-1"),
    NURSERY_2("Nursery-2"),
    NURSERY_3("Nursery-3"),
    PRE_PRIMARY("Pre-Primary");

    private final String displayName;

    ClassLevel(String displayName) {
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

