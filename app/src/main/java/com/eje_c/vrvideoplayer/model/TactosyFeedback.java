package com.eje_c.vrvideoplayer.model;

import java.util.Arrays;

/**
 * Created by westside on 2016-08-31.
 */
public class TactosyFeedback {
    public PositionType position;
    public FeedbackMode mode;
    public byte[] values;

    public PositionType getPosition() {
        return position;
    }

    public void setPosition(PositionType position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "TactosyFeedback{" +
                "values=" + Arrays.toString(values) +
                ", mode=" + mode +
                ", position=" + position +
                '}';
    }

    public FeedbackMode getMode() {
        return mode;
    }

    public void setMode(FeedbackMode mode) {
        this.mode = mode;
    }

    public byte[] getValues() {
        return values;
    }

    public void setValues(byte[] values) {
        this.values = values;
    }
}
