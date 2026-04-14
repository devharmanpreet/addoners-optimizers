package com.teamaddoners.shader;

import com.teamaddoners.util.LoggerUtil;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Attempts to detect whether a shader pack is currently active.
 *
 * <p><b>Detection strategy (v1.5 — safe, mixin-free):</b>
 * <ol>
 *   <li>System property {@code addoners.shaderName} — for testing and integration bridges.</li>
 *   <li>Iris detection via {@link FabricLoader#isModLoaded} + reflection call to
 *       {@code net.irisshaders.iris.api.v0.IrisApi.getInstance().isShaderPackInUse()}.
 *       This is deliberately done with reflection so the mod compiles and runs
 *       without Iris on the classpath.</li>
 *   <li>Canvas detection via {@link FabricLoader#isModLoaded} — Canvas always
 *       replaces the vanilla render pipeline, so its presence implies shaders.</li>
 *   <li>Fallback: returns {@code "none"}.</li>
 * </ol>
 *
 * <p>No specific mod is <em>required</em>: detection degrades gracefully if no
 * shader loader is installed.
 */
public final class ShaderDetector {

    /** System property key — allows tests and external bridges to override detection. */
    private static final String SYSTEM_PROP_KEY = "addoners.shaderName";

    /** Iris mod ID on Modrinth / CurseForge — stable across versions. */
    private static final String IRIS_MOD_ID = "iris";

    /** Canvas mod ID (alternative PBR/shader pipeline). */
    private static final String CANVAS_MOD_ID = "canvas";

    // ── Internal state ─────────────────────────────────────────────────────────

    private String  currentShader  = "none";
    private boolean irisPresent    = false;
    private boolean canvasPresent  = false;
    private boolean checkedLoaders = false;

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Samples the current shader state. Call periodically — NOT every tick.
     */
    public void refresh() {
        ensureLoaderCheck();
        String detected = detect();
        if (!detected.equals(currentShader)) {
            LoggerUtil.info("Shader change detected: '{}' → '{}'", currentShader, detected);
            currentShader = detected;
        }
    }

    /**
     * Returns the last detected shader identifier (lowercase).
     *
     * @return Shader name, or {@code "none"} if no shader pack is active.
     */
    public String getActiveShader() {
        return currentShader;
    }

    /**
     * Returns {@code true} if any shader (other than {@code "none"}) is currently loaded.
     */
    public boolean isShaderActive() {
        return !"none".equalsIgnoreCase(currentShader);
    }

    // ── Internal ───────────────────────────────────────────────────────────────

    /** Checks which shader loaders are on the classpath — run once at startup. */
    private void ensureLoaderCheck() {
        if (checkedLoaders) return;
        FabricLoader loader = FabricLoader.getInstance();
        irisPresent    = loader.isModLoaded(IRIS_MOD_ID);
        canvasPresent  = loader.isModLoaded(CANVAS_MOD_ID);
        checkedLoaders = true;
        LoggerUtil.info("Shader loader scan: iris={}, canvas={}", irisPresent, canvasPresent);
    }

    private String detect() {
        // Priority 1: explicit system property (testing / external bridges)
        String sysProp = System.getProperty(SYSTEM_PROP_KEY);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim().toLowerCase();
        }

        // Priority 2: Iris — query via reflection to avoid hard compile-time dependency
        if (irisPresent) {
            String irisResult = queryIrisApi();
            if (!"none".equals(irisResult)) return irisResult;
        }

        // Priority 3: Canvas — its presence implies a full shader pipeline replacement
        if (canvasPresent) {
            return "canvas";
        }

        return "none";
    }

    /**
     * Attempts to call {@code IrisApi.getInstance().isShaderPackInUse()} via reflection.
     * Returns {@code "iris"} if a pack is in use, {@code "none"} otherwise.
     * Any reflection error is silently swallowed — Iris API compatibility is best-effort.
     */
    private static String queryIrisApi() {
        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance   = apiClass.getMethod("getInstance").invoke(null);
            boolean inUse     = (boolean) apiClass.getMethod("isShaderPackInUse").invoke(instance);
            return inUse ? "iris" : "none";
        } catch (Exception ignored) {
            // Iris is present but its API class or method signature differ — treat as no-shader.
            return "none";
        }
    }
}
