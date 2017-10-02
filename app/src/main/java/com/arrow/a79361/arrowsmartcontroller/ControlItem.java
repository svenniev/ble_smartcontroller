package com.arrow.a79361.arrowsmartcontroller;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

/**
 * Created by A79361 on 2016.07.28..
 */
public class ControlItem {
    /**
     * types:
     * 1 - BLE Lamp node
     * 2 - Mesh
     */
    private int type;
    private int rss;
    private String name;
    private String address;
    private BluetoothDevice device;
    private BluetoothClass btclass;
    private ParcelUuid[] uuids;
    private int state;
    private String sPassword;

    /**
     *  STATUS indication
     */
    public final String STATE_CONNECTING = "Connecting..";         // 1
    public final String STATE_ONLINE = "Online";                   // 0
    public final String STATE_BOUND = "Device Paired";             // 2
    public final String STATE_BUSY = "Device Occupied";            // 3
    public final String STATE_OFFLINE = "Device Unavailable";      // 4

    ControlItem() {

    }

    ControlItem(String sName) {
        this.name = sName;
    }

    ControlItem(BluetoothDevice passDevice) {
        this.device = passDevice;
        this.state = 0;

        rss = 0;

        if (this.device.getName() != null) {
            name = this.device.getName();
        } else {
            name = "Misc";
        }


        if (this.device.getAddress() != null) {
            address = this.device.getAddress();
        } else {
            address = "N/A";
        }

        /**
        if (this.device.getType() != 0) {
            type = this.device.getType();
        } else {
            type = 0;
        }
         */

        if (this.device.getBluetoothClass() != null) {
            btclass = this.device.getBluetoothClass();
        } else {
            btclass = null;
        }

        if (this.device.getUuids() != null) {
            uuids = this.device.getUuids();
        } else {
            uuids = null;
        }
    }

    public void setRssi(int rssi) { rss = rssi; }

    public int getRssi() { return rss; }

    public void setName(String sSetName) {
        this.name = sSetName;
    }

    public String getName() {
        return this.name;
    }

    public void setAddress(String sSetAddress) {
        this.address = sSetAddress;
    }

    public String getAddress() {
        return this.address;
    }

    public void setType(int iSetType) {
        this.type = iSetType;
    }

    public int getType() {
        return this.type;
    }

    public ParcelUuid[] getUuids() {
        return this.uuids;
    }

    public BluetoothDevice getDevice() {
        return this.device;
    }

    public String getState() {
        String sState = "";

        switch (state) {
            case 0:
                sState = STATE_ONLINE;
                break;
            case 1:
                sState = STATE_CONNECTING;
                break;
            case 2:
                sState = STATE_BOUND;
                break;
            case 3:
                sState = STATE_BUSY;
                break;
            case 4:
                sState = STATE_OFFLINE;
                break;
        }

        return sState;
    }

    public int getStateInt() {
        return state;
    }

    public void setState(int sstate) {
        state = sstate;
    }

}
