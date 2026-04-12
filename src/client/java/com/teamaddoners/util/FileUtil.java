package com.teamaddoners.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * General-purpose file I/O utilities for the Addoners Optimizer ecosystem.
 */
public final class FileUtil {

    private FileUtil() {}

    /**
     * Reads the entire content of a file as a UTF-8 string.
     *
     * @param path Path to the file.
     * @return File contents as a string, or {@code null} if reading fails.
     */
    public static String readFile(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Pass the Throwable directly so the full stack trace is preserved in logs.
            LoggerUtil.error("Failed to read file: " + path, e);
            return null;
        }
    }

    /**
     * Writes a UTF-8 string to the given path, creating parent directories as needed.
     *
     * @param path    Target file path.
     * @param content Content to write.
     * @return {@code true} if write succeeded, {@code false} otherwise.
     */
    public static boolean writeFile(Path path, String content) {
        try {
            ensureDir(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            LoggerUtil.error("Failed to write file: " + path, e);
            return false;
        }
    }

    /**
     * Ensures a directory and all its parents exist.
     *
     * @param dir Directory path to create.
     */
    public static void ensureDir(Path dir) {
        if (dir == null) return;
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            LoggerUtil.error("Failed to create directory: " + dir, e);
        }
    }

    /**
     * Lists all .addoners files in the given directory (non-recursive).
     *
     * @param dir Directory to scan.
     * @return List of paths to .addoners files; empty list if directory is absent or on error.
     */
    public static List<Path> listAddoners(Path dir) {
        if (dir == null || !Files.isDirectory(dir)) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.toString().endsWith(".addoners"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LoggerUtil.error("Failed to list .addoners files in: " + dir, e);
            return Collections.emptyList();
        }
    }
}
