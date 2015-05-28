package com.example.rec;

import java.io.*;
import java.util.*;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class Rec extends Activity {
	LinearLayout layout;
	int mPairedDeviceCount = 0;
	public static Set<BluetoothDevice> mDevices;
	public static BluetoothAdapter mBluetoothAdapter;
	public static OutputStream mOutputStream = null;
	public static InputStream mInputStream = null;
	public static ConnectToDevice connectToDevice;
	public static boolean pairingSuccess;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mainui);
		layout = (LinearLayout) findViewById(R.id.main);
		layout.setBackgroundResource(R.drawable.backimg);
		findViewById(R.id.recording).setOnClickListener(mClickListener);
		findViewById(R.id.select).setOnClickListener(mClickListener);
		findViewById(R.id.connection).setOnClickListener(mClickListener);
		findViewById(R.id.bluetooth).setOnClickListener(mClickListener);
		findViewById(R.id.search).setOnClickListener(mClickListener);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		connectToDevice = new ConnectToDevice();
	}
	
	
	Button.OnClickListener mClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.recording:
				Log.i("onClick", "recording");
				Intent recordingActivity = new Intent(Rec.this, RecPage.class);
				//recordingActivity.putExtra("connectToDevice", connectToDevice);
				startActivity(recordingActivity);
				break;
			case R.id.select:
				Log.i("onClick", "select");
				Intent selectActivity = new Intent(Rec.this, FileList.class);
				startActivity(selectActivity);
				break;
			case R.id.connection:
				Log.i("onClick", "connection");
				Intent connectionActivity = new Intent(Rec.this, MapPage.class);
				startActivity(connectionActivity);
				break;
			case R.id.bluetooth:
				Log.i("onClick", "bluetooth");
				if (!mBluetoothAdapter.isEnabled()) {
					Intent turnon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(turnon, 0);
				} 
				else {
					Toast.makeText(getApplicationContext(), "이미 켜져있습니다.", Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.search:
				connectToDevice.setmDevices();
				connectToDevice.setPairedDeviceCount();
				mPairedDeviceCount = connectToDevice.getmPairedDeviceCount();
				if (mPairedDeviceCount == 0) {// 페어링 된 장치가 없는 경우
					Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(Rec.this);
				builder.setTitle("블루투스 장치 선택");
				
				// 페어링 된 블루투스 장치의 이름 목록 작성
				List<String> listItems = new ArrayList<String>();
				mDevices = connectToDevice.getmDevices();
				for (BluetoothDevice device : mDevices) {
					listItems.add(device.getName());
				}

				final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);

				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						// 연결할 장치를 선택한 경우 선택한 장치와 연결을 시도함
						pairingSuccess = connectToDevice.connectToSelectedDevice(items[item].toString());
						if(pairingSuccess)
							Toast.makeText(getApplicationContext(), "아두이노와 연결이 성공되었습니다.", Toast.LENGTH_LONG).show();
						else
							Toast.makeText(getApplicationContext(), "아두이노와 연결이 실패하었습니다.", Toast.LENGTH_LONG).show();
						mOutputStream = connectToDevice.getOutputStrem();
						mInputStream = connectToDevice.getInputStream();
					}//end onClick
				});//end setItems
				builder.setCancelable(false); // 뒤로 가기 버튼 사용 금지
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	};

	protected void onDestroy() { // 어플리케이션이 종료될때 호출되는 함수
		connectToDevice.close();
		super.onDestroy();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rec, menu);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}