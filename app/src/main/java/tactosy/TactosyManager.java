package tactosy;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TactosyManager {

    // Static variables. tag string, static instance.
    public static final String TAG = TactosyManager.class.getSimpleName();

    private static TactosyManager instance = null;

    private List<String> connectedDevices = new ArrayList<>();

    public static TactosyManager getInstance(Context _context) {
        if (instance == null) {
            instance = new TactosyManager(_context);
        }
        return instance;
    }

    private TactosyManager(Context _context) {
        context = _context;
    }

    // inner class definition.
    // handler for getting reponse from BluetoothLeService and
    // providing callback for (com.bhaptics.unity.TactosyManager's) users
    private class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TactosyConstants.MESSAGE_REPLY:
                    Bundle data = msg.getData();

                    if (data == null) {
                        break;
                    }
                    connectedDevices.clear();
                    for (Parcelable p: data.getParcelableArrayList(TactosyConstants.KEY_CONNECTED)) {
                        BluetoothDevice device = (BluetoothDevice) p;
                        connectedDevices.add(device.getAddress());
                    }
                    Log.i(TAG, "ConnectedDevices: " + connectedDevices);

                    break;
                case TactosyConstants.MESSAGE_CONNECT:
                case TactosyConstants.MESSAGE_READ_SUCCESS:
                    int status = msg.arg1;
                    data = msg.getData();
                    String address = data.getString(TactosyConstants.KEY_ADDR);
                    String uuid = data.getString(TactosyConstants.KEY_CHAR_ID);
                    byte[] values = data.getByteArray(TactosyConstants.KEY_VALUES);

                    Log.i(TAG, "handleMessage: " + address + "," + uuid + "," + values[0]);
                    break;
                case TactosyConstants.MESSAGE_WRITE_SUCCESS:
                    status = msg.arg1;
                    data = msg.getData();
                    address = data.getString(TactosyConstants.KEY_ADDR);
                    uuid = data.getString(TactosyConstants.KEY_CHAR_ID);
                    Log.i(TAG, "handleMessage: " + address + "," + uuid);

                    break;
                case TactosyConstants.MESSAGE_READ_ERROR:
                    int errCode = TactosyConstants.MESSAGE_READ_ERROR;
                case TactosyConstants.MESSAGE_WRITE_ERROR:
                    errCode = TactosyConstants.MESSAGE_WRITE_ERROR;
                    break;
                default:
                    break;
            }
        }
    }

    // Member variables.
    private Context context;
    private Messenger messenger = null;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder _service) {
            Log.i(TAG, "onServiceConnected: " + name + ", " + _service);
            messenger = new Messenger(_service);

            Message msg = new Message();
            msg.what = TactosyConstants.MESSAGE_REPLY;
            msg.replyTo = new Messenger(new InnerHandler());

            sendMessageSafe(msg);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "Service disconnected suddenly");
            messenger = null;
        }
    };

    public void bindService() {
        Intent intent = new Intent("com.bhaptics.commons.service");
        intent.setPackage("com.bhaptics.demo_ble");
        context.bindService(intent,
                            connection,
                            Context.BIND_IMPORTANT);
        Log.i(TAG, "bindService: ");
    }

    public void unbindService() {
        context.unbindService(connection);
    }

    private void sendMessageSafe(Message msg) {
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, "sendMessageSafe: ", e);
        } catch (Exception e) {
            Toast.makeText(context, "Start tactosy app.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "sendMessageSafe: ", e);
        }
    }

    public void setMotor(byte[] values) {
        setMotor(values, false);
    }

    public void setMotor(byte[] values, boolean force) {
        setMotor(values, force, TactosyConstants.MOTOR_CHAR);
    }

    public void setMotorPathMode(byte[] bytes) {
        setMotor(bytes, false, TactosyConstants.MOTOR_CHAR_MAPP);
    }

    private void setMotor(byte[] values, boolean force, UUID charUUID) {

        for (String addr : connectedDevices) {
            Message msg = new Message();
            Bundle data = new Bundle();

            Log.i(TAG, "setMotor: ");
            data.putString(TactosyConstants.KEY_SERVICE_ID, TactosyConstants.MOTOR_SERVICE.toString());
            data.putString(TactosyConstants.KEY_CHAR_ID, charUUID.toString());
            data.putByteArray(TactosyConstants.KEY_VALUES, values);
            data.putString(TactosyConstants.KEY_ADDR, addr);

            msg.arg1 = force ? 1 : 0;
            msg.what = TactosyConstants.MESSAGE_WRITE;

            msg.setData(data);
            sendMessageSafe(msg);
        }
    }

    public void readBattery(String addr) {
        Message msg = new Message();
        Bundle data = new Bundle();

        data.putString(TactosyConstants.KEY_SERVICE_ID, TactosyConstants.BATTERY_SERVICE.toString());
        data.putString(TactosyConstants.KEY_CHAR_ID, TactosyConstants.BATTERY_CHAR.toString());

        msg.obj = addr;
        msg.what = TactosyConstants.MESSAGE_READ;

        msg.setData(data);
        sendMessageSafe(msg);
    }

    public void readVersion(String addr) {
        Message msg = new Message();
        Bundle data = new Bundle();

        data.putString(TactosyConstants.KEY_SERVICE_ID, TactosyConstants.DFU_SERVICE.toString());
        data.putString(TactosyConstants.KEY_CHAR_ID, TactosyConstants.DFU_VERSION.toString());

        msg.obj = addr;
        msg.what = TactosyConstants.MESSAGE_READ;

        msg.setData(data);
        sendMessageSafe(msg);
    }
}
