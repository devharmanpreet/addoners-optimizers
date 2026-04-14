package com.teamaddoners.adaptive;

import com.teamaddoners.core.OptimizationLevel;
import com.teamaddoners.core.SystemMonitor;
import com.teamaddoners.profile.AddonersProfile;
import com.teamaddoners.profile.ProfileApplier;
import com.teamaddoners.shader.ShaderDetector;
import com.teamaddoners.util.LoggerUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Applies real-time game adjustments based on OptimizationLevel
 * and active AddonersProfile.
 * 
 * <p>v2.0 - Smooth Transitions:
 * - Tracks last applied render distance
 * - Gradually transitions render distance instead of jumping
 * - Improves chunk loading smoothness
 */
public final class DynamicOptimizer {

    private final SystemMonitor systemMonitor;

    // 🔥 NEW: shader awareness
    private ShaderDetector shaderDetector;

    // 🔥 NEW: Smooth render distance transitions
    private int lastAppliedRenderDistance = 16; // default

    public DynamicOptimizer(SystemMonitor systemMonitor) {
        this.systemMonitor = systemMonitor;
    }

    // 🔥 Inject shader detector (set from OptimizerMod)
    public void setShaderDetector(ShaderDetector shaderDetector) {
        this.shaderDetector = shaderDetector;
    }

    public void apply(OptimizationLevel level, AddonersProfile profile, int fps) {
        boolean shadersEnabled = isShaderEnabled();

        if (profile != null) {
            applyWithProfile(level, profile, fps, shadersEnabled);
        } else {
            applyFallback(level, shadersEnabled);
        }
    }

    // ── Profile-based path ────────────────────────────────────────────────────────

    private void applyWithProfile(
            OptimizationLevel level,
            AddonersProfile profile,
            int fps,
            boolean shadersEnabled
    ) {
        // 🔹 Base settings (profile first — highest priority)
        Map<String, Object> merged = new HashMap<>(profile.getSettings());

        // 🔹 Rule overrides
        Map<String, Object> ruleOverrides = RuleEngine.evaluate(profile, fps);
        merged.putAll(ruleOverrides);

        // 🔥 Apply smooth render distance transition (safe conversion)
        if (merged.containsKey("renderDistance")) {
            int targetRd = toInt(merged.get("renderDistance"), 12);
            int smoothedRd = smoothRenderDistance(targetRd);
            merged.put("renderDistance", smoothedRd);
        }

        // 🔥 Shader awareness adjustment
        if (shadersEnabled) {
            applyShaderSafety(merged);
        }

        LoggerUtil.debug(
                "Profile optimization | level={} | fps={} | rules={} | shader={}",
                level,
                fps,
                ruleOverrides.size(),
                shadersEnabled
        );

        ProfileApplier.applyMap(merged, profile.getName() + "+rules");
    }

    // ── Fallback path ────────────────────────────────────────────────────────────

    private void applyFallback(OptimizationLevel level, boolean shadersEnabled) {
        int baseRd = systemMonitor.getRecommendedRenderDistance();
        Map<String, Object> settings = new HashMap<>();

        int targetRenderDistance;

        switch (level) {
            case LOW -> {
                targetRenderDistance = baseRd;
                settings.put("particles", 2);
                settings.put("smoothLighting", true);
            }
            case MEDIUM -> {
                targetRenderDistance = Math.max(4, baseRd - 4);
                settings.put("particles", 1);
                settings.put("smoothLighting", true);
            }
            case AGGRESSIVE -> {
                targetRenderDistance = Math.max(2, baseRd - 8);
                settings.put("particles", 0);
                settings.put("smoothLighting", false);
            }
            default -> targetRenderDistance = baseRd;
        }

        // 🔥 Apply smooth render distance transition
        int smoothedRd = smoothRenderDistance(targetRenderDistance);
        settings.put("renderDistance", smoothedRd);

        // 🔥 Shader safety
        if (shadersEnabled) {
            applyShaderSafety(settings);
        }

        LoggerUtil.debug(
                "Fallback optimization | level={} | targetRd={} | smoothedRd={} | shader={}",
                level,
                targetRenderDistance,
                smoothedRd,
                shadersEnabled
        );

        ProfileApplier.applyMap(settings, "fallback[" + level + "]");
    }

    // ── Smooth Render Distance Transition ──────────────────────────────────────────

    /**
     * Gradually transitions render distance to prevent massive chunk reloads.
     * 
     * Example: 10 → 2 transitions as: 10 → 8 → 6 → 4 → 2
     * 
     * @param targetRd Target render distance
     * @return Smoothed render distance (stepped towards target)
     */
    private int smoothRenderDistance(int targetRd) {
        if (targetRd == lastAppliedRenderDistance) {
            return targetRd; // No change needed
        }

        // Step size: move by 2 chunks per optimization cycle (~1 second)
        // This prevents jarring visual transitions and chunk load spikes
        final int STEP_SIZE = 2;

        int current = lastAppliedRenderDistance;
        
        if (current < targetRd) {
            // Increasing render distance: step up
            int stepped = Math.min(current + STEP_SIZE, targetRd);
            lastAppliedRenderDistance = stepped;
            return stepped;
        } else if (current > targetRd) {
            // Decreasing render distance: step down
            int stepped = Math.max(current - STEP_SIZE, targetRd);
            lastAppliedRenderDistance = stepped;
            return stepped;
        }

        return current;
    }

    // ── Shader Safety Layer ──────────────────────────────────────────────────────

    /**
     * Prevent overly aggressive settings when shaders are active.
     */
    private void applyShaderSafety(Map<String, Object> settings) {
        // Prevent ultra-low render distance with shaders (safe conversion)
        if (settings.containsKey("renderDistance")) {
            int rd = toInt(settings.get("renderDistance"), 6);
            settings.put("renderDistance", Math.max(6, rd));
        }

        // Avoid disabling smooth lighting (visual impact too high with shaders)
        settings.put("smoothLighting", true);
    }

    // ── Safe Numeric Conversion (Gson-safe) ──────────────────────────────────────

    /**
     * Safely converts any Object to int, handling Number types from JSON parsing.
     * Gson parses numeric values as Double, not Integer, so direct casting fails.
     *
     * @param value     The value to convert (may be Double, Integer, String, etc.)
     * @param fallback  The default value if conversion fails
     * @return          Converted int value or fallback
     */
    private int toInt(Object value, int fallback) {
        if (value instanceof Number n) {
            return n.intValue();
        }

        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
            }
        }

        if (value == null) {
            return fallback;
        }

        LoggerUtil.debug("Could not convert value {} to int, using fallback {}", value, fallback);
        return fallback;
    }

    /**
     * Safely converts any Object to boolean, handling various input types.
     *
     * @param value     The value to convert
     * @param fallback  The default value if conversion fails
     * @return          Converted boolean value or fallback
     */
    private boolean toBool(Object value, boolean fallback) {
        if (value instanceof Boolean b) {
            return b;
        }

        if (value instanceof Number n) {
            return n.intValue() != 0;
        }

        if (value instanceof String s) {
            String lower = s.toLowerCase();
            if (lower.equals("true") || lower.equals("1") || lower.equals("yes")) {
                return true;
            }
            if (lower.equals("false") || lower.equals("0") || lower.equals("no")) {
                return false;
            }
        }

        return fallback;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private boolean isShaderEnabled() {
        if (shaderDetector == null) return false;

        try {
            String shader = shaderDetector.detect();
            return shader != null && !shader.equalsIgnoreCase("none");
        } catch (Exception e) {
            return false;
        }
    }
}