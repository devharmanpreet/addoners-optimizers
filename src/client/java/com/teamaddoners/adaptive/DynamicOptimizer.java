package com.teamaddoners.adaptive;

import com.teamaddoners.core.OptimizationLevel;
import com.teamaddoners.core.SystemMonitor;
import com.teamaddoners.profile.AddonersProfile;
import com.teamaddoners.profile.ProfileApplier;
import com.teamaddoners.util.LoggerUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Applies real-time game adjustments based on the current {@link OptimizationLevel}
 * and the active {@link AddonersProfile}.
 *
 * <p>The application strategy:
 * <ol>
 *   <li>Start from the profile's base settings.</li>
 *   <li>Merge in rule-engine overrides for the current FPS.</li>
 *   <li>If no profile is loaded, apply built-in level-specific fallback settings.</li>
 *   <li>Delegate the final merged map to {@link ProfileApplier}.</li>
 * </ol>
 */
public final class DynamicOptimizer {

    private final SystemMonitor systemMonitor;

    public DynamicOptimizer(SystemMonitor systemMonitor) {
        this.systemMonitor = systemMonitor;
    }

    /**
     * Applies optimization for the given level and FPS, using the active profile's rules.
     *
     * @param level   Current optimization level (resolved from FPS).
     * @param profile Currently active profile (may be null).
     * @param fps     Current FPS reading (used for rule evaluation).
     */
    public void apply(OptimizationLevel level, AddonersProfile profile, int fps) {
        if (profile != null) {
            applyWithProfile(level, profile, fps);
        } else {
            applyFallback(level);
        }
    }

    // ── Profile-based path ────────────────────────────────────────────────────────

    private void applyWithProfile(OptimizationLevel level, AddonersProfile profile, int fps) {
        // Start with profile base settings
        Map<String, Object> merged = new HashMap<>(profile.getSettings());

        // Layer rule engine overrides
        Map<String, Object> ruleOverrides = RuleEngine.evaluate(profile, fps);
        merged.putAll(ruleOverrides);

        LoggerUtil.debug("Applying optimization level={} overrides={}", level, ruleOverrides.size());
        ProfileApplier.applyMap(merged, profile.getName() + "+rules");
    }

    // ── Fallback path (no profile loaded) ────────────────────────────────────────

    private void applyFallback(OptimizationLevel level) {
        int baseRd = systemMonitor.getRecommendedRenderDistance();
        Map<String, Object> settings = new HashMap<>();

        switch (level) {
            case LOW -> {
                settings.put("renderDistance", baseRd);
                settings.put("particles", 2);
                settings.put("smoothLighting", true);
            }
            case MEDIUM -> {
                settings.put("renderDistance", Math.max(4, baseRd - 4));
                settings.put("particles", 1);
                settings.put("smoothLighting", true);
            }
            case AGGRESSIVE -> {
                settings.put("renderDistance", Math.max(2, baseRd - 8));
                settings.put("particles", 0);
                settings.put("smoothLighting", false);
            }
        }

        LoggerUtil.debug("Applying fallback optimization: level={}", level);
        ProfileApplier.applyMap(settings, "fallback[" + level + "]");
    }
}
