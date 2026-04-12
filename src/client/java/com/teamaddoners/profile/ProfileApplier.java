package com.teamaddoners.profile;

import com.teamaddoners.util.LoggerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ParticlesMode;

import java.util.Map;

/**
 * Applies the settings from an {@link AddonersProfile} (or a merged override map)
 * directly to Minecraft's {@link GameOptions}.
 *
 * <p>Recognised keys in the settings map:
 * <ul>
 *   <li>{@code renderDistance} (int 2–32)   — simulation/render distance in chunks</li>
 *   <li>{@code particles}      (int 0–2)    — 0=minimal, 1=decreased, 2=all</li>
 *   <li>{@code smoothLighting} (bool)       — enables/disables ambient occlusion</li>
 *   <li>{@code disableShaders} (bool)       — hint for shader bridge (actual toggle is shader-api specific)</li>
 * </ul>
 *
 * <p><b>Disk I/O throttling:</b> {@link GameOptions#write()} is only called when at
 * least one setting actually changed, avoiding unnecessary disk writes every cycle.
 */
public final class ProfileApplier {

    /** Minimum clamped render distance to avoid degenerate values. */
    private static final int MIN_RENDER_DISTANCE = 2;

    /** Maximum clamped render distance — prevents extreme values from corrupted profiles. */
    private static final int MAX_RENDER_DISTANCE = 32;

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
     * @param settings   Key-value settings to apply.
     * @param sourceName Label used in log messages.
     */
    public static void applyMap(Map<String, Object> settings, String sourceName) {
        if (settings == null || settings.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) return;

        GameOptions options = client.options;
        boolean changed = false;

        // ── Render distance ───────────────────────────────────────────────────────
        if (settings.containsKey("renderDistance")) {
            int rd = toInt(settings.get("renderDistance"), -1);
            if (rd > 0) {
                // Clamp to a safe range — prevents degenerate values from corrupted profiles.
                int clamped = Math.max(MIN_RENDER_DISTANCE, Math.min(MAX_RENDER_DISTANCE, rd));
                if (clamped != rd) {
                    LoggerUtil.warn(
                        "renderDistance={} from '{}' is out of [{}, {}] — clamped to {}.",
                        rd, sourceName, MIN_RENDER_DISTANCE, MAX_RENDER_DISTANCE, clamped
                    );
                }
                options.getViewDistance().setValue(clamped);
                changed = true;
            }
        }

        // ── Particles: 0 = minimal, 1 = decreased, 2 = all ───────────────────────
        if (settings.containsKey("particles")) {
            int particles = toInt(settings.get("particles"), -1);
            if (particles >= 0 && particles <= 2) {
                ParticlesMode mode = ParticlesMode.byId(particles);
                if (mode != null) {
                    // Guard against byId() returning null for an out-of-range id.
                    options.getParticles().setValue(mode);
                    changed = true;
                } else {
                    LoggerUtil.warn(
                        "ParticlesMode.byId({}) returned null for '{}' — skipping particles setting.",
                        particles, sourceName
                    );
                }
            }
        }

        // ── Smooth lighting / ambient occlusion ───────────────────────────────────
        if (settings.containsKey("smoothLighting")) {
            boolean smooth = toBool(settings.get("smoothLighting"), true);
            options.getAo().setValue(smooth);
            changed = true;
        }

        // ── disableShaders: signal for ShaderProfileBridge, not applied here ──────
        // Shader API integration is shader-loader specific (Iris / Canvas) and
        // handled in the shader package. We intentionally don't act on it here.

        // Only persist to disk when something actually changed — options.write() is
        // a file I/O operation and must not be called every optimizer cycle.
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
