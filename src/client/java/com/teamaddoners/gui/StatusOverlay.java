package com.teamaddoners.gui;

import com.teamaddoners.core.OptimizationLevel;
import com.teamaddoners.config.ModConfig;
import com.teamaddoners.util.LoggerUtil;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Status overlay renderer for the Addoners Optimizer.
 * Displays a lightweight HUD in the top-left corner with current optimizer state.
 *
 * <p>Design: Server-compatible architecture (overlay registration happens client-side only).
 */
public final class StatusOverlay implements HudRenderCallback {

    private static StatusOverlay INSTANCE;

    private int fps = 0;
    private String profileName = "none";
    private OptimizationLevel currentLevel = OptimizationLevel.LOW;
    private boolean shadersEnabled = false;

    // Cache update frequency to avoid spam
    private int tickCounter = 0;

    private StatusOverlay() {}

    public static StatusOverlay getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StatusOverlay();
            HudRenderCallback.EVENT.register(INSTANCE);
            LoggerUtil.debug("StatusOverlay registered to HUD rendering pipeline");
        }
        return INSTANCE;
    }

    /**
     * Updates state. Called by OptimizerEngine every cycle.
     */
    public void update(int fps, String profileName, OptimizationLevel level, boolean shadersEnabled) {
        this.fps = fps;
        this.profileName = profileName != null ? profileName : "none";
        this.currentLevel = level;
        this.shadersEnabled = shadersEnabled;
    }

    @Override
    public void onHudRender(GuiGraphics guiGraphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.screen != null) return; // Don't render when menu is open

        ModConfig config = ModConfig.get();
        if (!config.enabled || !config.showStatus) return;

        renderStatus(guiGraphics);
    }

    private void renderStatus(GuiGraphics guiGraphics) {
        int x = 10;
        int y = 10;
        int lineHeight = 10;

        // Color coding for optimization level
        int levelColor = switch (currentLevel) {
            case LOW -> 0xFF00FF00;        // Green
            case MEDIUM -> 0xFFFFFF00;     // Yellow
            case AGGRESSIVE -> 0xFFFF5555; // Red
        };

        // Title
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                "§6Addoners Optimizer: " + currentLevel.getDisplayName(),
                x, y,
                levelColor
        );

        y += lineHeight;

        // FPS
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                "§7FPS: §f" + fps,
                x, y,
                0xFFFFFFFF
        );

        y += lineHeight;

        // Profile
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                "§7Profile: §f" + profileName,
                x, y,
                0xFFFFFFFF
        );

        y += lineHeight;

        // Shader status
        String shaderStatus = shadersEnabled ? "§aON" : "§cOFF";
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                "§7Shaders: " + shaderStatus,
                x, y,
                0xFFFFFFFF
        );
    }

    /**
     * Toggles visibility (useful for future keybinding).
     */
    public void toggleVisibility() {
        ModConfig config = ModConfig.get();
        config.showStatus = !config.showStatus;
        config.save();
        LoggerUtil.info("Status overlay toggled: {}", config.showStatus);
    }
}
