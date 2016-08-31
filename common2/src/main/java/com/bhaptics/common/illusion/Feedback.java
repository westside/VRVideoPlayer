package com.bhaptics.common.illusion;

import java.util.Arrays;

/**
 * Created by westside on 2016-04-28.
 */
public class Feedback {
    private int gridSize;
    private int[] values;
    private int resolution;
    private byte[] bytes;

    public Feedback(int gridSize) {
        if (gridSize < 0) {
            throw new IllegalArgumentException("grid size is negative.");
        }

        this.gridSize = gridSize;
        this.values = new int[gridSize];
        this.bytes = new byte[gridSize];
    }

    public Feedback(int gridSize, int[] values, int resolution) {
        this.gridSize = gridSize;
        this.values = values;
        this.resolution = resolution;
        this.bytes = new byte[gridSize];

        for (int i = 0 ; i < gridSize ; i++) {
            bytes[i] = (byte) (values[i]);
        }
    }

    public int[] getValues() {
        return values;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "gridSize=" + gridSize +
                ", resolution=" + resolution +
                ", values=" + Arrays.toString(values) +
                ", bytes=" + CommonUtils.bytesToHexString(bytes) +
                '}';
    }

    private static Feedback emptyFeedback;
    public static Feedback emptyFeedback(int gridSize) {
        if (emptyFeedback == null || emptyFeedback.gridSize != gridSize) {
            emptyFeedback = new Feedback(gridSize);
        }

        return emptyFeedback;
    }
}
