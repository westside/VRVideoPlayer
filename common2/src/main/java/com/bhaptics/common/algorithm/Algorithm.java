package com.bhaptics.common.algorithm;

import java.util.HashMap;
import java.util.Map;

public class Algorithm {
	private static final String TAG = "Algorithm";

	private int resolution = 160;
	private int column;
	private int row;
	private int gridSize;

	private static final double CONSTANT = Math.log(3) - Math.log(1);
	private static final double LOG1 = Math.log(1);

	public enum Interpolation {
		Linear, Log;
	}

	public Algorithm(int column, int row, int gridSize) {
		this.column = column;
		this.row = row;
		this.gridSize = gridSize;
	}

	public byte[] gridValuesByBytes(int[] xList, int[] yList, double[] rList, Interpolation type) {
		Map<Integer, Byte> actives = new HashMap<>();
		for (int i = 0; i < xList.length ; i++) {
			int index = xList[i] + yList[i] * column;

			if (type.equals(Interpolation.Log)) {
				int value = (int)log((int)(rList[i] * resolution), resolution) / (resolution / 16);
				byte val = (byte) Math.min(15, value);
				actives.put(index, val);
			} else if (type.equals(Interpolation.Linear)) {
				int value = (int)(rList[i] * resolution) / (resolution / 16);
				byte val = (byte) Math.min(15, value);
				actives.put(index, val);
			}
		}

		return to4bitBytes(actives);
	}

	// linear
	public byte[] pointQuantization(double ptx, double pty, Interpolation type) {
		byte[] result = new byte[gridSize / 2];
		Map<Integer, Byte> actives = new HashMap<>();

		int tmpX = scale(ptx, column, resolution);
		int tmpY = scale(pty, row, resolution);

		int gridX = tmpX / resolution;
		int gridY = tmpY  / resolution;

		int pointX = tmpX - resolution * gridX;
		int pointY = tmpY - resolution * gridY;


		int index1 = gridX + column * gridY;
		int index2 = index1 + 1;
		int index3 = index1 + column;
		int index4 = index3 + 1;

		int[] indexs = {index1, index2, index3, index4};
		int[] values = new int[4];

		if (pointX >= pointY) {
			values[0] = resolution - pointX;
			values[1] = pointX - pointY;
			values[3] = pointY;
		} else {
			values[0] = resolution - pointY;
			values[2] = pointY - pointX;
			values[3] = pointX;
		}

		if (type.equals(Interpolation.Log)) {
			for (int i = 0 ; i < values.length ; i++) {
				values[i] = (int) log(values[i], resolution) / 10;
			}

		} else if (type.equals(Interpolation.Linear)) {
			for (int i = 0 ; i < values.length ; i++) {
				values[i] = values[i] / 10;
			}
		}

		for(int i = 0 ; i < 4 ; i++) {
			if (values[i] > 0) {
				if (values[i] > 0xF) {
					values[i] = 0xF;
				}

				actives.put(indexs[i], (byte)values[i]);
			}
		}

		for(int index : actives.keySet()) {
			int resultIndex = index / 2;
			int innerIndex = index % 2;
			byte value = actives.get(index);


			byte before = result[resultIndex];

			if (innerIndex == 0) {
				result[resultIndex] = (byte)(before | (value << 4));
			} else {
				result[resultIndex] = (byte)(before | value);
			}
		}

		return result;
	}


	public byte[] objectTypeValuesByBytes(double[] x, double[] y, Interpolation type) {
		byte[] result = new byte[gridSize / 2];

		for(int i = 0 ; i < x.length ; i++) {
			byte[] a = pointQuantization(x[i], y[i], type);

			result = add(result, a);
		}

		return result;
	}

	private byte[] to4bitBytes(Map<Integer, Byte> actives) {
		byte[] result = new byte[gridSize / 2];
		for(int index : actives.keySet()) {
			int resultIndex = index / 2;
			int innerIndex = index % 2;
			byte value = actives.get(index);


			byte before = result[resultIndex];

			if (innerIndex == 0) {
				result[resultIndex] = (byte)(before | (value << 4));
			} else {
				result[resultIndex] = (byte)(before | value);
			}
		}

		return result;
	}



	private byte[] add(byte[] a, byte[] b) {
		byte[] result = new byte[gridSize / 2];

		for (int idx = 0 ; idx < a.length ; idx++) {
			int left = ((0xf0 & a[idx]) >> 4) + ((0xf0 & b[idx]) >> 4);
			int right = (0xf & a[idx]) + (0xf & b[idx]);

			if (left > 15) {
				left = 15;
			}
			if (right > 15) {
				right = 15;
			}

			byte v = (byte)((left << 4) + right);

			result[idx] = v;
		}

		return result;

	}

	private double log(int val, int resolution) {
		double ratio = (double)val / resolution;
		double changed = ratio * CONSTANT - LOG1;
		if (changed > 1) {
			changed = 1;
		}
		return changed * resolution;
	}

	private int scale(double value, int gridSize, int resolution) {
		double rValue = 1.2 * value - 0.1;
//		double rValue = value;
		if (rValue < 0) {
			rValue = 0;
		} else if (rValue > 1) {
			rValue = 1;
		}

		int quantized = (int) Math.round(rValue * (gridSize - 1) * (resolution));

		return quantized;
	}

}
