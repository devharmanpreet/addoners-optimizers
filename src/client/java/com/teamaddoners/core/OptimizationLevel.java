package com.teamaddoners.core;

/**
 * Represents the current optimization intensity level applied by the engine.
 * Each level maps to a set of game-setting adjustments designed to recover FPS.
 */
public enum OptimizationLevel {

    /**
     * Minimal intervention. Applied when FPS is healthy (>= 80).
     * The profile's base settings are used with no aggressive overrides.
     */
    LOW("Low", "Minimal optimization — FPS is healthy."),

    /**
     * Moderate optimization. Applied when FPS is acceptable but below ideal (50–79).
     * Slight reductions to render distance and particle density.
     */
    MEDIUM("Medium", "Moderate optimization — applying performance adjustments."),

    /**
     * Maximum optimization. Applied when FPS is critically low (< 50).
     * Aggressively reduces render distance, disables particles, and may disable shaders.
     */
    AGGRESSIVE("Aggressive", "Aggressive optimization — FPS critically low.");

    private final String displayName;
    private final String description;

    OptimizationLevel(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Resolves the correct optimization level for a given FPS reading.
     *
     * @param fps Current measured frames per second.
     * @return The corresponding OptimizationLevel.
     */
    public static OptimizationLevel fromFps(int fps) {
        if (fps >= 80) return LOW;
        if (fps >= 50) return MEDIUM;
        return AGGRESSIVE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
