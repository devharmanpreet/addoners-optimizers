package com.teamaddoners.adaptive;

import com.teamaddoners.profile.AddonersProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Evaluates the {@code dynamicRules} section of an {@link AddonersProfile} against
 * the current FPS and produces a merged override settings map to be applied on top
 * of the profile's base settings.
 *
 * <p>Rule keys (evaluated in ascending severity):
 * <ul>
 *   <li>{@code fpsBelow60} — applied when FPS &lt; 60</li>
 *   <li>{@code fpsBelow30} — applied on top of above when FPS &lt; 30</li>
 *   <li>{@code fpsBelow20} — applied on top of above when FPS &lt; 20</li>
 * </ul>
 */
public final class RuleEngine {

    private RuleEngine() {}

    /**
     * Evaluates all applicable dynamic rules for the current FPS reading.
     *
     * @param profile Current active profile. Returns empty map if null.
     * @param fps     Current measured FPS.
     * @return A merged map of setting overrides to apply, ordered by severity (most urgent wins).
     *         Returns an empty map when no rules fire.
     */
    public static Map<String, Object> evaluate(AddonersProfile profile, int fps) {
        if (profile == null) return new HashMap<>();

        Map<String, Map<String, Object>> rules = profile.getDynamicRules();
        if (rules == null || rules.isEmpty()) return new HashMap<>();

        Map<String, Object> merged = new HashMap<>();

        // Layer overrides from least to most severe so higher-severity keys take precedence
        applyIf(merged, rules, "fpsBelow60", fps < 60);
        applyIf(merged, rules, "fpsBelow30", fps < 30);
        applyIf(merged, rules, "fpsBelow20", fps < 20);

        return merged;
    }

    /**
     * Applies a named rule's overrides into the merged map if the condition is true.
     */
    private static void applyIf(
            Map<String, Object> merged,
            Map<String, Map<String, Object>> rules,
            String ruleKey,
            boolean condition) {

        if (!condition) return;
        Map<String, Object> ruleSettings = rules.get(ruleKey);
        if (ruleSettings != null) {
            merged.putAll(ruleSettings);
        }
    }
}
