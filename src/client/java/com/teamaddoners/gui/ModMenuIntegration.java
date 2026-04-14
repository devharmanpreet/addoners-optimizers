package com.teamaddoners.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApiV3;
import com.teamaddoners.config.ModConfig;
import net.minecraft.client.gui.screens.Screen;

/**
 * Mod Menu integration entry point.
 * Provides the in-game config screen for Addoners Optimizer.
 */
public class ModMenuIntegration implements ModMenuApiV3 {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return OptimizerConfigScreen::new;
    }
}
