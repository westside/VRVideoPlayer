package com.bhaptics.common;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by westside on 2016-03-17.
 */
public class StringUtils {
    public static final String TAG = StringUtils.class.getSimpleName();

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }

        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static List<Integer> split(String str) {
        List<String> items = Arrays.asList(str.split("\\s*,\\s*"));

        List<Integer> result = new ArrayList<>();

        for (String item : items) {
            try {
                result.add(Integer.parseInt(item));
            } catch (Exception e) {
                Log.e(TAG, "split: " + item, e);
            }
        }

        return result;
    }

    public static String convertToTime(int totalSecs) {
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return timeString;
    }
}
