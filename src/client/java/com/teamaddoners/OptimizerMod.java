package com.teamaddoners;

import com.teamaddoners.adaptive.DynamicOptimizer;
import com.teamaddoners.config.ModConfig;
import com.teamaddoners.core.FPSMonitor;
import com.teamaddoners.core.OptimizerEngine;
import com.teamaddoners.core.SystemMonitor;
import com.teamaddoners.profile.ProfileManager;
import com.teamaddoners.shader.ShaderDetector;
import com.teamaddoners.shader.ShaderOptimizer;
import com.teamaddoners.shader.ShaderProfileBridge;
import com.teamaddoners.util.LoggerUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Addoners Optimizer — primary client-side mod entry point.
 *
 * <p>Initializes all subsystems in dependency order and registers the tick
 * event listener that drives the 20-tick optimization cycle.
 */
@Environment(EnvType.CLIENT)
public class OptimizerMod implements ClientModInitializer {

    public static final String MOD_ID = "addoners_optimizer";
    public static final String VERSION = "1.0.0";

    // ── Singleton subsystem access (read-only from other modules) ─────────────────
    private static ProfileManager profileManager;
    private static OptimizerEngine optimizerEngine;
    private static ShaderDetector shaderDetector;

    // ── Tick counter for interval management ─────────────────────────────────────
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        LoggerUtil.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        LoggerUtil.info("  Addoners Optimizer v{} loading...", VERSION);
        LoggerUtil.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // 1. Load configuration
        ModConfig config = ModConfig.get();
        LoggerUtil.info("Config loaded — enabled={}, debugLogs={}", config.enabled, config.debugLogs);

        if (!config.enabled) {
            LoggerUtil.warn("Optimizer is DISABLED via config. Skipping initialization.");
            return;
        }

        // 2. System detection
        SystemMonitor systemMonitor = new SystemMonitor();
        LoggerUtil.info("System detected: {}", systemMonitor);

        // 3. Profile system
        profileManager = new ProfileManager();
        profileManager.initialize();

        // 4. Core monitoring
        FPSMonitor fpsMonitor = new FPSMonitor();

        // 5. Shader system
        shaderDetector = new ShaderDetector();
        ShaderOptimizer shaderOptimizer = new ShaderOptimizer();
        ShaderProfileBridge shaderProfileBridge = new ShaderProfileBridge(profileManager, shaderOptimizer);

        // 6. Adaptive optimization
        DynamicOptimizer dynamicOptimizer = new DynamicOptimizer(systemMonitor);

        // 7. Optimizer engine
        optimizerEngine = new OptimizerEngine(
                fpsMonitor,
                dynamicOptimizer,
                profileManager,
                shaderDetector,
                shaderProfileBridge,
                config
        );

        // 8. Register 20-tick interval via ClientTickEvents
        int interval = config.optimizerIntervalTicks;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter >= interval) {
                tickCounter = 0;
                optimizerEngine.cycle();
            }
        });

        LoggerUtil.info("Addoners Optimizer initialized successfully.");
        LoggerUtil.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    // ── Static accessors for other modules / future GUI ───────────────────────────

    public static ProfileManager getProfileManager() { return profileManager; }
    public static OptimizerEngine getOptimizerEngine() { return optimizerEngine; }
    public static ShaderDetector getShaderDetector() { return shaderDetector; }
}
