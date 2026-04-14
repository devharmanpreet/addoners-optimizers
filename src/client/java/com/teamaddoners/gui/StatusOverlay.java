package com.teamaddoners.gui;

import com.teamaddoners.OptimizerMod;
import com.teamaddoners.config.ModConfig;
import com.teamaddoners.core.OptimizerEngine;
import com.teamaddoners.core.OptimizationLevel;
import com.teamaddoners.util.LoggerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Status overlay for the Addoners Optimizer.
 * Displays optimizer status via console logging every N ticks.
 * This approach is compatible with all Minecraft versions.
 */
@Environment(EnvType.CLIENT)
public class StatusOverlay {

    private static int lastFps = -1;
    private static OptimizationLevel lastLevel = null;
    private static boolean lastCooldown = false;
    private static int logCounter = 0;
    private static final int LOG_INTERVAL = 100; // Log every 5 seconds (100 ticks)

    /**
     * Register the status display callback. Called during mod initialization.
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> updateStatus());
    }

    /**
     * Updates and logs status periodically.
     */
    private static void updateStatus() {
        ModConfig config = ModConfig.get();
        if (!config.enabled || !config.showStatus) {
            return;
        }

        if (!OptimizerMod.isInitialized()) {
            return;
        }

        OptimizerEngine engine = OptimizerMod.getOptimizerEngine();
        if (engine == null) {
            return;
        }

        logCounter++;
        
        // Get current state
        int fps = engine.getCurrentFps();
        OptimizationLevel level = engine.getCurrentLevel();
        boolean cooldownActive = engine.isCooldownActive();
        int stability = engine.getStabilityCounter();
        int cycles = engine.getCycleCount();

        // Log when state changes or periodically
        boolean stateChanged = (level != lastLevel || fps != lastFps || cooldownActive != lastCooldown);
        
        if (stateChanged) {
            LoggerUtil.info(
                "[Status] FPS: {} | Level: {} | Cooldown: {} | Stability: {}/3 | Cycles: {}",
                fps,
                level.getDisplayName(),
                cooldownActive ? "ACTIVE" : "OFF",
                stability,
                cycles
            );
            logCounter = 0; // Reset counter on change
        } else if (logCounter >= LOG_INTERVAL) {
            // Log periodically even if no change
            LoggerUtil.debug(
                "[Status] FPS: {} | Level: {} | Cooldown: {} | Stability: {}/3 | Cycles: {}",
                fps,
                level.getDisplayName(),
                cooldownActive ? "ACTIVE" : "OFF",
                stability,
                cycles
            );
            logCounter = 0;
        }

        lastFps = fps;
        lastLevel = level;
        lastCooldown = cooldownActive;
    }
}
