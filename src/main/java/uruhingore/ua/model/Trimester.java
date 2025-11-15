package uruhingore.ua.model;

public enum Trimester {
    FIRST(1, "First Trimester"),
    SECOND(2, "Second Trimester"),
    THIRD(3, "Third Trimester");

    private final int value;
    private final String displayName;

    Trimester(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Trimester fromValue(int value) {
        for (Trimester trimester : Trimester.values()) {
            if (trimester.getValue() == value) {
                return trimester;
            }
        }
        throw new IllegalArgumentException("Invalid trimester value: " + value);
    }
}

