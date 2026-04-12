package com.teamaddoners.profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating the bundled default .addoners profiles.
 * These are written to disk on first run by {@link ProfileManager}.
 */
final class DefaultProfiles {

    private DefaultProfiles() {}

    static AddonersProfile createDefault() {
        AddonersProfile p = new AddonersProfile();
        p.setName("default");
        p.setType("optimizer_profile");
        p.setVersion(1);
        p.setShader("none");
        p.setPerformanceTier("MID_END");

        Map<String, Object> settings = new HashMap<>();
        settings.put("renderDistance", 10);
        settings.put("particles", 1);
        settings.put("disableShaders", false);
        settings.put("shadowQuality", 1);
        settings.put("smoothLighting", true);
        p.setSettings(settings);

        Map<String, Map<String, Object>> rules = new HashMap<>();

        Map<String, Object> below60 = new HashMap<>();
        below60.put("renderDistance", 6);
        below60.put("particles", 0);
        rules.put("fpsBelow60", below60);

        Map<String, Object> below30 = new HashMap<>();
        below30.put("renderDistance", 4);
        below30.put("disableShaders", true);
        below30.put("particles", 0);
        rules.put("fpsBelow30", below30);

        Map<String, Object> below20 = new HashMap<>();
        below20.put("renderDistance", 2);
        below20.put("disableShaders", true);
        below20.put("particles", 0);
        below20.put("smoothLighting", false);
        rules.put("fpsBelow20", below20);

        p.setDynamicRules(rules);
        return p;
    }

    static AddonersProfile createQuartzglow() {
        AddonersProfile p = new AddonersProfile();
        p.setName("quartzglow");
        p.setType("optimizer_profile");
        p.setVersion(1);
        p.setShader("quartzglow");
        p.setPerformanceTier("HIGH_END");

        Map<String, Object> settings = new HashMap<>();
        settings.put("renderDistance", 14);
        settings.put("particles", 1);
        settings.put("disableShaders", false);
        settings.put("shadowQuality", 2);
        settings.put("smoothLighting", true);
        p.setSettings(settings);

        Map<String, Map<String, Object>> rules = new HashMap<>();

        Map<String, Object> below60 = new HashMap<>();
        below60.put("renderDistance", 10);
        below60.put("shadowQuality", 1);
        rules.put("fpsBelow60", below60);

        Map<String, Object> below30 = new HashMap<>();
        below30.put("renderDistance", 7);
        below30.put("shadowQuality", 0);
        below30.put("particles", 0);
        rules.put("fpsBelow30", below30);

        Map<String, Object> below20 = new HashMap<>();
        below20.put("renderDistance", 5);
        below20.put("disableShaders", true);
        below20.put("particles", 0);
        rules.put("fpsBelow20", below20);

        p.setDynamicRules(rules);
        return p;
    }

    static AddonersProfile createSkygleam() {
        AddonersProfile p = new AddonersProfile();
        p.setName("skygleam");
        p.setType("optimizer_profile");
        p.setVersion(1);
        p.setShader("skygleam");
        p.setPerformanceTier("MID_END");

        Map<String, Object> settings = new HashMap<>();
        settings.put("renderDistance", 12);
        settings.put("particles", 1);
        settings.put("disableShaders", false);
        settings.put("shadowQuality", 1);
        settings.put("smoothLighting", true);
        p.setSettings(settings);

        Map<String, Map<String, Object>> rules = new HashMap<>();

        Map<String, Object> below60 = new HashMap<>();
        below60.put("renderDistance", 8);
        below60.put("particles", 0);
        rules.put("fpsBelow60", below60);

        Map<String, Object> below30 = new HashMap<>();
        below30.put("renderDistance", 5);
        below30.put("shadowQuality", 0);
        below30.put("disableShaders", false);
        rules.put("fpsBelow30", below30);

        Map<String, Object> below20 = new HashMap<>();
        below20.put("renderDistance", 3);
        below20.put("disableShaders", true);
        below20.put("particles", 0);
        rules.put("fpsBelow20", below20);

        p.setDynamicRules(rules);
        return p;
    }
}
