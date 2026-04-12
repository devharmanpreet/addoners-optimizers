package com.teamaddoners.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.teamaddoners.util.FileUtil;
import com.teamaddoners.util.LoggerUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Persistent configuration for the Addoners Optimizer mod.
 * Stored at config/teamaddoners/modconfig.json.
 */
public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILENAME = "modconfig.json";

    // ── Fields (mutable at runtime) ──────────────────────────────────────────────

    /** Master switch — disabling this completely bypasses all optimization logic. */
    public boolean enabled = true;

    /** When true, extra debug information is logged to the console every optimizer cycle. */
    public boolean debugLogs = false;

    /** Minimum ticks between optimizer cycles. Default 20 (= 1 second at 20 TPS). */
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
     * Loads config from disk, or creates defaults if the file doesn't exist.
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
            LoggerUtil.warn("Mod config file is empty, using defaults.");
            return new ModConfig();
        }

        try {
            ModConfig loaded = GSON.fromJson(json, ModConfig.class);
            LoggerUtil.info("Loaded mod config from {}", path);
            return loaded;
        } catch (Exception e) {
            LoggerUtil.error("Failed to parse mod config, using defaults.", e);
            return new ModConfig();
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
