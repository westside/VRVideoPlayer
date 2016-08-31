package com.bhaptics.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
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

import com.bhaptics.common.illusion.MotorArrayConfig;
import com.bhaptics.common.illusion.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TactosyManager {

    // Static variables. tag string, static instance.
    public static final String TAG = TactosyManager.class.getSimpleName();

    private MotorArrayConfig motorArrayConfig = new MotorArrayConfig(5, 4, 10);
    private static TactosyManager instance = null;

    // Constructors and Factory methods.
    public static TactosyManager getInstance(Context context) {
        return TactosyManager.getInstance(context, null);
    }

    public static TactosyManager getInstance(Context _context, ScanCallback scanCallback) {
        return TactosyManager.getInstance(_context, scanCallback, null, null);
    }

    public static TactosyManager getInstance(Context _context,
                                             ScanCallback scanCallback,
                                             ConnectCallback connectCallback,
                                             DataCallback dataCallback) {
        if (instance == null) {
            instance = new TactosyManager(_context, scanCallback, connectCallback, dataCallback);
        }
        return instance;
    }

    private TactosyManager(Context _context,
                           ScanCallback _scanCallback,
                           ConnectCallback _connectCallback,
                           DataCallback _dataCallback) {
        context = _context;
        scanCallback = _scanCallback;
        connectCallback = _connectCallback;
        dataCallback = _dataCallback;
        bluetoothDeviceItemMap = new HashMap<>();
    }

    public interface ScanCallback {
        void onTactosyScan(TactosyDevice device);
    }

    public interface ConnectCallback {
        void onConnect(String addr);
        void onDisconnect(String addr);
        void onConnectionError(String addr);
    }

    public interface DataCallback {
        void onRead(String address, UUID charUUID, byte[] bytes, int status);
        void onWrite(String address, UUID charUUID, int status);
        void onDataError(int errCode);
    }

    // inner class definition.
    // handler for getting reponse from BluetoothLeService and
    // providing callback for (TactosyManager's) users
    private class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TactosyConstants.MESSAGE_REPLY:
                    Bundle data = msg.getData();

                    if (data == null) {
                        break;
                    }
                    for (Parcelable p: data.getParcelableArrayList(TactosyConstants.KEY_CONNECTED)) {
                        BluetoothDevice device = (BluetoothDevice) p;
                        onTactosyScan(device);

                        if (connectCallback != null) {
                            connectCallback.onConnect(device.getAddress());
                        }
                    }

                    break;
                case TactosyConstants.MESSAGE_SCAN_RESPONSE:
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    onTactosyScan(device);
                    break;
                case TactosyConstants.MESSAGE_CONNECT_RESPONSE:
                    int status = msg.arg1;
                    String address = (String) msg.obj;

                    if (connectCallback != null) {
                        if (status == BluetoothProfile.STATE_CONNECTED) {
                            connectCallback.onConnect(address);
                        } else if (status == BluetoothProfile.STATE_DISCONNECTED) {
                            connectCallback.onDisconnect(address);
                        } else {
                            connectCallback.onConnectionError(address);
                        }
                    }

                    break;
                case TactosyConstants.MESSAGE_READ_SUCCESS:
                    BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) msg.obj;
                    status = msg.arg1;
                    data = msg.getData();
                    address = data.getString(TactosyConstants.KEY_ADDR);
                    UUID uuid = UUID.fromString(data.getString(TactosyConstants.KEY_CHAR_ID));
                    byte[] bytes = data.getByteArray(TactosyConstants.KEY_VALUES);
                    if (dataCallback != null) {
                        dataCallback.onRead(address, uuid, bytes, status);
                    }
                    break;
                case TactosyConstants.MESSAGE_WRITE_SUCCESS:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    status = msg.arg1;
                    data = msg.getData();
                    address = data.getString(TactosyConstants.KEY_ADDR);
                    uuid = UUID.fromString(data.getString(TactosyConstants.KEY_CHAR_ID));
                    if (dataCallback != null) {
                        dataCallback.onWrite(address, uuid, status);
                    }
                    break;

                case TactosyConstants.MESSAGE_READ_ERROR:
                    int errCode = TactosyConstants.MESSAGE_READ_ERROR;
                    if (dataCallback != null) {
                        dataCallback.onDataError(errCode);
                    }
                case TactosyConstants.MESSAGE_WRITE_ERROR:
                    errCode = TactosyConstants.MESSAGE_WRITE_ERROR;
                    if (dataCallback != null) {
                        dataCallback.onDataError(errCode);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    // Member variables.
    private Context context;
    private Messenger service = null;
    private ScanCallback scanCallback = null;
    private ConnectCallback connectCallback = null;
    private DataCallback dataCallback = null;

    private Map<String, TactosyDevice> bluetoothDeviceItemMap;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder _service) {
            service = new Messenger(_service);

            Message msg = new Message();
            msg.what = TactosyConstants.MESSAGE_REPLY;
            msg.replyTo = new Messenger(new InnerHandler());

            sendMessageSafe(msg);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "Service disconnected suddenly");
            service = null;
        }
    };

    public void bindService() {
        context.bindService(new Intent(context, BluetoothLeService.class),
                            connection,
                            Context.BIND_IMPORTANT);
    }

    public void unbindService() {
        context.unbindService(connection);
    }

    public void scan() {
        Log.i(TAG, "scan start");

        Message msg = new Message();
        msg.what = TactosyConstants.MESSAGE_SCAN;
        sendMessageSafe(msg);
    }

    public void stopScan() {
        Message msg = new Message();
        msg.what = TactosyConstants.MESSAGE_STOPSCAN;
        sendMessageSafe(msg);
    }

    public void onTactosyScan(BluetoothDevice device) {
        if (scanCallback != null && !bluetoothDeviceItemMap.containsKey(device.getAddress())) {
            TactosyDevice tDevice = new TactosyDevice(device.getAddress(), device.getName());
            bluetoothDeviceItemMap.put(tDevice.getMacAddress(), tDevice);
            scanCallback.onTactosyScan(tDevice);
        }
    }

    private void sendMessageSafe(Message msg) {
        try {
            service.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, "sendMessageSafe: ", e);
        } catch (Exception e) {
            Log.e(TAG, "sendMessageSafe: ", e);
        }
    }

    public List<TactosyDevice> getConnectedDevices() {
        List<TactosyDevice> result = new ArrayList<>();
        for (TactosyDevice tactosyDevice : bluetoothDeviceItemMap.values()) {
            if (tactosyDevice.getConnected()) {
                result.add(tactosyDevice);
            }
        }
        return result;
    }

    public TactosyDevice getBleDeviceItem(String address) {
        return bluetoothDeviceItemMap.get(address);
    }

    public boolean connect(TactosyDevice tDevice) {
        if (tDevice != null) {
            Message msg = new Message();
            msg.what = TactosyConstants.MESSAGE_CONNECT;
            msg.obj = tDevice.getMacAddress();
            sendMessageSafe(msg);

            return true;
        }
        return false;
    }

    public void disconnect(TactosyDevice tDevice) {
        if (tDevice != null) {
            Message msg = new Message();
            msg.what = TactosyConstants.MESSAGE_DISCONNECT;
            msg.obj = tDevice.getMacAddress();
            sendMessageSafe(msg);
        }
    }

    public void setMotor(String addr, byte[] values) {
        setMotor(addr, values, false);
    }

    public void setMotor(String addr, byte[] values, boolean force) {
        setMotor(addr, values, force, TactosyConstants.MOTOR_CHAR);
    }

    public void setMotor(String addr, List<Point> points) {
        if (points.size() > 6) {
            Log.e(TAG, "setMotor: Illegal argument " + points);
            return;
        }
        byte[] bytes = new byte[TactosyConstants.DATA_SIZE];
        bytes[0] = (byte)points.size();
        for (int i = 0 ; i < points.size() ; i++) {
            bytes[3*i + 1] = (byte)(points.get(i).getX() * motorArrayConfig.getResolution() * (motorArrayConfig.getColumn() - 1));
            bytes[3*i + 2] = (byte)(points.get(i).getY() * motorArrayConfig.getResolution() * (motorArrayConfig.getRow() - 1));
            bytes[3*i + 3] = (byte)(points.get(i).getIntensity() * 100);
        }

        setMotor(addr, bytes, false, TactosyConstants.MOTOR_CHAR_MAPP);
    }

    private void setMotor(String addr, byte[] values, boolean force, UUID charUUID) {
        Message msg = new Message();
        Bundle data = new Bundle();

        Log.i(TAG, "setMotor: " + charUUID.toString());
        data.putString(TactosyConstants.KEY_SERVICE_ID, TactosyConstants.MOTOR_SERVICE.toString());
        data.putString(TactosyConstants.KEY_CHAR_ID, charUUID.toString());
        data.putByteArray(TactosyConstants.KEY_VALUES, values);
        data.putString(TactosyConstants.KEY_ADDR, addr);

        msg.arg1 = force ? 1 : 0;
        msg.what = TactosyConstants.MESSAGE_WRITE;

        msg.setData(data);
        sendMessageSafe(msg);
    }

    public void setMotorConfig(String addr, byte[] raw_data) {
        Message msg = new Message();
        Bundle data = new Bundle();

        data.putString(TactosyConstants.KEY_SERVICE_ID, TactosyConstants.MOTOR_SERVICE.toString());
        data.putString(TactosyConstants.KEY_CHAR_ID, TactosyConstants.MOTOR_CHAR_CONFIG.toString());
        data.putString(TactosyConstants.KEY_ADDR, addr);
        data.putByteArray(TactosyConstants.KEY_VALUES, raw_data);

        msg.what = TactosyConstants.MESSAGE_WRITE;

        msg.setData(data);
        sendMessageSafe(msg);
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
