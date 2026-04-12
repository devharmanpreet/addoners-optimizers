package com.teamaddoners.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.teamaddoners.util.FileUtil;
import com.teamaddoners.util.LoggerUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Persistent configuration for the Addoners Optimizer mod.
 * Stored at {@code config/teamaddoners/modconfig.json}.
 *
 * <p>All fields have safe defaults and are validated after each load to prevent
 * out-of-range values (e.g., a zero tick-interval) from causing runtime issues.
 */
public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILENAME = "modconfig.json";

    // ── Bounds ───────────────────────────────────────────────────────────────────

    /** Minimum tick interval — prevents running the optimizer every single tick. */
    public static final int MIN_INTERVAL_TICKS = 1;

    /** Maximum tick interval — 200 ticks = 10 seconds is a reasonable upper bound. */
    public static final int MAX_INTERVAL_TICKS = 200;

    // ── Fields (mutable at runtime) ──────────────────────────────────────────────

    /** Master switch — disabling this completely bypasses all optimization logic. */
    public boolean enabled = true;

    /** When true, extra diagnostic information is logged every optimizer cycle via INFO level. */
    public boolean debugLogs = false;

    /**
     * Minimum ticks between optimizer cycles. Default 20 (= 1 second at 20 TPS).
     * Clamped to [{@value #MIN_INTERVAL_TICKS}, {@value #MAX_INTERVAL_TICKS}] after loading.
     */
    public int optimizerIntervalTicks = 20;

    // ── Singleton ────────────────────────────────────────────────────────────────

    private static ModConfig instance;

    private ModConfig() {}

    public static ModConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    // ── Persistence ───────────────────────────────────────────────────────────────

    private static Path configPath() {
        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve("teamaddoners")
                .resolve(CONFIG_FILENAME);
    }

    /**
     * Loads config from disk, validates it, and returns the result.
     * Falls back to defaults if the file is absent, empty, or corrupted.
     */
    public static ModConfig load() {
        Path path = configPath();
        if (!path.toFile().exists()) {
            ModConfig defaults = new ModConfig();
            defaults.save();
            LoggerUtil.info("Created default mod config at {}", path);
            return defaults;
        }

        String json = FileUtil.readFile(path);
        if (json == null || json.isBlank()) {
            LoggerUtil.warn("Mod config file is empty or unreadable — using defaults.");
            return new ModConfig();
        }

        try {
            ModConfig loaded = GSON.fromJson(json, ModConfig.class);
            if (loaded == null) {
                LoggerUtil.warn("Mod config parsed as null — using defaults.");
                return new ModConfig();
            }
            loaded.validate();
            LoggerUtil.info("Loaded mod config from {}", path);
            return loaded;
        } catch (Exception e) {
            LoggerUtil.error("Failed to parse mod config — using defaults. Cause: " + e.getMessage(), e);
            return new ModConfig();
        }
    }

    /**
     * Validates and clamps all fields to safe ranges.
     * Called automatically after deserialization.
     */
    private void validate() {
        if (optimizerIntervalTicks < MIN_INTERVAL_TICKS || optimizerIntervalTicks > MAX_INTERVAL_TICKS) {
            int original = optimizerIntervalTicks;
            optimizerIntervalTicks = Math.max(MIN_INTERVAL_TICKS, Math.min(MAX_INTERVAL_TICKS, optimizerIntervalTicks));
            LoggerUtil.warn(
                "optimizerIntervalTicks={} is out of valid range [{}, {}] — clamped to {}.",
                original, MIN_INTERVAL_TICKS, MAX_INTERVAL_TICKS, optimizerIntervalTicks
            );
        }
    }

    /**
     * Saves the current configuration state to disk.
     */
    public void save() {
        Path path = configPath();
        FileUtil.writeFile(path, GSON.toJson(this));
    }
}
