package com.bhaptics.common.illusion;

/**
 * Created by westside on 2016-04-29.
 */
public class CoordinatePoint {
    private int x;
    private int y;
    private float intensity;

    public CoordinatePoint(int x, int y, float intensity) {
        this.x = x;
        this.y = y;
        this.intensity = CommonUtils.rangeFloat(intensity);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getIntensity() {
        return intensity;
    }

    @Override
    public String toString() {
        return "CoordinatePoint{" +
                "x=" + x +
                ", y=" + y +
                ", intensity=" + intensity +
                '}';
    }
}
