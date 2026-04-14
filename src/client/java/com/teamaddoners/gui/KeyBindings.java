package com.teamaddoners.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Keybinding handler for the Addoners Optimizer mod.
 * Detects period (.) key press and opens the config screen.
 */
@Environment(EnvType.CLIENT)
public class KeyBindings {

    public static final String CATEGORY = "key.addoners_optimizer.category";
    public static final String OPEN_CONFIG = "key.addoners_optimizer.open_config";

    private static boolean periodWasPressed = false;

    /**
     * Updates the window reference and checks if config screen keybinding was pressed.
     * Should be called from a tick event listener.
     */
    public static boolean wasConfigScreenPressed() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) {
            return false;
        }

        // Get the window handle using reflection if getHandle() doesn't exist
        long windowHandle = getWindowHandle(client);
        if (windowHandle == 0) {
            return false;
        }

        int periodState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_PERIOD);
        boolean isPeriodDown = periodState == GLFW.GLFW_PRESS;
        
        // Detect transition from not-pressed to pressed
        boolean wasPressed = isPeriodDown && !periodWasPressed;
        
        periodWasPressed = isPeriodDown;
        
        return wasPressed;
    }

    /**
     * Get window handle from client, handling MC 26.1.2 compatibility.
     */
    private static long getWindowHandle(Minecraft client) {
        try {
            // Try to use getHandle() method
            Object window = client.getWindow();
            if (window != null) {
                java.lang.reflect.Method getHandle = window.getClass().getMethod("getHandle");
                return (long) getHandle.invoke(window);
            }
        } catch (Exception e) {
            // Handle method doesn't exist, try alternative
        }
        
        // Fallback: attempt direct field access
        try {
            Object window = client.getWindow();
            if (window != null) {
                java.lang.reflect.Field handleField = window.getClass().getDeclaredField("handle");
                handleField.setAccessible(true);
                Object handle = handleField.get(window);
                if (handle instanceof Long) {
                    return (Long) handle;
                }
            }
        } catch (Exception e) {
            // Field doesn't exist either
        }
        
        return 0;
    }

    /**
     * Registers all keybindings for the mod.
     * Called during mod initialization.
     */
    public static void register() {
        // No-op for now - actual detection happens in wasConfigScreenPressed()
    }
}
