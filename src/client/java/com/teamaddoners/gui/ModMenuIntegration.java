package com.teamaddoners.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

/**
 * ModMenu integration for the Addoners Optimizer.
 * Provides config screen access through the ModMenu.
 */
@Environment(EnvType.CLIENT)
public class ModMenuIntegration {
    
    /**
     * Returns the config screen for ModMenu.
     * Called by ModMenu to open the config screen.
     * 
     * @param parentScreen The parent screen to return to when done
     * @return The config screen to display
     */
    public static Screen getModConfigScreen(Screen parentScreen) {
        return new OptimizerConfigScreen.ConfigGui(parentScreen);
    }
}
