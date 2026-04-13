package com.teamaddoners.core;

import net.minecraft.client.Minecraft;

/**
 * Monitors the current Minecraft client FPS with a rolling average
 * to smooth out momentary spikes and produce a stable reading.
 */
public final class FPSMonitor {

    private static final int SAMPLE_SIZE = 10;

    private final int[] samples = new int[SAMPLE_SIZE];
    private int head = 0;
    private int sampleCount = 0;

    /**
     * Records the current raw FPS into the rolling sample buffer.
     * Should be called once per optimizer cycle (every 20 ticks).
     */
    public void tick() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        int raw = client.getFps();
        samples[head] = raw;
        head = (head + 1) % SAMPLE_SIZE;
        if (sampleCount < SAMPLE_SIZE) sampleCount++;
    }

    /**
     * Returns the rolling average FPS over the last {@value #SAMPLE_SIZE} cycles.
     * Falls back to the most recent raw value if only one sample exists.
     *
     * @return Smoothed FPS value (>= 0).
     */
    public int getFPS() {
        if (sampleCount == 0) {
            Minecraft client = Minecraft.getInstance();
            return (client != null) ? client.getFps() : 0;
        }
        int sum = 0;
        for (int i = 0; i < sampleCount; i++) {
            sum += samples[i];
        }
        return sum / sampleCount;
    }

    /**
     * Returns the most recent raw FPS without averaging.
     */
    public int getRawFPS() {
        Minecraft client = Minecraft.getInstance();
        return (client != null) ? client.getFps() : 0;
    }

    /** Resets the sample buffer. */
    public void reset() {
        head = 0;
        sampleCount = 0;
    }
}