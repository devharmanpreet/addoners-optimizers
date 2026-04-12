package com.teamaddoners.shader;

import com.teamaddoners.util.LoggerUtil;

/**
 * Attempts to detect the name of the currently active shader pack.
 *
 * <p>In a vanilla Fabric environment without a shader loader (Iris, Oculus, etc.),
 * there is no public API to query the active shader. This class provides a
 * future-ready stub architecture:
 *
 * <ul>
 *   <li>Checks a system property {@code addoners.shaderName} (for testing/integration).</li>
 *   <li>Falls back to inspecting Minecraft options (future: iris API hook).</li>
 *   <li>Returns {@code "none"} when no shader is detected.</li>
 * </ul>
 */
public final class ShaderDetector {

    /** System property override — useful for testing and future shader loader bridges. */
    private static final String SYSTEM_PROP_KEY = "addoners.shaderName";

    private String currentShader = "none";

    /**
     * Samples the current shader name. Call periodically (not every tick).
     */
    public void refresh() {
        String detected = detect();
        if (!detected.equals(currentShader)) {
            LoggerUtil.info("Shader change detected: '{}' → '{}'", currentShader, detected);
            currentShader = detected;
        }
    }

    /**
     * Returns the last detected shader name.
     *
     * @return Lowercase shader name, or {@code "none"} if no shader is active.
     */
    public String getActiveShader() {
        return currentShader;
    }

    /**
     * Returns true if any shader (other than "none") is currently loaded.
     */
    public boolean isShaderActive() {
        return !"none".equalsIgnoreCase(currentShader);
    }

    // ── Detection Logic ───────────────────────────────────────────────────────────

    private String detect() {
        // Priority 1: explicit system property (for testing and integration bridges)
        String sysProp = System.getProperty(SYSTEM_PROP_KEY);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim().toLowerCase();
        }

        // Priority 2: future Iris/Canvas API hook point.
        // When an Iris integration module is added, it can inject the shader name here.
        // Example: return IrisBridge.getActiveShaderName().orElse("none");

        // No shader loader detected — return "none".
        return "none";
    }
}
