package com.teamaddoners.core;

import com.teamaddoners.adaptive.DynamicOptimizer;
import com.teamaddoners.config.ModConfig;
import com.teamaddoners.profile.AddonersProfile;
import com.teamaddoners.profile.ProfileManager;
import com.teamaddoners.shader.ShaderDetector;
import com.teamaddoners.shader.ShaderOptimizer;
import com.teamaddoners.shader.ShaderProfileBridge;
import com.teamaddoners.util.LoggerUtil;

/**
 * Central optimization scheduler. Runs on a 20-tick interval and coordinates
 * FPS monitoring, level determination, shader detection, cooldown enforcement,
 * and dynamic optimization.
 *
 * <p>Architecture (v1.5):
 * <ol>
 *   <li>Sample FPS into rolling buffer (FPSMonitor).</li>
 *   <li>Determine optimization level (OptimizationLevel.fromFps).</li>
 *   <li>Check cooldown — skip applying if within the quiet window.</li>
 *   <li>Optionally cap level when shaders are active (shader-aware mode).</li>
 *   <li>Apply settings via DynamicOptimizer (profile → rules → fallback).</li>
 *   <li>Emit structured log lines.</li>
 * </ol>
 */
public final class OptimizerEngine {

    // ── Cooldown system ───────────────────────────────────────────────────────────

    /** Minimum milliseconds between optimization changes (default 3 seconds). */
    private static final long COOLDOWN_MS = 3_000L;

    /** Wall-clock timestamp of the last applied optimization change, in ms. */
    private long lastAppliedMs = 0L;

    /** True for the current cycle if we are still within the cooldown window. */
    private boolean cooldownActive = false;

    // ── Dependencies ──────────────────────────────────────────────────────────────

    private final FPSMonitor     fpsMonitor;
    private final DynamicOptimizer dynamicOptimizer;
    private final ProfileManager profileManager;
    private final ShaderDetector shaderDetector;
    private final ShaderOptimizer shaderOptimizer;
    private final ShaderProfileBridge shaderProfileBridge;
    private final ModConfig      config;

    // ── State ─────────────────────────────────────────────────────────────────────

    private OptimizationLevel currentLevel = OptimizationLevel.LOW;
    private int               currentFps   = 0;
    private int               cycleCount   = 0;

    /** How often (in optimizer cycles) to run shader detection. Every 5 cycles ≈ 5 s. */
    private static final int SHADER_CHECK_INTERVAL = 5;

    public OptimizerEngine(
            FPSMonitor fpsMonitor,
            DynamicOptimizer dynamicOptimizer,
            ProfileManager profileManager,
            ShaderDetector shaderDetector,
            ShaderOptimizer shaderOptimizer,
            ShaderProfileBridge shaderProfileBridge,
            ModConfig config) {

        this.fpsMonitor         = fpsMonitor;
        this.dynamicOptimizer   = dynamicOptimizer;
        this.profileManager     = profileManager;
        this.shaderDetector     = shaderDetector;
        this.shaderOptimizer    = shaderOptimizer;
        this.shaderProfileBridge = shaderProfileBridge;
        this.config             = config;
    }

    /**
     * Called every N ticks by the tick event listener in {@link com.teamaddoners.OptimizerMod}.
     * Wrapped in a top-level try-catch so any subsystem bug skips the cycle instead of crashing.
     */
    public void cycle() {
        if (!config.enabled) return;

        // Overflow-safe increment
        cycleCount = (cycleCount >= Integer.MAX_VALUE) ? 1 : cycleCount + 1;

        try {
            runCycle();
        } catch (Exception e) {
            LoggerUtil.error("Unexpected error in optimizer cycle #" + cycleCount + " — skipping.", e);
        }
    }

    /** Inner cycle implementation, separated for clean wrapping. */
    private void runCycle() {

        // 1. Sample FPS into rolling buffer
        fpsMonitor.tick();
        int fps = fpsMonitor.getFPS();
        currentFps = fps;

        // 2. Resolve raw optimization level from averaged FPS
        OptimizationLevel rawLevel = OptimizationLevel.fromFps(fps);

        // 3. Periodically refresh shader detection
        if (cycleCount % SHADER_CHECK_INTERVAL == 0) {
            String prevShader = shaderDetector.getActiveShader();
            shaderDetector.refresh();
            String newShader = shaderDetector.getActiveShader();

            if (!newShader.equals(prevShader)) {
                shaderProfileBridge.onShaderChanged(newShader);
                LoggerUtil.info("[Addoners Optimizer] Shader detected: {}", shaderDetector.isShaderActive());
            }
        }

        // 4. Shader-aware level capping (v1.5)
        OptimizationLevel effectiveLevel = rawLevel;
        if (config.shaderOptimization && shaderDetector.isShaderActive()) {
            effectiveLevel = shaderOptimizer.adjustLevel(rawLevel);
        }

        // 5. Cooldown check — suppress changes if within quiet window
        long now = System.currentTimeMillis();
        cooldownActive = (now - lastAppliedMs) < COOLDOWN_MS;

        if (cooldownActive) {
            if (config.debugLogs) {
                LoggerUtil.info("[Addoners Optimizer] Cooldown active: true");
            }
            currentLevel = effectiveLevel; // update display level without re-applying
            return;
        }

        // 6. Apply dynamic optimization
        AddonersProfile activeProfile = profileManager.getActiveProfile();
        dynamicOptimizer.apply(effectiveLevel, activeProfile, fps);
        lastAppliedMs = now;
        currentLevel  = effectiveLevel;

        // 7. Structured logs (v1.5 format)
        LoggerUtil.info("[Addoners Optimizer] FPS: {} → {}", fps, effectiveLevel.getDisplayName().toUpperCase());
        LoggerUtil.info("[Addoners Optimizer] Cooldown active: false");

        String profileName = (activeProfile != null) ? activeProfile.getName() : "none";
        LoggerUtil.info("[Addoners Optimizer] Profile applied: {}", profileName);

        if (config.debugLogs) {
            String shader = shaderDetector.getActiveShader();
            LoggerUtil.info("[Addoners Optimizer] Shader detected: {}", shaderDetector.isShaderActive());
            LoggerUtil.info("[DEBUG] FPS={} | Level={} | Profile='{}' | Shader='{}'",
                fps, effectiveLevel, profileName, shader);
        }
    }

    // ── Public accessors (used by StatusOverlay and future UI components) ─────────

    /** Current effective optimization level (used on last applied cycle). */
    public OptimizationLevel getCurrentLevel()  { return currentLevel; }

    /** Smoothed FPS reading from the last sampled cycle. */
    public int getCurrentFps()                  { return currentFps; }

    /** True if the cooldown window is still active — no changes are being applied. */
    public boolean isCooldownActive()           { return cooldownActive; }

    /** Total optimizer cycles executed since startup. */
    public int getCycleCount()                  { return cycleCount; }

    /**
     * @deprecated Use {@link #getCurrentLevel()} — kept for binary compatibility.
     */
    @Deprecated
    public OptimizationLevel getLastLevel()     { return currentLevel; }
}
