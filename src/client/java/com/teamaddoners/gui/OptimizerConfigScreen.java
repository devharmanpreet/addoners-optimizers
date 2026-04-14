package com.teamaddoners.gui;

import com.teamaddoners.config.ModConfig;
import com.teamaddoners.util.LoggerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Configuration screen for Addoners Optimizer.
 * Displayed when opening mod settings from Mod Menu.
 */
public class OptimizerConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 30;

    private final Screen parent;
    private final ModConfig config;

    // Buttons/Widgets
    private Button enableButton;
    private Button debugLogsButton;
    private Button showStatusButton;
    private Button shaderOptButton;
    private CycleButton<Integer> intervalButton;
    private Button saveButton;
    private Button backButton;

    public OptimizerConfigScreen(Screen parent) {
        super(Component.literal("Addoners Optimizer Settings"));
        this.parent = parent;
        this.config = ModConfig.get();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int y = startY;

        // ── Enabled Toggle ──────────────────────────────────────────────────────────
        this.enableButton = this.addRenderableWidget(Button.builder(
                getEnabledButtonLabel(),
                button -> toggleEnabled()
        ).pos(centerX - BUTTON_WIDTH / 2, y).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).build());

        y += SPACING;

        // ── Debug Logs Toggle ───────────────────────────────────────────────────────
        this.debugLogsButton = this.addRenderableWidget(Button.builder(
                getDebugLogsButtonLabel(),
                button -> toggleDebugLogs()
        ).pos(centerX - BUTTON_WIDTH / 2, y).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).build());

        y += SPACING;

        // ── Show Status Toggle (v1.5) ───────────────────────────────────────────────
        this.showStatusButton = this.addRenderableWidget(Button.builder(
                getShowStatusButtonLabel(),
                button -> toggleShowStatus()
        ).pos(centerX - BUTTON_WIDTH / 2, y).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).build());

        y += SPACING;

        // ── Shader Optimization Toggle (v1.5) ───────────────────────────────────────
        this.shaderOptButton = this.addRenderableWidget(Button.builder(
                getShaderOptButtonLabel(),
                button -> toggleShaderOpt()
        ).pos(centerX - BUTTON_WIDTH / 2, y).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).build());

        y += SPACING;

        // ── Interval Slider/Cycle Button ────────────────────────────────────────────
        this.intervalButton = this.addRenderableWidget(CycleButton.<Integer>builder(
                value -> Component.literal("Interval: " + value + " ticks")
        ).withValues(
                1, 5, 10, 20, 40, 60, 100, 200
        ).withInitialValue(config.optimizerIntervalTicks)
                .create(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                        Component.literal("Cycle Interval"),
                        (button, value) -> config.optimizerIntervalTicks = value)
        ).build());

        y += SPACING + 20;

        // ── Save Button ─────────────────────────────────────────────────────────────
        this.saveButton = this.addRenderableWidget(Button.builder(
                Component.literal("Save & Close"),
                button -> saveAndClose()
        ).pos(centerX - BUTTON_WIDTH / 2, y).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).build());

        y += SPACING;

        // ── Back Button ─────────────────────────────────────────────────────────────
        this.backButton = this.addRenderableWidget(Button.builder(
                Component.literal("Cancel"),
                button -> this.onClose()
        ).pos(centerX - BUTTON_WIDTH / 2, y).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Background
        this.renderBackground(guiGraphics);

        // Title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        // Status text
        String statusText = "Optimizer: " + (config.enabled ? "§aON" : "§cOFF");
        guiGraphics.drawCenteredString(this.font, statusText, this.width / 2, 90, 0xFFFFFF);

        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    private void toggleEnabled() {
        config.enabled = !config.enabled;
        this.enableButton.setMessage(getEnabledButtonLabel());
        LoggerUtil.info("Optimizer toggled: {}", config.enabled);
    }

    private void toggleDebugLogs() {
        config.debugLogs = !config.debugLogs;
        this.debugLogsButton.setMessage(getDebugLogsButtonLabel());
        LoggerUtil.info("Debug logs toggled: {}", config.debugLogs);
    }

    private void toggleShowStatus() {
        config.showStatus = !config.showStatus;
        this.showStatusButton.setMessage(getShowStatusButtonLabel());
        LoggerUtil.info("Status overlay toggled: {}", config.showStatus);
    }

    private void toggleShaderOpt() {
        config.shaderOptimization = !config.shaderOptimization;
        this.shaderOptButton.setMessage(getShaderOptButtonLabel());
        LoggerUtil.info("Shader optimization toggled: {}", config.shaderOptimization);
    }

    private void saveAndClose() {
        config.save();
        LoggerUtil.info("Config saved. Optimizer will update on next cycle.");
        this.onClose();
    }

    private Component getEnabledButtonLabel() {
        return Component.literal(config.enabled ? "§a✓ Enabled" : "§c✗ Disabled");
    }

    private Component getDebugLogsButtonLabel() {
        return Component.literal("Debug Logs: " + (config.debugLogs ? "§aON" : "§cOFF"));
    }

    private Component getShowStatusButtonLabel() {
        return Component.literal("Status: " + (config.showStatus ? "§aON" : "§cOFF"));
    }

    private Component getShaderOptButtonLabel() {
        return Component.literal("Shaders: " + (config.shaderOptimization ? "§aON" : "§cOFF"));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
