package com.teamaddoners.gui;

import com.teamaddoners.config.ModConfig;
import com.teamaddoners.util.LoggerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Config screen handler for the Addoners Optimizer mod.
 * Opens an in-game editable config screen when period key is pressed.
 */
@Environment(EnvType.CLIENT)
public class OptimizerConfigScreen {
    
    /**
     * Display current config to console (legacy support).
     */
    public static void displayConfig() {
        ModConfig config = ModConfig.get();
        
        // Log to console with visible separator
        LoggerUtil.info("[KEY_PRESSED] Period key detected!");
        LoggerUtil.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        LoggerUtil.info("  ⚙ ADDONERS OPTIMIZER CONFIGURATION");
        LoggerUtil.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        LoggerUtil.info("  ✓ Optimizer Enabled: {}", config.enabled);
        LoggerUtil.info("  ✓ Debug Logs: {}", config.debugLogs);
        LoggerUtil.info("  ✓ Show Status: {}", config.showStatus);
        LoggerUtil.info("  ✓ Shader Optimization: {}", config.shaderOptimization);
        LoggerUtil.info("  ✓ Optimizer Interval: {} ticks (~{}s)", 
            config.optimizerIntervalTicks, 
            Math.round(config.optimizerIntervalTicks / 20f));
        LoggerUtil.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        LoggerUtil.info("  📝 Edit config at: config/teamaddoners/modconfig.json");
        LoggerUtil.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * Open the config screen as an in-game GUI.
     */
    public static void open() {
        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            client.setScreen(new ConfigGui(null));
        }
    }

    /**
     * The actual config screen GUI.
     */
    @Environment(EnvType.CLIENT)
    public static class ConfigGui extends Screen {
        private final Screen previousScreen;

        public ConfigGui(Screen previousScreen) {
            super(Component.literal("Addoners Optimizer Config"));
            this.previousScreen = previousScreen;
        }

        @Override
        protected void init() {
            ModConfig config = ModConfig.get();
            int centerX = this.width / 2;
            int startY = 30;
            int buttonHeight = 20;
            int spacing = 25;

            // Title
            this.addRenderableWidget(Button.builder(
                Component.literal("Addoners Optimizer Config"),
                button -> {} // No-op button for display
            ).bounds(centerX - 100, startY, 200, buttonHeight).build());

            // Optimizer Enabled Toggle
            this.addRenderableWidget(Button.builder(
                Component.literal("Optimizer: " + (config.enabled ? "✓ ON" : "✗ OFF")),
                button -> {
                    config.enabled = !config.enabled;
                    config.save();
                    button.setMessage(Component.literal("Optimizer: " + (config.enabled ? "✓ ON" : "✗ OFF")));
                    LoggerUtil.info("Optimizer toggled: {}", config.enabled);
                }
            ).bounds(centerX - 100, startY + spacing, 200, buttonHeight).build());

            // Debug Logs Toggle
            this.addRenderableWidget(Button.builder(
                Component.literal("Debug Logs: " + (config.debugLogs ? "✓ ON" : "✗ OFF")),
                button -> {
                    config.debugLogs = !config.debugLogs;
                    config.save();
                    button.setMessage(Component.literal("Debug Logs: " + (config.debugLogs ? "✓ ON" : "✗ OFF")));
                    LoggerUtil.info("Debug logs toggled: {}", config.debugLogs);
                }
            ).bounds(centerX - 100, startY + spacing * 2, 200, buttonHeight).build());

            // Show Status Toggle
            this.addRenderableWidget(Button.builder(
                Component.literal("Show Status: " + (config.showStatus ? "✓ ON" : "✗ OFF")),
                button -> {
                    config.showStatus = !config.showStatus;
                    config.save();
                    button.setMessage(Component.literal("Show Status: " + (config.showStatus ? "✓ ON" : "✗ OFF")));
                    LoggerUtil.info("Status overlay toggled: {}", config.showStatus);
                }
            ).bounds(centerX - 100, startY + spacing * 3, 200, buttonHeight).build());

            // Shader Optimization Toggle
            this.addRenderableWidget(Button.builder(
                Component.literal("Shader Opt: " + (config.shaderOptimization ? "✓ ON" : "✗ OFF")),
                button -> {
                    config.shaderOptimization = !config.shaderOptimization;
                    config.save();
                    button.setMessage(Component.literal("Shader Opt: " + (config.shaderOptimization ? "✓ ON" : "✗ OFF")));
                    LoggerUtil.info("Shader optimization toggled: {}", config.shaderOptimization);
                }
            ).bounds(centerX - 100, startY + spacing * 4, 200, buttonHeight).build());

            // Done Button
            this.addRenderableWidget(Button.builder(
                Component.literal("Done"),
                button -> this.onClose()
            ).bounds(centerX - 50, this.height - 30, 100, buttonHeight).build());
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(previousScreen);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
