package com.bhaptics.common.illusion;

/**
 * Created by westside on 2016-04-28.
 */
public class Point {
    private float x;
    private float y;
    private float intensity;

    public Point(float x, float y, float intensity) {
        this.x = CommonUtils.rangeFloat(x);
        this.y = CommonUtils.rangeFloat(y);
        this.intensity = CommonUtils.rangeFloat(intensity);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getIntensity() {
        return intensity;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", intensity=" + intensity +
                '}';
    }
}
