package com.bhaptics.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.bhaptics.common.PowerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by westside on 2016-04-25.
 */
public class BluetoothLeService extends Service implements BluetoothAdapter.LeScanCallback {

    public static final String TAG = BluetoothLeService.class.getSimpleName();

    private Messenger client;

    private Looper looper;
    private BluetoothHandler bluetoothHandler;

    private Messenger messenger;

    private BluetoothAdapter mBtAdapter = null;
    private BluetoothManager mBluetoothManager = null;

    private Map<String, BluetoothGatt> bluetoothGattMap;
    private Map<String, LinkedBlockingQueue<Object>> currentRequestMap;

    private final class BluetoothHandler extends Handler {
        public BluetoothHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String addr;
            LinkedBlockingQueue<Object> queue;

            Bundle data;
            UUID serviceId;
            UUID charId;
            byte[] values;

            BluetoothGattCharacteristic characteristic;
            BluetoothLeRequest req;

            switch (msg.what) {
                case TactosyConstants.MESSAGE_REPLY:
                    client = msg.replyTo;

                    Message response = new Message();
                    List<BluetoothDevice> devices =
                            mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);

                    data = new Bundle();
                    data.putParcelableArrayList(TactosyConstants.KEY_CONNECTED, (ArrayList<BluetoothDevice>) devices);

                    response.setData(data);
                    response.what = TactosyConstants.MESSAGE_REPLY;

                    try {
                        client.send(response);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                case TactosyConstants.MESSAGE_SCAN:
                    scan();
                    break;
                case TactosyConstants.MESSAGE_STOPSCAN:
                    stopScan();
                    break;
                case TactosyConstants.MESSAGE_CONNECT:
                    addr = (String) msg.obj;
                    connect(addr);
                    break;
                case TactosyConstants.MESSAGE_DISCONNECT:
                    addr = (String) msg.obj;
                    disconnect(addr);
                    break;
                case TactosyConstants.MESSAGE_READ:
                    addr = (String) msg.obj;

                    queue = currentRequestMap.get(addr);
                    if (queue == null) {
                        queue = new LinkedBlockingQueue<>(1);
                        currentRequestMap.put(addr, queue);
                    } else {
                        try {
                            queue.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }

                    data = msg.getData();
                    serviceId = UUID.fromString(data.getString(TactosyConstants.KEY_SERVICE_ID));
                    charId = UUID.fromString(data.getString(TactosyConstants.KEY_CHAR_ID));


                    characteristic = characteristic(addr, serviceId, charId);
                    if (characteristic == null) {
                        Message err = new Message();
                        err.what = TactosyConstants.MESSAGE_READ_ERROR;
                        break;
                    }
                    req = new BluetoothLeRequest();

                    req.address = addr;
                    req.characteristic = characteristic;
                    Log.e(TAG, characteristic + "");
                    req.status = BluetoothLeRequest.bleRequestStatus.NOT_QUEUED;
                    req.operation = BluetoothLeRequest.bleRequestOperation.RD;

                    executeRequest(req);
                    break;
                case TactosyConstants.MESSAGE_WRITE:
                    data = msg.getData();
                    addr = data.getString(TactosyConstants.KEY_ADDR);
                    queue = currentRequestMap.get(addr);
                    if (queue == null) {
                        queue = new LinkedBlockingQueue<>(1);
                        currentRequestMap.put(addr, queue);
                    } else {
                        try {
                            queue.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    serviceId = UUID.fromString(data.getString(TactosyConstants.KEY_SERVICE_ID));
                    charId = UUID.fromString(data.getString(TactosyConstants.KEY_CHAR_ID));
                    values = data.getByteArray(TactosyConstants.KEY_VALUES);


                    req = new BluetoothLeRequest();
                    characteristic = characteristic(addr, serviceId, charId);

                    characteristic.setValue(values);
                    req.address = addr;
                    req.characteristic = characteristic;
                    req.status = BluetoothLeRequest.bleRequestStatus.NOT_QUEUED;
                    req.operation = BluetoothLeRequest.bleRequestOperation.WR;


                    Log.e(TAG, addr + " " + serviceId + " " + charId);
                    executeRequest(req);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onCreate: lock");
        Log.i(TAG, "onStartCommand: " + intent + ", " + flags + "," + startId);

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Cannot initialize BluetoothManager.");
                stopSelf();
            }
        }


        mBtAdapter = mBluetoothManager.getAdapter();
        if (mBtAdapter == null) {
            Log.e(TAG, "Cannot obtain a BluetoothAdapter.");
            stopSelf();
        }

        client = null;

        bluetoothGattMap = new HashMap<>();
        currentRequestMap = new HashMap<>();

        HandlerThread thread = new HandlerThread("BluetoothHandlerThread",
                                                 Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();

        looper = thread.getLooper();
        bluetoothHandler = new BluetoothHandler(looper);
        messenger = new Messenger(bluetoothHandler);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        for (BluetoothGatt mBluetoothGatt : bluetoothGattMap.values()) {
            mBluetoothGatt.close();
        }
        bluetoothGattMap.clear();

        bluetoothHandler.removeCallbacksAndMessages(null);
        looper.quit();

        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    private void scan() {
        mBtAdapter.startLeScan(this);
    }
    private void stopScan() {
        mBtAdapter.stopLeScan(this);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Message msg = new Message();
        msg.what = TactosyConstants.MESSAGE_SCAN_RESPONSE;
        msg.obj = device;

        try {
            client.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void connect(String address) {
        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect. " + address);
            removeBluetoothGatt(address);
            return;
        }

        int connectionState = mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
        if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
            if (hasConnectionBefore(address) && getBluetoothGatt(address) != null) {
                // Suddenly disconnected device. Try to reconnect.
                Log.i(TAG, "Re-connect GATT - " + address);
                getBluetoothGatt(address).connect();
            } else {
                // We want to directly connect to the device, so we are setting the
                // autoConnect parameter to false.
                Log.d(TAG, "Create a new GATT connection. " + address);
                TactosyBluetoothGattCallback callback = new TactosyBluetoothGattCallback();
                BluetoothGatt bluetoothGatt = device.connectGatt(this, false, callback);
                if (bluetoothGatt != null) {
                    addBluetoothGatt(address, bluetoothGatt);
                }
            }
        }
    }

    private void disconnect(String address) {
        if (mBtAdapter == null) {
            Log.w(TAG, "disconnect: BluetoothAdapter not initialized");
            return;
        }
        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        int connectionState = mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);

        if (getBluetoothGatt(address) != null) {
            if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
                getBluetoothGatt(address).disconnect();
            } else {
                Log.w(TAG, "Attempt to disconnect in state: " + connectionState);
            }
        }
    }

    private BluetoothGatt addBluetoothGatt(String address, BluetoothGatt bluetoothGatt) {
        return bluetoothGattMap.put(address, bluetoothGatt);
    }

    private BluetoothGatt removeBluetoothGatt(String address) {
        return bluetoothGattMap.remove(address);
    }

    private BluetoothGatt getBluetoothGatt(String address) {
        return bluetoothGattMap.get(address);
    }

    private boolean hasConnectionBefore(String address) {
        return bluetoothGattMap.keySet().contains(address);
    }

    private void onConnectionResponse(BluetoothGatt gatt, int state) {
        Message msg = new Message();
        msg.what = TactosyConstants.MESSAGE_CONNECT_RESPONSE;
        msg.arg1 = state;
        msg.obj = gatt.getDevice().getAddress();

        try {
            client.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class TactosyBluetoothGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (gatt == null) {
                Log.e(TAG, "mBluetoothGatt not created!");
                return;
            }

            BluetoothDevice device = gatt.getDevice();
            String address = device.getAddress();
            Log.i(TAG, "onConnectionStateChange (" + address + ") " + newState + " status: " + status);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "onConnectionStateChange: " + address + " connected.");
                    discoverServices(gatt.getDevice().getAddress());
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "onConnectionStateChange: " + address + " disconnected.");

                    onConnectionResponse(gatt, newState);
                    break;
                default:
                    Log.d(TAG, "New state not processed: " + newState);
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // NOTE It is assumed when characteristic only contained by tactosy is read.
            // TODO Filter only tactosys using uuid of advertising.
            Log.i(TAG, "onServicesDiscovered() " + status);
            onConnectionResponse(gatt, BluetoothProfile.STATE_CONNECTED);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LinkedBlockingQueue<Object> queue = currentRequestMap.get(gatt.getDevice().getAddress());
            assert queue != null;

            Message msg = new Message();
            msg.what = TactosyConstants.MESSAGE_READ_SUCCESS;
            try {
                queue.put(new Object());
            } catch (InterruptedException e) {
                e.printStackTrace();
                msg.what = TactosyConstants.MESSAGE_READ_ERROR;
            }

            Bundle data = new Bundle();
            data.putString(TactosyConstants.KEY_ADDR, gatt.getDevice().getAddress());
            data.putString(TactosyConstants.KEY_CHAR_ID, characteristic.getUuid().toString());
            data.putByteArray(TactosyConstants.KEY_VALUES, characteristic.getValue());

            msg.arg1 = status;
            msg.setData(data);

            try {
                client.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LinkedBlockingQueue<Object> queue = currentRequestMap.get(gatt.getDevice().getAddress());
            assert queue != null;

            Message msg = new Message();
            msg.what = TactosyConstants.MESSAGE_WRITE_SUCCESS;
            try {
                queue.put(new Object());
            } catch (InterruptedException e) {
                e.printStackTrace();
                msg.what = TactosyConstants.MESSAGE_WRITE_ERROR;
            }

            Bundle data = new Bundle();
            data.putString(TactosyConstants.KEY_ADDR, gatt.getDevice().getAddress());
            data.putString(TactosyConstants.KEY_CHAR_ID, characteristic.getUuid().toString());
            msg.arg1 = status;
            msg.setData(data);

            try {
                client.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean discoverServices(String address) {
        Log.i(TAG, "discoverServices(): " + address);

        BluetoothGatt mBluetoothGatt = bluetoothGattMap.get(address);

        if (mBluetoothGatt != null) {
            mBluetoothGatt.discoverServices();
            return true;
        }

        return false;
    }

    public boolean checkGatt(String address) {

        if (mBtAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized. " + address);
            return false;
        }

        if (getBluetoothGatt(address) == null) {
            Log.w(TAG, "BluetoothGatt not initialized. " + address);
            return false;
        }

        return true;
    }

    public void executeRequest(BluetoothLeRequest request) {
        if (request == null) {
            return;
        }

        BluetoothGatt bluetoothGatt = getBluetoothGatt(request.address);
        if (bluetoothGatt == null) {
            Log.e(TAG, "executeRequest: gatt is null");
            return;
        }

        Log.i(TAG, "executeRequest: " + request);

        switch (request.operation) {
            case WR:
                request.status = BluetoothLeRequest.bleRequestStatus.PROCESSING;
                if (!checkGatt(request.address)) {
                    request.status = BluetoothLeRequest.bleRequestStatus.FAILED;
                    break;
                }
                bluetoothGatt.writeCharacteristic(request.characteristic);
                break;
            case RD:
                request.status = BluetoothLeRequest.bleRequestStatus.PROCESSING;

                if (!checkGatt(request.address)) {
                    request.status = BluetoothLeRequest.bleRequestStatus.FAILED;
                    break;
                }
                bluetoothGatt.readCharacteristic(request.characteristic);

                break;
            default:
                break;
        }
    }

    BluetoothGattCharacteristic characteristic(String address, UUID serviceId, UUID charId) {
        BluetoothGatt bluetoothGatt = getBluetoothGatt(address);

        if (bluetoothGatt == null) {
            return null;
        }

        BluetoothGattService service = bluetoothGatt.getService(serviceId);
        if (service == null) {
            return null;
        }

        return service.getCharacteristic(charId);
    }
}
