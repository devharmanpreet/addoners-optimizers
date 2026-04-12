package com.teamaddoners.shader;

import com.teamaddoners.util.LoggerUtil;

/**
 * Applies advanced optimization when an Addoners-family shader (Quartzglow or Skygleam)
 * is detected, enabling specialized render path adjustments.
 *
 * <p>In the current implementation this manages a flag and log state.
 * Future versions will interface with the shader loader's extension API
 * to set shader-specific uniforms and quality presets.
 */
public final class ShaderOptimizer {

    private boolean advancedModeActive = false;
    private String activeShaderName = "none";

    /**
     * Evaluates the detected shader and activates (or deactivates) advanced mode.
     *
     * @param shaderName Lowercase shader name from {@link ShaderDetector}.
     */
    public void evaluate(String shaderName) {
        if (shaderName == null || shaderName.isBlank()) {
            shaderName = "none";
        }

        boolean isAddoners = isAddonersShader(shaderName);

        if (isAddoners && !advancedModeActive) {
            advancedModeActive = true;
            activeShaderName = shaderName;
            LoggerUtil.info("Advanced shader optimization ENABLED for '{}'", shaderName);
            activateAdvancedMode(shaderName);

        } else if (!isAddoners && advancedModeActive) {
            advancedModeActive = false;
            activeShaderName = "none";
            LoggerUtil.info("Advanced shader optimization DEACTIVATED.");
            deactivateAdvancedMode();
        }
    }

    /**
     * Returns true when Quartzglow or Skygleam is the active shader.
     */
    public boolean isAdvancedModeActive() {
        return advancedModeActive;
    }

    public String getActiveShaderName() {
        return activeShaderName;
    }

    // ── Internal ──────────────────────────────────────────────────────────────────

    private static boolean isAddonersShader(String name) {
        return name.contains("quartzglow") || name.contains("skygleam");
    }

    /**
     * Hook: activate shader-specific optimizations.
     * Future: inject shader uniforms, switch render quality presets, etc.
     */
    private void activateAdvancedMode(String shaderName) {
        // Future: ShaderLoaderBridge.setQualityPreset(shaderName, "optimized");
        // Future: ShaderLoaderBridge.setUniform("ao_strength", 0.6f);
        LoggerUtil.debug("Advanced mode hooks ready for '{}'.", shaderName);
    }

    /**
     * Hook: deactivate shader-specific optimizations and restore defaults.
     */
    private void deactivateAdvancedMode() {
        // Future: ShaderLoaderBridge.restoreDefaults();
        LoggerUtil.debug("Advanced mode hooks cleared.");
    }
}
