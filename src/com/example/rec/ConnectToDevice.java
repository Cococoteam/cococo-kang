package com.example.rec;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class ConnectToDevice implements Serializable{
	private static final long serialVersionUID = -6959557772486023514L;
	int mPairedDeviceCount = 0;
	private Set<BluetoothDevice> mDevices;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mRemoteDevice;
	private BluetoothSocket mSocket = null;
	private OutputStream mOutputStream = null;
	private InputStream mInputStream = null;
	boolean pairingSuccess;

	public ConnectToDevice() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public void setmDevices() {
		mDevices = mBluetoothAdapter.getBondedDevices();
	}

	public Set<BluetoothDevice> getmDevices() {
		return mDevices;
	}

	public void setPairedDeviceCount() {
		mPairedDeviceCount = mDevices.size();
	}

	public int getmPairedDeviceCount() {
		return mPairedDeviceCount;
	}
	
	public OutputStream getOutputStrem(){
		return mOutputStream;
	}
	
	public InputStream getInputStream(){
		return mInputStream;
	}
	
	public boolean getPairingSuccess(){
		return pairingSuccess;
	}

	BluetoothDevice getDeviceFromBondedList(String name) { // 해당 블루투스 장치 객체를 페어링  된 장치 목록에서 찾아내기
		BluetoothDevice selectedDevice = null;
		for (BluetoothDevice device : mDevices) {
			if (name.equals(device.getName())) {
				selectedDevice = device;
				break;
			}
		}
		return selectedDevice;
	}// end getDeviceFromBondedList

	boolean connectToSelectedDevice(String selectedDeviceName) { // 소켓
		mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
		try {// 소켓 생성
			mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
			// RFCOMM 채널을 통한 연결
			mSocket.connect();
			// 데이터 송신을 위한 스트림 생성
			mOutputStream = mSocket.getOutputStream();
			mInputStream = mSocket.getInputStream();
			pairingSuccess = true;
			return pairingSuccess;
		} catch (Exception e) { // 블루투스 연결 중 오류 발생
			pairingSuccess = false;
			return pairingSuccess;
		}
	}// end connectToSelectedDevice

	public void close() {
		try {
			mOutputStream.close();
			mSocket.close();
		} 
		catch (Exception e) { }
	}//end close
}