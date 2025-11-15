package uruhingore.ua.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    /**
     * Serialize enum as its name (NURSERY_1, NURSERY_2, etc.) for JSON
     */
    @JsonValue
    public String toJsonValue() {
        return name();
    }

    /**
     * Custom deserializer that handles both enum names and display names
     * Also handles variations like "Nursery 1" (with space) -> "Nursery-1"
     */
    @JsonCreator
    public static ClassLevel fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = value.trim();
        
        // First, try direct enum name match (case insensitive)
        try {
            String upperValue = trimmed.toUpperCase().replace("-", "_").replace(" ", "_");
            return ClassLevel.valueOf(upperValue);
        } catch (IllegalArgumentException e) {
            // Continue to try other formats
        }
        
        // Normalize the input: replace spaces with hyphens
        String normalized = trimmed.replace(" ", "-").replace("_", "-");
        
        // Try to match by display name (case insensitive)
        for (ClassLevel level : ClassLevel.values()) {
            if (level.displayName.equalsIgnoreCase(normalized) || 
                level.displayName.equalsIgnoreCase(trimmed)) {
                return level;
            }
        }
        
        // Try matching enum name with normalized format
        String upperNormalized = normalized.toUpperCase();
        for (ClassLevel level : ClassLevel.values()) {
            String enumNameAsHyphen = level.name().replace("_", "-");
            if (enumNameAsHyphen.equals(upperNormalized)) {
                return level;
            }
        }
        
        throw new IllegalArgumentException("Invalid ClassLevel value: " + value + 
                ". Valid values are: NURSERY_1, NURSERY_2, NURSERY_3, PRE_PRIMARY or their display names (Nursery-1, Nursery-2, etc.).");
    }
}

