package com.teamaddoners.core;

import com.teamaddoners.util.LoggerUtil;

/**
 * Cooldown system to prevent rapid optimization switching.
 * Maintains stability by enforcing minimum time between optimization changes.
 *
 * <p>Design: This is intentionally server-compatible (no client-only APIs).
 */
public final class CooldownSystem {

    private static final long COOLDOWN_MS = 3_000L; // 3 seconds

    private long lastAppliedTimestamp = 0L;
    private OptimizationLevel lastAppliedLevel = OptimizationLevel.LOW;

    /**
     * Checks if enough time has passed since the last optimization was applied.
     *
     * @return true if cooldown has expired and optimization can be applied
     */
    public boolean isCooldownExpired() {
        long now = System.currentTimeMillis();
        return (now - lastAppliedTimestamp) >= COOLDOWN_MS;
    }

    /**
     * Records that an optimization was applied at this moment.
     */
    public void recordApplication(OptimizationLevel level) {
        lastAppliedTimestamp = System.currentTimeMillis();
        lastAppliedLevel = level;
        LoggerUtil.debug("Optimization applied | level={} | cooldown_ms={}", level, COOLDOWN_MS);
    }

    /**
     * Checks if applying a new level would represent a meaningful change.
     * Avoids re-applying the same level.
     */
    public boolean shouldApply(OptimizationLevel level) {
        return !level.equals(lastAppliedLevel);
    }

    public long getTimeRemainingMs() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastAppliedTimestamp;
        return Math.max(0L, COOLDOWN_MS - elapsed);
    }

    public OptimizationLevel getLastAppliedLevel() {
        return lastAppliedLevel;
    }

    /** Resets the cooldown system. Used for testing. */
    public void reset() {
        lastAppliedTimestamp = 0L;
        lastAppliedLevel = OptimizationLevel.LOW;
    }
}
