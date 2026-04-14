package com.teamaddoners;

import com.teamaddoners.adaptive.DynamicOptimizer;
import com.teamaddoners.config.ModConfig;
import com.teamaddoners.core.FPSMonitor;
import com.teamaddoners.core.OptimizerEngine;
import com.teamaddoners.core.SystemMonitor;
import com.teamaddoners.gui.StatusOverlay;
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
 */
@Environment(EnvType.CLIENT)
public class OptimizerMod implements ClientModInitializer {

    public static final String MOD_ID = "addoners_optimizer";
    public static final String VERSION = "1.0.0"; // TODO: sync with fabric.mod.json later

    // ── Subsystems ───────────────────────────────────────────────────────────────
    private static ProfileManager profileManager;
    private static OptimizerEngine optimizerEngine;
    private static ShaderDetector shaderDetector;

    // ── Tick management ──────────────────────────────────────────────────────────
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        LoggerUtil.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        LoggerUtil.info("  Addoners Optimizer v{} loading...", VERSION);
        LoggerUtil.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // 1. Load config
        ModConfig config = ModConfig.get();
        LoggerUtil.info(
                "Config — enabled={}, debugLogs={}, overlay={}, shaderOpt={}",
                config.enabled,
                config.debugLogs,
                config.showStatus,
                config.shaderOptimization
        );

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

        // 4. FPS monitor (with smoothing)
        FPSMonitor fpsMonitor = new FPSMonitor();

        // 5. Shader system
        shaderDetector = new ShaderDetector();
        LoggerUtil.info("Initial shader state: {}", shaderDetector.detect());

        ShaderOptimizer shaderOptimizer = new ShaderOptimizer();
        ShaderProfileBridge shaderProfileBridge =
                new ShaderProfileBridge(profileManager, shaderOptimizer);

        // 6. Adaptive optimizer
        DynamicOptimizer dynamicOptimizer = new DynamicOptimizer(systemMonitor);

        // 7. Core engine
        optimizerEngine = new OptimizerEngine(
                fpsMonitor,
                dynamicOptimizer,
                profileManager,
                shaderDetector,
                shaderOptimizer,
                shaderProfileBridge,
                config
        );

        // 8. Tick loop
        int interval = Math.max(1, config.optimizerIntervalTicks);
        LoggerUtil.info("Optimizer cycle interval: {} tick(s)", interval);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;

            // 🔥 FPS smoothing update (IMPORTANT)
            fpsMonitor.tick();

            if (tickCounter >= interval) {
                tickCounter = 0;
                optimizerEngine.cycle();
            }
        });

        // 9. Status overlay (V1.5 feature)
        if (config.showStatus) {
            StatusOverlay.getInstance(); // Registers to HUD rendering
            LoggerUtil.info("Status overlay enabled.");
        }

        LoggerUtil.info("Addoners Optimizer initialized successfully.");
        LoggerUtil.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    // ── Accessors ────────────────────────────────────────────────────────────────

    public static ProfileManager getProfileManager() {
        return profileManager;
    }

    public static OptimizerEngine getOptimizerEngine() {
        return optimizerEngine;
    }

    public static ShaderDetector getShaderDetector() {
        return shaderDetector;
    }

    public static boolean isInitialized() {
        return optimizerEngine != null;
    }
}