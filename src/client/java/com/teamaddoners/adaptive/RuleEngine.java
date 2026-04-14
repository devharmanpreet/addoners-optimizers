package com.teamaddoners.adaptive;

import com.teamaddoners.profile.AddonersProfile;
import com.teamaddoners.util.LoggerUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Evaluates dynamic rules of an AddonersProfile based on FPS.
 */
public final class RuleEngine {

    private RuleEngine() {}

    public static Map<String, Object> evaluate(AddonersProfile profile, int fps) {
        if (profile == null) return new HashMap<>();

        Map<String, Map<String, Object>> rules = profile.getDynamicRules();
        if (rules == null || rules.isEmpty()) return new HashMap<>();

        Map<String, Object> merged = new HashMap<>();

        // Track applied rules (for debugging/logging)
        int applied = 0;

        // 🔥 Order matters: low → high severity
        applied += applyIf(merged, rules, "fpsBelow60", fps < 60);
        applied += applyIf(merged, rules, "fpsBelow30", fps < 30);
        applied += applyIf(merged, rules, "fpsBelow20", fps < 20);

        // 🔥 Debug logging (only if rules triggered)
        if (applied > 0) {
            LoggerUtil.debug(
                    "RuleEngine: fps={} appliedRules={} finalOverrides={}",
                    fps,
                    applied,
                    merged.size()
            );
        }

        return merged;
    }

    /**
     * Applies a rule if condition is met.
     * @return 1 if applied, 0 otherwise
     */
    private static int applyIf(
            Map<String, Object> merged,
            Map<String, Map<String, Object>> rules,
            String ruleKey,
            boolean condition
    ) {
        if (!condition) return 0;

        Map<String, Object> ruleSettings = rules.get(ruleKey);
        if (ruleSettings == null || ruleSettings.isEmpty()) return 0;

        // 🔥 Safe merge (override previous values)
        for (Map.Entry<String, Object> entry : ruleSettings.entrySet()) {
            merged.put(entry.getKey(), entry.getValue());
        }

        return 1;
    }
}