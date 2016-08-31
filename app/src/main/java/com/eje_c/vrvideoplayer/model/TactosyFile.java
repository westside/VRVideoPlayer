package com.eje_c.vrvideoplayer.model;

import java.util.Map;

/**
 * Created by westside on 2016-08-31.
 */
public class TactosyFile {
    private int intervalMillis;
    private int size;
    private int durationMillis;

    public Map<Integer, TactosyFeedback[]> feedback;

    public int getIntervalMillis() {
        return intervalMillis;
    }

    public void setIntervalMillis(int intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(int durationMillis) {
        this.durationMillis = durationMillis;
    }

    public Map<Integer, TactosyFeedback[]> getFeedback() {
        return feedback;
    }

    public void setFeedback(Map<Integer, TactosyFeedback[]> feedback) {
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return "TactosyFile{" +
                "intervalMillis=" + intervalMillis +
                ", size=" + size +
                ", durationMillis=" + durationMillis +
                ", feedback=" + feedback +
                '}';
    }
}
