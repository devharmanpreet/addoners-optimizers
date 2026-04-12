package com.teamaddoners.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.teamaddoners.util.FileUtil;
import com.teamaddoners.util.LoggerUtil;

import java.nio.file.Path;

/**
 * Loads and deserializes {@link AddonersProfile} objects from .addoners JSON files.
 */
public final class ProfileLoader {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            // Note: serializeNulls() intentionally omitted — we don't want null fields
            // written into .addoners files; defaults handle absent keys at read time.
            .create();

    private ProfileLoader() {}

    /**
     * Loads an {@link AddonersProfile} from the given .addoners file path.
     *
     * @param path Absolute path to a .addoners file.
     * @return Parsed profile, or null if reading or parsing fails.
     */
    public static AddonersProfile load(Path path) {
        if (path == null) {
            LoggerUtil.warn("ProfileLoader.load() called with null path.");
            return null;
        }

        String json = FileUtil.readFile(path);
        if (json == null || json.isBlank()) {
            LoggerUtil.warn("Profile file is empty or unreadable: {}", path);
            return null;
        }

        try {
            AddonersProfile profile = GSON.fromJson(json, AddonersProfile.class);
            if (profile == null) {
                LoggerUtil.warn("Parsed profile is null for: {}", path);
                return null;
            }

            // Infer name from file stem if not set explicitly
            String fileName = path.getFileName().toString();
            String stem = fileName.endsWith(".addoners")
                    ? fileName.substring(0, fileName.length() - ".addoners".length())
                    : fileName;
            if ("Unnamed Profile".equals(profile.getName())) {
                profile.setName(stem);
            }

            LoggerUtil.info("Loaded profile '{}' from {}", profile.getName(), path);
            return profile;

        } catch (Exception e) {
            // Pass the Throwable directly so the full stack trace appears in logs.
            LoggerUtil.error("Failed to parse profile at: " + path, e);
            return null;
        }
    }

    /**
     * Serializes an {@link AddonersProfile} and writes it to disk.
     * Used for generating default profiles on first run.
     *
     * @param profile Profile to save.
     * @param path    Destination path.
     */
    public static void save(AddonersProfile profile, Path path) {
        String json = GSON.toJson(profile);
        FileUtil.writeFile(path, json);
    }
}
