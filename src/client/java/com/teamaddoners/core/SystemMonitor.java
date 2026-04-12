package com.teamaddoners.core;

/**
 * Detects system hardware capabilities and classifies the machine into a performance tier.
 * This information is used by the optimizer to calibrate default settings.
 */
public final class SystemMonitor {

    /**
     * Hardware classification tier.
     */
    public enum Tier {
        LOW_END("Low-End", "≤4 GB RAM or ≤2 CPU cores"),
        MID_END("Mid-End", "4–8 GB RAM and 3–7 CPU cores"),
        HIGH_END("High-End", ">8 GB RAM and ≥8 CPU cores");

        private final String displayName;
        private final String description;

        Tier(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }

        @Override
        public String toString() { return displayName; }
    }

    // ── Hardware Properties (read once at startup) ────────────────────────────────

    private final long maxRamMb;
    private final int cpuCores;
    private final Tier tier;

    public SystemMonitor() {
        Runtime rt = Runtime.getRuntime();
        this.maxRamMb = rt.maxMemory() / (1024L * 1024L);
        this.cpuCores = rt.availableProcessors();
        this.tier = classify(maxRamMb, cpuCores);
    }

    /**
     * Classifies the system based on RAM and CPU cores.
     */
    private static Tier classify(long ramMb, int cores) {
        if (ramMb > 8192 && cores >= 8) return Tier.HIGH_END;
        if (ramMb >= 4096 && cores >= 3) return Tier.MID_END;
        return Tier.LOW_END;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────────

    /** Maximum JVM heap memory in megabytes. */
    public long getMaxRamMb() { return maxRamMb; }

    /** Number of logical CPU cores available to the JVM. */
    public int getCpuCores() { return cpuCores; }

    /** System performance classification. */
    public Tier getTier() { return tier; }

    /**
     * Returns the recommended default render distance for the detected tier.
     */
    public int getRecommendedRenderDistance() {
        return switch (tier) {
            case HIGH_END -> 16;
            case MID_END  -> 10;
            case LOW_END  -> 6;
        };
    }

    @Override
    public String toString() {
        return String.format("SystemMonitor[tier=%s, ram=%dMB, cores=%d]", tier, maxRamMb, cpuCores);
    }
}
