package com.teamaddoners.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized logging utility for Addoners Optimizer.
 * All log messages are prefixed with [Addoners Optimizer] for easy filtering.
 *
 * <p>Design note: SLF4J has a critical ambiguity when the last argument of a
 * varargs call is a {@link Throwable} — it is treated as the "cause" only when
 * it is the sole trailing argument after the format string. To avoid any
 * accidental overload resolution surprises we keep the Throwable overloads
 * explicitly separate and name them unambiguously.
 */
public final class LoggerUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger("addoners_optimizer");
    private static final String PREFIX = "[Addoners Optimizer] ";

    private LoggerUtil() {}

    // ── Info ─────────────────────────────────────────────────────────────────────

    public static void info(String message, Object... args) {
        LOGGER.info(PREFIX + message, args);
    }

    // ── Warn ─────────────────────────────────────────────────────────────────────

    public static void warn(String message, Object... args) {
        LOGGER.warn(PREFIX + message, args);
    }

    // ── Error — two clearly distinct overloads ────────────────────────────────────

    /**
     * Logs an error with format-string arguments (no attached throwable).
     * Use this for messages that don't need a stack trace.
     */
    public static void error(String message, Object... args) {
        LOGGER.error(PREFIX + message, args);
    }

    /**
     * Logs an error with an attached throwable so the full stack trace is preserved.
     * <p><b>Always prefer this overload when a {@link Throwable} is available.</b>
     * Passing a throwable through the varargs overload causes SLF4J to format it
     * as a plain string and silently discard the stack trace.
     */
    public static void error(String message, Throwable throwable) {
        LOGGER.error(PREFIX + message, throwable);
    }

    // ── Debug ────────────────────────────────────────────────────────────────────

    /**
     * Logs at DEBUG level. Visible only when the logger's effective level is DEBUG or lower.
     */
    public static void debug(String message, Object... args) {
        LOGGER.debug(PREFIX + message, args);
    }

    /**
     * Conditionally logs at INFO level with a [DEBUG] tag, controlled by a runtime flag.
     * Used for per-cycle diagnostic output when {@code config.debugLogs} is true.
     *
     * <p>Note: intentionally uses INFO so the message always appears when the flag is on,
     * regardless of the logger's configured level.
     */
    public static void debugIf(boolean enabled, String message, Object... args) {
        if (enabled) {
            LOGGER.info(PREFIX + "[DEBUG] " + message, args);
        }
    }
}
