package com.teamaddoners.profile;


import com.teamaddoners.util.LoggerUtil;
import net.minecraft.client.Minecraft;

import java.util.Map;

public final class ProfileApplier {

    private static final int MIN_RENDER_DISTANCE = 2;
    private static final int MAX_RENDER_DISTANCE = 32;

    private ProfileApplier() {}

    public static void apply(AddonersProfile profile) {
        if (profile == null) return;
        applyMap(profile.getSettings(), profile.getName());
    }

    public static void applyMap(Map<String, Object> settings, String sourceName) {
        if (settings == null || settings.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.options == null) return;

        var options = client.options;
        boolean changed = false;

        // ── Render Distance ─────────────────────────────
        if (settings.containsKey("renderDistance")) {
            int rd = toInt(settings.get("renderDistance"), -1);

            if (rd > 0) {
                int clamped = Math.max(MIN_RENDER_DISTANCE, Math.min(MAX_RENDER_DISTANCE, rd));

                if (clamped != rd) {
                    LoggerUtil.warn(
                            "renderDistance={} from '{}' is out of range — clamped to {}",
                            rd, sourceName, clamped
                    );
                }

                options.renderDistance().set(clamped);
                changed = true;
            }
        }

        // ── Particles ───────────────────────────────────
        if (settings.containsKey("particles")) {
            int particles = toInt(settings.get("particles"), -1);

            LoggerUtil.debug("Particles setting requested: {} (skipped due to mapping compatibility)", particles);
        }

        // ── Smooth Lighting / AO ─────────────────────────
        if (settings.containsKey("smoothLighting")) {
            boolean smooth = toBool(settings.get("smoothLighting"), true);
            options.ambientOcclusion().set(smooth);
            changed = true;
        }

        // ── Save only if changed ─────────────────────────
        if (changed) {
            options.save(); // modern replacement for write()
            LoggerUtil.debug("Applied settings from '{}'", sourceName);
        }
    }

    // ── Utils ───────────────────────────────────────────

    private static int toInt(Object val, int fallback) {
        if (val instanceof Number n) return n.intValue();

        if (val instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (Exception ignored) {}
        }

        return fallback;
    }

    private static boolean toBool(Object val, boolean fallback) {
        if (val instanceof Boolean b) return b;

        if (val instanceof String s) return Boolean.parseBoolean(s);

        return fallback;
    }
}