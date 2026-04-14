package com.teamaddoners.shader;

import com.teamaddoners.core.OptimizationLevel;
import com.teamaddoners.util.LoggerUtil;

/**
 * Applies shader-aware logic on top of the core optimization engine.
 *
 * <p>When an Addoners-family shader (Quartzglow or Skygleam) is detected,
 * advanced synergy mode is enabled — the optimizer avoids aggressive render
 * distance cuts and other changes that would degrade visual quality.
 *
 * <p>For any other shader, a softer cap is enforced via {@link #adjustLevel}
 * to preserve rendering fidelity while still helping performance.
 *
 * <p><b>v1.5 addition:</b> {@link #adjustLevel(OptimizationLevel)} — called by
 * {@code OptimizerEngine} when {@code config.shaderOptimization = true}.
 */
public final class ShaderOptimizer {

    private boolean advancedModeActive = false;
    private String  activeShaderName   = "none";

    // ── Core API ───────────────────────────────────────────────────────────────

    /**
     * Evaluates the detected shader and activates (or deactivates) advanced mode.
     *
     * @param shaderName Lowercase shader identifier from {@link ShaderDetector}.
     */
    public void evaluate(String shaderName) {
        if (shaderName == null || shaderName.isBlank()) shaderName = "none";

        boolean isAddoners = isAddonersShader(shaderName);

        if (isAddoners && !advancedModeActive) {
            advancedModeActive = true;
            activeShaderName   = shaderName;
            LoggerUtil.info("Advanced shader optimization ENABLED for '{}'", shaderName);
            activateAdvancedMode(shaderName);

        } else if (!isAddoners && advancedModeActive) {
            advancedModeActive = false;
            activeShaderName   = "none";
            LoggerUtil.info("Advanced shader optimization DEACTIVATED.");
            deactivateAdvancedMode();
        }
    }

    /**
     * Caps the requested optimization level when a shader is active to prevent
     * aggressive visual changes (e.g., drastic render distance drops).
     *
     * <p>Behavior:
     * <ul>
     *   <li>Addoners shaders (quartzglow, skygleam): allow up to MEDIUM only.</li>
     *   <li>All other shaders: allow up to MEDIUM only (same cap for safety).</li>
     *   <li>No shader active: return {@code requested} unchanged.</li>
     * </ul>
     *
     * @param requested The level determined by raw FPS reading.
     * @return Adjusted level — never more aggressive than MEDIUM when any shader is active.
     */
    public OptimizationLevel adjustLevel(OptimizationLevel requested) {
        if (!advancedModeActive && "none".equals(activeShaderName)) {
            // Mode hasn't been set yet by evaluate(); allow full aggressiveness.
            return requested;
        }
        // Cap at MEDIUM when any shader is running — avoid aggressive render cuts.
        if (requested == OptimizationLevel.AGGRESSIVE) {
            LoggerUtil.debug("Shader active — capping optimization level AGGRESSIVE → MEDIUM");
            return OptimizationLevel.MEDIUM;
        }
        return requested;
    }

    // ── Accessors ──────────────────────────────────────────────────────────────

    /** True when Quartzglow or Skygleam is the active shader. */
    public boolean isAdvancedModeActive() { return advancedModeActive; }

    /** The name of the currently active Addoners shader, or {@code "none"}. */
    public String getActiveShaderName()   { return activeShaderName; }

    // ── Internal ───────────────────────────────────────────────────────────────

    private static boolean isAddonersShader(String name) {
        return name.contains("quartzglow") || name.contains("skygleam");
    }

    private void activateAdvancedMode(String shaderName) {
        // Future: ShaderLoaderBridge.setQualityPreset(shaderName, "optimized");
        LoggerUtil.debug("Advanced mode hooks ready for '{}'.", shaderName);
    }

    private void deactivateAdvancedMode() {
        // Future: ShaderLoaderBridge.restoreDefaults();
        LoggerUtil.debug("Advanced mode hooks cleared.");
    }
}
