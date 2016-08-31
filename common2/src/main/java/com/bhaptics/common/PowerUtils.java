package com.bhaptics.common;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by westside on 2016-04-25.
 */
public class PowerUtils {
    private static PowerManager.WakeLock wakeLock = null;

    public static void lock(Context context) {
    //등록
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) context.getSystemService(context.POWER_SERVICE);
//            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakelock");
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE, "WakeLock");
            wakeLock.acquire();
        }
    }

    public static void unlock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
