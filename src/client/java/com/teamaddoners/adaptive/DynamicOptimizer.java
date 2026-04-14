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
 */
public final class DynamicOptimizer {

    private final SystemMonitor systemMonitor;

    // 🔥 NEW: shader awareness
    private ShaderDetector shaderDetector;

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

        // 🔥 Shader safety
        if (shadersEnabled) {
            applyShaderSafety(settings);
        }

        LoggerUtil.debug(
                "Fallback optimization | level={} | shader={}",
                level,
                shadersEnabled
        );

        ProfileApplier.applyMap(settings, "fallback[" + level + "]");
    }

    // ── Shader Safety Layer ──────────────────────────────────────────────────────

    /**
     * Prevent overly aggressive settings when shaders are active.
     */
    private void applyShaderSafety(Map<String, Object> settings) {
        // Prevent ultra-low render distance with shaders
        if (settings.containsKey("renderDistance")) {
            int rd = (int) settings.get("renderDistance");
            settings.put("renderDistance", Math.max(6, rd));
        }

        // Avoid disabling smooth lighting (visual impact too high with shaders)
        settings.put("smoothLighting", true);
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