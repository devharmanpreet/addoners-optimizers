package com.teamaddoners.profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable(-by-convention) data model for a .addoners profile file.
 * All fields are populated by {@link ProfileLoader} via Gson deserialization.
 *
 * <p>Example .addoners JSON structure:
 * <pre>
 * {
 *   "type": "optimizer_profile",
 *   "version": 1,
 *   "shader": "quartzglow",
 *   "performanceTier": "MID_END",
 *   "settings": {
 *     "renderDistance": 10,
 *     "particles": 1,
 *     "disableShaders": false,
 *     "shadowQuality": 1
 *   },
 *   "dynamicRules": {
 *     "fpsBelow60": { "renderDistance": 6 },
 *     "fpsBelow30": { "disableShaders": true, "renderDistance": 4 }
 *   }
 * }
 * </pre>
 */
public class AddonersProfile {

    /** Discriminator — always "optimizer_profile" for standard profiles. */
    private String type = "optimizer_profile";

    /** Schema version for forward-compatibility. */
    private int version = 1;

    /** Name of the shader this profile targets, or "none" for generic profiles. */
    private String shader = "none";

    /**
     * Target performance tier: "LOW_END", "MID_END", or "HIGH_END".
     * Used to select appropriate defaults when settings are absent.
     */
    private String performanceTier = "MID_END";

    /** Profile name (usually inferred from the filename). */
    private String name = "Unnamed Profile";

    /**
     * Static settings applied at profile load time.
     * Supported keys: renderDistance (int), particles (int 0-2), disableShaders (bool),
     * shadowQuality (int 0-3), smoothLighting (bool).
     */
    private Map<String, Object> settings = new HashMap<>();

    /**
     * Condition-to-override map evaluated at runtime by {@link com.teamaddoners.adaptive.RuleEngine}.
     * Keys: "fpsBelow60", "fpsBelow30", "fpsBelow20"
     * Values: partial settings maps applied when the condition is true.
     */
    private Map<String, Map<String, Object>> dynamicRules = new HashMap<>();

    // ── Accessors ─────────────────────────────────────────────────────────────────

    public String getType()            { return type; }
    public int    getVersion()         { return version; }
    public String getShader()          { return shader != null ? shader : "none"; }
    public String getPerformanceTier() { return performanceTier != null ? performanceTier : "MID_END"; }
    public String getName()            { return name != null ? name : "Unnamed Profile"; }
    public Map<String, Object> getSettings()    { return settings != null ? settings : new HashMap<>(); }
    public Map<String, Map<String, Object>> getDynamicRules() {
        return dynamicRules != null ? dynamicRules : new HashMap<>();
    }

    // ── Mutators (used during construction / testing) ──────────────────────────

    public void setType(String type)                      { this.type = type; }
    public void setVersion(int version)                   { this.version = version; }
    public void setShader(String shader)                  { this.shader = shader; }
    public void setPerformanceTier(String tier)           { this.performanceTier = tier; }
    public void setName(String name)                      { this.name = name; }
    public void setSettings(Map<String, Object> settings) { this.settings = settings; }
    public void setDynamicRules(Map<String, Map<String, Object>> rules) { this.dynamicRules = rules; }

    /**
     * Convenience — retrieves a typed setting value with a fallback default.
     *
     * @param key          Setting key.
     * @param defaultValue Fallback if the key is absent or has the wrong type.
     * @param <T>          Expected type.
     * @return The setting value or defaultValue.
     */
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String key, T defaultValue) {
        Object raw = settings.get(key);
        if (raw == null) return defaultValue;
        try {
            return (T) raw;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    @Override
    public String toString() {
        return String.format("AddonersProfile[name=%s, shader=%s, tier=%s, rules=%d]",
                name, shader, performanceTier, dynamicRules.size());
    }
}
