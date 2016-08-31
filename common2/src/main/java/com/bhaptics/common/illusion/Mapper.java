package com.bhaptics.common.illusion;

import java.util.Arrays;
import java.util.List;

/**
 * Created by westside on 2016-04-28.
 */
public class Mapper {

    public static final double CONSTANT = Math.log(3) - Math.log(1);
    public static final double LOG1 = Math.log(1);

    private boolean isRegionStretch = true;

    private MotorArrayConfig config;
    private Interpolation type;

    public Mapper(MotorArrayConfig config, Interpolation type) {
        this.config = config;
        this.type = type;
    }

    public Feedback rasterizationCoordinates(List<CoordinatePoint> points) {
        int[] result = rasterizationCoordinate(points);
        return new Feedback(config.getGridSize(), result, config.getResolution());
    }

    public int[] rasterization(CoordinatePoint point) {
        return rasterizationCoordinate(Arrays.asList(point));
    }

    private int[] rasterizationCoordinate(List<CoordinatePoint> points) {
        int[] result = new int[config.getGridSize()];
        for (CoordinatePoint point : points) {
            int x = CommonUtils.rangeIntFromTo(point.getX(),  0, config.getColumn() - 1);
            int y = CommonUtils.rangeIntFromTo(point.getY(),  0, config.getRow() - 1);

            int index = x + y * config.getColumn();
            int val = convertToIntValue(point.getIntensity());

            result[index] = val;
        }
        return result;
    }


    // TODO use intensity to generate feedback
    public int[] rasterization(Point point) {
        int[] result = new int[config.getGridSize()];

        int tmpX = horizontalQuantized(point.getX());
        int tmpY = verticalQuantized(point.getY());

        float intensity = point.getIntensity();

        int boxIndexX = tmpX / config.getResolution();
        int boxIndexY = tmpY  / config.getResolution();

        int innerBoxX = tmpX - config.getResolution() * boxIndexX;
        int innerBoxY = tmpY - config.getResolution() * boxIndexY;

        // using barycentric coordinates on a right-angled triangle
        int leftTopIndex = boxIndexX + config.getColumn() * boxIndexY;
        int rightTopIndex = leftTopIndex + 1;
        int leftBottomIndex = leftTopIndex + config.getColumn();
        int rightBottomIndex = leftBottomIndex + 1;

        if (rightTopIndex >= config.getGridSize()) {
            rightTopIndex =  - 1;
        }

        if (rightBottomIndex >= config.getGridSize()) {
            rightBottomIndex = - 1;
        }

        if (innerBoxX >= innerBoxY) {
            result[leftTopIndex] = convertToIntValue(intensity * normalize(config.getResolution() - innerBoxX));

            if (rightTopIndex >= 0) {
                result[rightTopIndex] = convertToIntValue(intensity * normalize(innerBoxX - innerBoxY));
            }


            if (rightBottomIndex >= 0) {
                result[rightBottomIndex] = convertToIntValue(intensity * normalize(innerBoxY));
            }
        } else {
            result[leftTopIndex] = convertToIntValue(intensity * normalize(config.getResolution() - innerBoxY));
            result[leftBottomIndex] = convertToIntValue(intensity * normalize(innerBoxY - innerBoxX));

            if (rightBottomIndex >= 0) {
                result[rightBottomIndex] = convertToIntValue(intensity * normalize(innerBoxX));
            }
        }

        return result;
    }


    public Feedback rasterization(List<Point> points) {
        int[] result = new int[config.getGridSize()];

        for (Point point : points) {
            int[] newValue = rasterization(point);

            result = add(result, newValue);
        }

        return new Feedback(config.getGridSize(), result, config.getResolution());
    }

    private int[] add(int[] a, int[] b) {
        int[] result = new int[config.getGridSize()];
        for (int i = 0 ; i < config.getGridSize() ; i++) {
            result[i] = Math.min(a[i] + b[i], config.getResolution() - 1);
        }

        return result;
    }

    private int horizontalQuantized(float val) {
        float rValue = val;
        if (isRegionStretch) {
            rValue = stretch(val);
        }


        return (int) Math.round(rValue * (config.getColumn() - 1) * (config.getResolution()));
    }

    private float stretch(float val) {
        float rValue = 1.2f * val - 0.1f;
        if (rValue < 0) {
            rValue = 0f;
        } else if (rValue > 0.999f) { // not 1 but 0.99, to avoid index overflow
            rValue = 0.999f;
        }

        return rValue;
    }

    private int verticalQuantized(float val) {
        float rValue = val;
        if (isRegionStretch) {
            rValue = stretch(val);
        }

        return (int) Math.round(rValue * (config.getRow() - 1) * (config.getResolution()));
    }

    private float normalize(int val) {
        return(float)val / config.getResolution();
    }

    private int convertToIntValue(float val) {
        return (int)(applyInterpolationModel(val) * config.getResolution());
    }

    private float applyInterpolationModel(float val) {
        if (type.equals(Interpolation.Linear)) {
            return val;
        }

        float changed = (float)(val * CONSTANT - LOG1);
        if (changed >= 0.999f) {
            changed = 0.999f;
        }

        return changed;
    }

    public enum Interpolation {
        Linear, Log;
    }
}
