package com.example.rec;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.util.*;

public class BluetoothService {
    // Debugging
    private static final String TAG = "BluetoothService";
     
    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
     
    private BluetoothAdapter btAdapter;
     
    private Activity Rec;
    private Handler mHandler;
     
    // Constructors
    public BluetoothService(Activity ac, Handler h) {
        Rec = ac;
        mHandler = h;
         
        // BluetoothAdapter 얻기
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println(btAdapter.getName());
    }
     
    /**
     * Check the Bluetooth support
     * @return boolean
     */
    public boolean getDeviceState() {
        Log.i(TAG, "Check the Bluetooth support");
         
        if(btAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");
             
            return false;
             
        } else {
            Log.d(TAG, "Bluetooth is available");
             
            return true;
        }
    }
     
    /**
     * Check the enabled Bluetooth
     */
    public void enableBluetooth() {
        Log.i(TAG, "Check the enabled Bluetooth");
         
        if(btAdapter.isEnabled()) {     
            // 기기의 블루투스 상태가 On인 경우
            Log.d(TAG, "Bluetooth Enable Now");
             
            // Next Step
        } else {        
            // 기기의 블루투스 상태가 Off인 경우
            Log.d(TAG, "Bluetooth Enable Request");
             
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Rec.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }
     
}