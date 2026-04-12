package com.teamaddoners.shader;

import com.teamaddoners.profile.AddonersProfile;
import com.teamaddoners.profile.ProfileManager;
import com.teamaddoners.util.LoggerUtil;

/**
 * Bridges between the detected shader and the profile system.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>When the active shader changes, attempt to auto-switch to the matching profile.</li>
 *   <li>Notify {@link ShaderOptimizer} of shader changes for advanced mode toggling.</li>
 *   <li>Provide a hook point for future macro injection into shader programs.</li>
 * </ul>
 */
public final class ShaderProfileBridge {

    private final ProfileManager profileManager;
    private final ShaderOptimizer shaderOptimizer;
    private String lastKnownShader = "none";

    public ShaderProfileBridge(ProfileManager profileManager, ShaderOptimizer shaderOptimizer) {
        this.profileManager = profileManager;
        this.shaderOptimizer = shaderOptimizer;
    }

    /**
     * Called when a new shader name is detected. Updates the optimizer and
     * attempts to auto-switch to the shader-matching profile.
     *
     * @param shaderName Current shader name (lowercase).
     */
    public void onShaderChanged(String shaderName) {
        if (shaderName == null) shaderName = "none";

        if (shaderName.equals(lastKnownShader)) return; // No change
        lastKnownShader = shaderName;

        // Notify shader optimizer
        shaderOptimizer.evaluate(shaderName);

        // Attempt profile auto-switch
        boolean switched = profileManager.switchProfile(shaderName);
        if (!switched) {
            LoggerUtil.debug("No dedicated profile for shader '{}' — keeping current profile.", shaderName);
        }

        // Future: prepareMacroInjection(shaderName);
    }

    /**
     * Returns the active profile's shader field, used for consistency checks.
     */
    public String getProfileShaderField() {
        AddonersProfile profile = profileManager.getActiveProfile();
        return (profile != null) ? profile.getShader() : "none";
    }

    /**
     * Hook for future shader macro injection.
     * Prepare metadata for macro compiler — not yet implemented.
     *
     * @param shaderName Target shader name.
     */
    @SuppressWarnings("unused")
    private void prepareMacroInjection(String shaderName) {
        // Future: MacroCompiler.prepare(shaderName, profileManager.getActiveProfile());
        LoggerUtil.debug("Macro injection prepared for '{}'.", shaderName);
    }
}
