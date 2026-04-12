package com.teamaddoners.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Placeholder mixin class required by the mixin JSON config.
 *
 * This stub exists to satisfy the Mixin framework's requirement for at least
 * one declared mixin class. Future mixins (e.g., for renderer hooks, chunk
 * loading intercepts, or direct FPS counter access) will be added here.
 *
 * The class intentionally does NOT inject any hooks — keeping it clean until
 * a concrete need arises.
 */
@Mixin(MinecraftClient.class)
public class MixinClientAccess {
    // Future: @Inject hooks for renderer interception will be added here.
}
