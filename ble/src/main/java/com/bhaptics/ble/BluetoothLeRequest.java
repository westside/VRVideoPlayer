package com.bhaptics.ble;

import android.bluetooth.BluetoothGattCharacteristic;

import com.bhaptics.common.StringUtils;

/**
 * Created by westside on 2016-04-25.
 */
public class BluetoothLeRequest {
    public int id;
    public String address;
    public BluetoothGattCharacteristic characteristic;
    public bleRequestOperation operation = bleRequestOperation.UNDEFINED;
    public volatile bleRequestStatus status;
    public int timeout;
    public int curTimeout;
    public boolean notifyenable;

    public enum bleRequestOperation {
        WRBLOCKING,
        WR,
        RDBLOCKING,
        RD,
        NSBLOCKING,
        UNDEFINED
    }

    public enum bleRequestStatus {
        NOT_QUEUED,
        QUEUED,
        PROCESSING,
        TIMEOUT,
        DONE,
        NO_SUCH_REQUEST,
        FAILED,
    }

    @Override
    public String toString() {
        return "BluetoothLeRequest{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", characteristic=" + StringUtils.bytesToHexString(characteristic.getValue()) +
                ", operation=" + operation +
                ", status=" + status +
                ", timeout=" + timeout +
                ", curTimeout=" + curTimeout +
                ", notifyenable=" + notifyenable +
                '}';
    }
}
