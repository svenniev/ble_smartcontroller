package com.arrow.a79361.arrowsmartcontroller;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;

import static android.support.v4.app.ActivityCompat.requestPermissions;

/**
 * Created by A79361 on 2016.08.02..
 */
public class BleService extends Service {

    private final IBinder mBinder = new LocalBinder();

    public static final int BLUETOOTH_INITIALIZATION_FAILED = 1;
    public static final int ADAPTER_INITIALIZATION_FAILED  = 2;
    public static final int BLE_NOT_SUPPORTED  = 3;

    private static Activity mMainActivity;

    private BluetoothManager mBleManager;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBleAdapter;

    BleService(Activity mMainActivityReference) {
        mMainActivity = mMainActivityReference;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BleService getService() {
            return BleService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public int initialize() {

        // Android M Permission check 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(mMainActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                });
                builder.show();
            }
        }

        if (mBleManager == null) {
            mBleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBleManager == null) {
                return BLUETOOTH_INITIALIZATION_FAILED;
            }
        }

        mBleAdapter = mBleManager.getAdapter();
        if (mBleAdapter == null) {
            return ADAPTER_INITIALIZATION_FAILED;
        } else {
            // Check if Bluetooth is enabled
            if (!mBleAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBT);
            }

            // Check if the Device Supports BLE
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                return BLE_NOT_SUPPORTED;
            }
        }

        return 0;
    }

}
