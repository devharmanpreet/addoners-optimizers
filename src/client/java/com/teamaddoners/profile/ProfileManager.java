package com.teamaddoners.profile;

import com.teamaddoners.util.FileUtil;
import com.teamaddoners.util.LoggerUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for all loaded .addoners profiles.
 * Manages the currently active profile, default fallback, and profile switching.
 *
 * <p>Profiles are stored in: {@code config/teamaddoners/profiles/}
 */
public final class ProfileManager {

    private static final String DEFAULT_PROFILE_NAME = "default";
    private static final String PROFILES_SUBDIR = "teamaddoners/profiles";

    private final Path profilesDir;
    private final Map<String, AddonersProfile> loadedProfiles = new HashMap<>();

    private AddonersProfile activeProfile;

    public ProfileManager() {
        this.profilesDir = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(PROFILES_SUBDIR);
        FileUtil.ensureDir(profilesDir);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────────

    /**
     * Initializes the manager: creates default profiles if absent, then loads all profiles.
     */
    public void initialize() {
        seedDefaultProfiles();
        reloadAll();

        // Set active profile to "default" first; fall back to first available
        AddonersProfile def = loadedProfiles.get(DEFAULT_PROFILE_NAME);
        if (def != null) {
            activeProfile = def;
            LoggerUtil.info("Active profile set to '{}'", activeProfile.getName());
        } else if (!loadedProfiles.isEmpty()) {
            activeProfile = loadedProfiles.values().iterator().next();
            LoggerUtil.warn("'default' profile missing — falling back to '{}'", activeProfile.getName());
        } else {
            LoggerUtil.warn("No profiles found — optimizer will run in fallback mode.");
            activeProfile = null;
        }
    }

    /**
     * Reloads all .addoners files from disk.
     */
    public void reloadAll() {
        loadedProfiles.clear();
        List<Path> files = FileUtil.listAddoners(profilesDir);
        for (Path file : files) {
            AddonersProfile p = ProfileLoader.load(file);
            if (p != null) {
                loadedProfiles.put(p.getName().toLowerCase(), p);
            }
        }
        LoggerUtil.info("Loaded {} profile(s) from {}", loadedProfiles.size(), profilesDir);
    }

    /**
     * Switches the active profile to the named profile (case-insensitive).
     *
     * @param name Profile name.
     * @return true if the switch succeeded.
     */
    public boolean switchProfile(String name) {
        if (name == null) return false;
        AddonersProfile p = loadedProfiles.get(name.toLowerCase());
        if (p == null) {
            LoggerUtil.warn("Cannot switch — profile '{}' not found.", name);
            return false;
        }
        activeProfile = p;
        LoggerUtil.info("Switched active profile to '{}'", name);
        return true;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────────

    /** Returns the currently active profile, or null if none is loaded. */
    public AddonersProfile getActiveProfile() {
        return activeProfile;
    }

    /** Returns an unmodifiable view of all loaded profiles. */
    public Map<String, AddonersProfile> getAllProfiles() {
        return Collections.unmodifiableMap(loadedProfiles);
    }

    /** Path to the profiles directory on disk. */
    public Path getProfilesDir() {
        return profilesDir;
    }

    // ── Default Profile Seeding ───────────────────────────────────────────────────

    /**
     * Writes built-in profiles to disk only if they do not already exist.
     */
    private void seedDefaultProfiles() {
        writeIfMissing("default", DefaultProfiles.createDefault());
        writeIfMissing("quartzglow", DefaultProfiles.createQuartzglow());
        writeIfMissing("skygleam", DefaultProfiles.createSkygleam());
    }

    private void writeIfMissing(String name, AddonersProfile profile) {
        Path dest = profilesDir.resolve(name + ".addoners");
        if (!dest.toFile().exists()) {
            ProfileLoader.save(profile, dest);
            LoggerUtil.info("Seeded default profile: {}", name);
        }
    }
}
