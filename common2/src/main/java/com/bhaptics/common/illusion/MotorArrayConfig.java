package com.bhaptics.common.illusion;

/**
 * Created by westside on 2016-04-29.
 */
public class MotorArrayConfig {
    private int column;
    private int row;
    private int gridSize;
    private int resolution;

    public MotorArrayConfig(int column, int row, int resolution) {
        if (column < 0 || row < 0) {
            throw new IllegalArgumentException("argument is negative");
        }

        this.column = column;
        this.row = row;
        this.gridSize = column * row;
        this.resolution = resolution;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public int getGridSize() {
        return gridSize;
    }

    public int getResolution() {
        return resolution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MotorArrayConfig that = (MotorArrayConfig) o;

        if (column != that.column) return false;
        if (row != that.row) return false;
        if (gridSize != that.gridSize) return false;
        return resolution == that.resolution;

    }

    @Override
    public int hashCode() {
        int result = column;
        result = 31 * result + row;
        result = 31 * result + gridSize;
        result = 31 * result + resolution;
        return result;
    }
}
