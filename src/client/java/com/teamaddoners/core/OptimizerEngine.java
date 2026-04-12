package com.teamaddoners.core;

import com.teamaddoners.adaptive.DynamicOptimizer;
import com.teamaddoners.config.ModConfig;
import com.teamaddoners.profile.AddonersProfile;
import com.teamaddoners.profile.ProfileManager;
import com.teamaddoners.shader.ShaderDetector;
import com.teamaddoners.shader.ShaderProfileBridge;
import com.teamaddoners.util.LoggerUtil;

/**
 * Central optimization scheduler. Runs on a 20-tick interval and coordinates
 * FPS monitoring, level determination, shader detection, and dynamic optimization.
 *
 * <p>This is the "brain" of the mod. All per-cycle logic flows through here,
 * keeping the entry point clean and each subsystem independently testable.
 */
public final class OptimizerEngine {

    private final FPSMonitor fpsMonitor;
    private final DynamicOptimizer dynamicOptimizer;
    private final ProfileManager profileManager;
    private final ShaderDetector shaderDetector;
    private final ShaderProfileBridge shaderProfileBridge;
    private final ModConfig config;

    private OptimizationLevel lastLevel = OptimizationLevel.LOW;
    private int cycleCount = 0;

    /** How often (in optimizer cycles) to run shader detection. Every 5 cycles ≈ ~5 seconds. */
    private static final int SHADER_CHECK_INTERVAL = 5;

    public OptimizerEngine(
            FPSMonitor fpsMonitor,
            DynamicOptimizer dynamicOptimizer,
            ProfileManager profileManager,
            ShaderDetector shaderDetector,
            ShaderProfileBridge shaderProfileBridge,
            ModConfig config) {

        this.fpsMonitor = fpsMonitor;
        this.dynamicOptimizer = dynamicOptimizer;
        this.profileManager = profileManager;
        this.shaderDetector = shaderDetector;
        this.shaderProfileBridge = shaderProfileBridge;
        this.config = config;
    }

    /**
     * Called every 20 ticks by the tick event listener in {@link com.teamaddoners.OptimizerMod}.
     */
    public void cycle() {
        if (!config.enabled) return;

        cycleCount++;

        // 1. Sample FPS into rolling buffer
        fpsMonitor.tick();
        int fps = fpsMonitor.getFPS();

        // 2. Resolve optimization level
        OptimizationLevel level = OptimizationLevel.fromFps(fps);

        // 3. Periodically refresh shader detection
        if (cycleCount % SHADER_CHECK_INTERVAL == 0) {
            String prevShader = shaderDetector.getActiveShader();
            shaderDetector.refresh();
            String newShader = shaderDetector.getActiveShader();
            if (!newShader.equals(prevShader)) {
                shaderProfileBridge.onShaderChanged(newShader);
            }
        }

        // 4. Apply dynamic optimization
        AddonersProfile activeProfile = profileManager.getActiveProfile();
        dynamicOptimizer.apply(level, activeProfile, fps);

        // 5. Debug logging
        if (config.debugLogs) {
            String profileName = (activeProfile != null) ? activeProfile.getName() : "none";
            String shader = shaderDetector.getActiveShader();
            LoggerUtil.info(
                "[DEBUG] FPS={} | Level={} | Profile='{}' | Shader='{}'",
                fps, level, profileName, shader
            );
        }

        lastLevel = level;
    }

    public OptimizationLevel getLastLevel() { return lastLevel; }
    public int getCycleCount() { return cycleCount; }
}
