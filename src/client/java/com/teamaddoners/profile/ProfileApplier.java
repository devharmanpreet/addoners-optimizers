package com.teamaddoners.profile;

import com.teamaddoners.util.LoggerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;

/**
 * Applies the settings from an {@link AddonersProfile} (or a merged override map)
 * directly to Minecraft's {@link GameOptions}.
 *
 * <p>Keys recognised in the settings map:
 * <ul>
 *   <li>{@code renderDistance} (int)      — simulation / render distance in chunks</li>
 *   <li>{@code particles}      (int 0-2)  — 0=minimal, 1=decreased, 2=all</li>
 *   <li>{@code smoothLighting} (bool)     — enables/disables ambient occlusion</li>
 *   <li>{@code disableShaders} (bool)     — hint for shader bridge (actual toggle is shader-api specific)</li>
 * </ul>
 */
public final class ProfileApplier {

    private ProfileApplier() {}

    /**
     * Applies the profile's base settings to the current Minecraft options.
     *
     * @param profile Source profile. No-op if null.
     */
    public static void apply(AddonersProfile profile) {
        if (profile == null) return;
        applyMap(profile.getSettings(), profile.getName());
    }

    /**
     * Applies a raw settings map (e.g. from a rule engine override) to Minecraft options.
     *
     * @param settings  Key-value settings to apply.
     * @param sourceName Label used in log messages.
     */
    public static void applyMap(java.util.Map<String, Object> settings, String sourceName) {
        if (settings == null || settings.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) return;

        GameOptions options = client.options;
        boolean changed = false;

        // Render distance
        if (settings.containsKey("renderDistance")) {
            int rd = toInt(settings.get("renderDistance"), -1);
            if (rd > 0) {
                options.getViewDistance().setValue(rd);
                changed = true;
            }
        }

        // Particles: 0 = minimal, 1 = decreased, 2 = all
        if (settings.containsKey("particles")) {
            int particles = toInt(settings.get("particles"), -1);
            if (particles >= 0 && particles <= 2) {
                options.getParticles().setValue(
                    net.minecraft.client.option.ParticlesMode.byId(particles)
                );
                changed = true;
            }
        }

        // Smooth lighting / ambient occlusion
        if (settings.containsKey("smoothLighting")) {
            boolean smooth = toBool(settings.get("smoothLighting"), true);
            options.getAo().setValue(smooth);
            changed = true;
        }

        // disableShaders is a signal consumed by ShaderProfileBridge — not applied here directly
        // (Shader API integration is shader-loader specific and handled in the shader package)

        if (changed) {
            options.write();
            LoggerUtil.debug("Applied settings from '{}' to Minecraft options.", sourceName);
        }
    }

    // ── Type coercions ────────────────────────────────────────────────────────────

    private static int toInt(Object val, int fallback) {
        if (val instanceof Number n) return n.intValue();
        if (val instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }

    private static boolean toBool(Object val, boolean fallback) {
        if (val instanceof Boolean b) return b;
        if (val instanceof String s) return Boolean.parseBoolean(s);
        return fallback;
    }
}
