package com.bhaptics.ble;

/**
 * Created by westside on 2016-03-17.
 */
public class TactosyDevice {
    private boolean connected;
    private String macAddress;
    private String deviceName;
    private int battery;
    private int versionMajor;
    private int versionMinor;
    public boolean showDetail;

    public TactosyDevice(String macAddress, String deviceName) {
        this.macAddress = macAddress;
        this.deviceName = deviceName;
        this.connected = false;
        this.battery = -1;
        this.versionMajor = -1;
        this.versionMinor = -1;
        this.showDetail = false;
    }

    public void setConnected(boolean conn){this.connected = conn;}

    public boolean getConnected() { return connected; }

    public String getMacAddress() {
        return macAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getBattery() {
        return battery;
    }

    public boolean isOutDated(int latestMajor, int latestMinor) {
        return versionMajor < latestMajor || ((versionMajor == latestMajor) && versionMinor < latestMinor);
    }

    public void setVersion(int major, int minor) {
        versionMajor = major;
        versionMinor = minor;
    }

    public String getVersion() {
        return String.valueOf(versionMajor) + "." + String.valueOf(versionMinor);
    }

    @Override
    public String toString() {
        return "TactosyDevice{" +
                "connected=" + connected +
                ", macAddress='" + macAddress + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", battery=" + battery +
                '}';
    }

}
