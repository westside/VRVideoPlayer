package tactosy;

import android.content.Context;
import android.util.Log;

import com.eje_c.vrvideoplayer.model.PositionType;

/**
 * Created by westside on 2016-07-29.
 */
public class UnityTactosyManager {
    private static TactosyManager tactosyManager;
    public static final String TAG = UnityTactosyManager.class.getSimpleName();
    private static UnityTactosyManager unityTactosyManager;

    public static UnityTactosyManager instance(Context context) {
        if (unityTactosyManager == null) {
            unityTactosyManager = new UnityTactosyManager();
            unityTactosyManager.init(context);
        }

        return unityTactosyManager;
    }

    public void init(Context context) {
        tactosyManager = TactosyManager.getInstance(context);
        tactosyManager.bindService();
        Log.i(TAG, "init: ");
    }

    public boolean setMotor(PositionType positionType,  byte[] bytes) {
        Log.i(TAG, "setMotor()");
        tactosyManager.setMotor(positionType, bytes);

        return true;
    }

    public boolean setMotorPathMode(PositionType positionType, byte[] bytes) {
        Log.i(TAG, "setMotorPathMode()");
        tactosyManager.setMotorPathMode(positionType, bytes);
        return true;
    }

    public void destroy() {
        Log.i(TAG, "destroy()");
        tactosyManager.unbindService();
    }

}
