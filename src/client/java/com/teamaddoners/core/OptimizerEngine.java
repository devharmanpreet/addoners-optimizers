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
 * <p>Architecture (v2.0 - Stable & Smooth):
 * <ol>
 *   <li>Sample FPS into rolling buffer (FPSMonitor).</li>
 *   <li>Determine raw optimization level (OptimizationLevel.fromFps).</li>
 *   <li>Implement stability counter: only switch after 3 consecutive cycles at same level.</li>
 *   <li>Check cooldown — skip applying if within the quiet window (4 seconds).</li>
 *   <li>Skip applying if level hasn't changed since last application.</li>
 *   <li>Optionally cap level when shaders are active (shader-aware mode).</li>
 *   <li>Apply settings via DynamicOptimizer (profile → rules → fallback).</li>
 *   <li>Emit structured log lines with full context.</li>
 * </ol>
 * 
 * <p>Key Improvements:
 * <ul>
 *   <li>Stability counter prevents flickering (3-cycle hysteresis)</li>
 *   <li>Robust cooldown enforcement (4 seconds between changes)</li>
 *   <li>Skip re-applying same level (no unnecessary allocation)</li>
 *   <li>Clear logging for debugging and monitoring</li>
 * </ul>
 */
public final class OptimizerEngine {
    private OptimizationLevel lockedLevel = null;
    // ── Cooldown & Stability system ───────────────────────────────────────────────

    /** Minimum milliseconds between optimization changes (4 seconds for stability). */
    private static final long COOLDOWN_MS = 4_000L;

    /** Number of consecutive cycles at same level before switching. Prevents flickering. */
    private static final int STABILITY_THRESHOLD = 3;

    /** Wall-clock timestamp of the last applied optimization change, in ms. */
    private long lastApplyTime = 0L;

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

    /** The currently active optimization level (last applied). */
    private OptimizationLevel currentLevel = OptimizationLevel.LOW;

    /** The proposed level from FPS analysis (raw determination). */
    private OptimizationLevel pendingLevel = OptimizationLevel.LOW;

    /** The level that was last applied to the game. Used to skip re-applying. */
    private OptimizationLevel lastAppliedLevel = OptimizationLevel.LOW;

    /** Counter for consecutive cycles at the same pending level. */
    private int stabilityCounter = 0;

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

        // 4. Shader-aware level capping (v2.0)
        OptimizationLevel effectiveLevel = rawLevel;
        if (config.shaderOptimization && shaderDetector.isShaderActive()) {
            effectiveLevel = shaderOptimizer.adjustLevel(rawLevel);
        }

        // 5. Stability counter: track consecutive cycles at the same pending level
        if (effectiveLevel.equals(pendingLevel)) {
            stabilityCounter++;
        } else {
            pendingLevel = effectiveLevel;
            stabilityCounter = 1;
        }

        // 6. Cooldown check — suppress changes if within quiet window
        long now = System.currentTimeMillis();
        cooldownActive = (now - lastApplyTime) < COOLDOWN_MS;


        // 🔥 HARD LOCK during cooldown
        if (cooldownActive && lockedLevel != null) {
            pendingLevel = lockedLevel;
            currentLevel = lockedLevel;
            stabilityCounter = 0;
        }

        // ┌─────────────────────────────────────────────────────────────────────────┐
        // │ EARLY RETURN if cooldown is still active                              │
        // └─────────────────────────────────────────────────────────────────────────┘
        if (cooldownActive) {
            logCooldownActive(fps, pendingLevel);
            return;
        }

        // 7. Stability check: only apply if we've seen the same level for STABILITY_THRESHOLD cycles
        if (stabilityCounter < STABILITY_THRESHOLD) {
            logWaitingForStability(fps, pendingLevel, stabilityCounter);
            return;
        }

        // 8. Check if this is a real change (skip if same as last applied)
        if (pendingLevel.equals(lastAppliedLevel)) {
            stabilityCounter = 0; // 🔥 IMPORTANT RESET
            logNoChangeNeeded(fps, pendingLevel);
            return;
        }

        // 9. APPLY: All checks passed, now apply the optimization
        ApplyResult result = applyOptimization(pendingLevel, fps);
        
        lastApplyTime = now;
        lastAppliedLevel = pendingLevel;
        currentLevel = pendingLevel;
        lockedLevel = pendingLevel; // 🔥 LOCK LEVEL
        stabilityCounter = 0;

        logApplicationSuccess(fps, pendingLevel, result);
    }

    /**
     * Applies the optimization level to the game. Encapsulated for clarity and testing.
     * Returns metadata about the application for logging.
     */
    private ApplyResult applyOptimization(OptimizationLevel level, int fps) {
        AddonersProfile activeProfile = profileManager.getActiveProfile();
        String profileName = (activeProfile != null) ? activeProfile.getName() : "none";

        dynamicOptimizer.apply(level, activeProfile, fps);

        String shader = shaderDetector.getActiveShader();
        boolean shaderActive = shaderDetector.isShaderActive();

        return new ApplyResult(profileName, shader, shaderActive);
    }

    // ── Logging helpers (v2.0 - structured and clear) ───────────────────────────

    private void logCooldownActive(int fps, OptimizationLevel proposedLevel) {
        long timeRemaining = Math.max(0, COOLDOWN_MS - (System.currentTimeMillis() - lastApplyTime));
        // Always log cooldown to show why changes are blocked (important for understanding behavior)
        LoggerUtil.info("[Addoners Optimizer] Cooldown active: true | Remaining: {}ms | FPS: {} → {} (pending)", 
            timeRemaining, fps, proposedLevel.getDisplayName().toUpperCase());
    }

    private void logWaitingForStability(int fps, OptimizationLevel level, int counter) {
        if (config.debugLogs) {
            LoggerUtil.info("[Addoners Optimizer] Stability counter: {}/{} | FPS: {} → {}", 
                counter, STABILITY_THRESHOLD, fps, level.getDisplayName().toUpperCase());
        }
    }

    private void logNoChangeNeeded(int fps, OptimizationLevel level) {
        if (config.debugLogs) {
            LoggerUtil.info("[Addoners Optimizer] Skipped (no change) | FPS: {} | Level: {}", 
                fps, level.getDisplayName().toUpperCase());
        }
    }

    private void logApplicationSuccess(int fps, OptimizationLevel level, ApplyResult result) {
        // Primary log line
        LoggerUtil.info("[Addoners Optimizer] FPS: {} → {}", fps, level.getDisplayName().toUpperCase());
        LoggerUtil.info("[Addoners Optimizer] Cooldown active: false");
        LoggerUtil.info("[Addoners Optimizer] Applying level: {}", level.getDisplayName().toUpperCase());
        LoggerUtil.info("[Addoners Optimizer] Profile applied: {}", result.profileName());

        if (config.debugLogs) {
            LoggerUtil.info("[DEBUG] Optimizer state | FPS={} | Level={} | Profile='{}' | Shader='{}' | ShaderActive={}",
                fps, level, result.profileName(), result.shader(), result.shaderActive());
            LoggerUtil.info("[DEBUG] Stability counter reset. Ready for next level change.");
        }
    }

    /**
     * Immutable result object for apply operation (reduces allocations).
     */
    private record ApplyResult(String profileName, String shader, boolean shaderActive) {
    }

    // ── Public accessors (used by StatusOverlay and future UI components) ─────────

    /** Current effective optimization level (used on last applied cycle). */
    public OptimizationLevel getCurrentLevel()  { return currentLevel; }

    /** The pending level (proposed by FPS analysis, waiting for stability). */
    public OptimizationLevel getPendingLevel()  { return pendingLevel; }

    /** Smoothed FPS reading from the last sampled cycle. */
    public int getCurrentFps()                  { return currentFps; }

    /** True if the cooldown window is still active — no changes are being applied. */
    public boolean isCooldownActive()           { return cooldownActive; }

    /** Current stability counter value (0..STABILITY_THRESHOLD). */
    public int getStabilityCounter()            { return stabilityCounter; }

    /** Total optimizer cycles executed since startup. */
    public int getCycleCount()                  { return cycleCount; }

    /**
     * @deprecated Use {@link #getCurrentLevel()} — kept for binary compatibility.
     */
    @Deprecated
    public OptimizationLevel getLastLevel()     { return currentLevel; }
}
