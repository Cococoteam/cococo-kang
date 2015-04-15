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
	Vibrator mVib;
	boolean Vib = true;
	LinearLayout layout;
	static final int REQUEST_ENABLE_BT = 10;
	 int mPairedDeviceCount = 0;
	 Set<BluetoothDevice> mDevices;
	 BluetoothAdapter mBluetoothAdapter;
	 BluetoothDevice mRemoteDevice;
	 BluetoothSocket mSocket = null;
	 OutputStream mOutputStream = null;
	 InputStream mInputStream = null;
	 String mStrDelimiter = "\n";
	 char mCharDelimiter = '\n';
	 Thread mWorkerThread = null;
	 byte[] readBuffer;
	 int readBufferPosition;
	 EditText mEditReceive, mEditSend;
	 Button mButtonSend;

    protected void onCreate(Bundle savedInstanceState) {   
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mainui);
        mVib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        layout=(LinearLayout)findViewById(R.id.main);
		layout.setBackgroundResource(R.drawable.backimg);
        findViewById(R.id.recording).setOnClickListener(mClickListener);
        findViewById(R.id.select).setOnClickListener(mClickListener);
        findViewById(R.id.connection).setOnClickListener(mClickListener);
        findViewById(R.id.bluetooth).setOnClickListener(mClickListener);
        findViewById(R.id.setting).setOnClickListener(mClickListener);
        findViewById(R.id.search).setOnClickListener(mClickListener);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
   }
    Button.OnClickListener mClickListener=new OnClickListener(){
    	public void onClick(View v){
    		if(Vib)
    			mVib.vibrate(100);
    		switch(v.getId()){
    		case R.id.recording : 
    			Log.i("onClick", "recording");
    			Intent recordingActivity=new Intent(Rec.this,RecPage.class);
    			startActivity(recordingActivity);
    			break;
    		case R.id.select : 
    			Log.i("onClick", "select");
    			Intent selectActivity=new Intent(Rec.this,FileList.class);
    			startActivity(selectActivity);
    			break;
    		case R.id.connection :
    			Log.i("onClick", "connection");
    			Intent connectionActivity=new Intent(Rec.this,MapPage.class);
    			startActivity(connectionActivity);
    			break;
    		case R.id.bluetooth : 
    			Log.i("onClick", "bluetooth");
    			if(!mBluetoothAdapter.isEnabled()){
    				Intent turnon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    				startActivityForResult(turnon, 0);
    				Toast.makeText(getApplicationContext(), "블루투스가 켜집니다.", Toast.LENGTH_LONG).show();
    			}
    			else{
    				Toast.makeText(getApplicationContext(), "이미 켜져있습니다.", Toast.LENGTH_LONG).show();
    			}
    			break;
    		case R.id.setting : 
    			if(Vib)
    				Vib = false;
    			else
    				Vib = true;
    			break;
    			
    		case R.id.search :
    			mDevices = mBluetoothAdapter.getBondedDevices();
    			  mPairedDeviceCount = mDevices.size();
    			  if(mPairedDeviceCount == 0){
    			   // 페어링 된 장치가 없는 경우
    			   Toast.makeText(getApplicationContext(),"페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
    			   finish();// 어플리케이션 종료
    			  }
    			  
    			  AlertDialog.Builder builder = new AlertDialog.Builder(Rec.this);
    			  builder.setTitle("블루투스 장치 선택");
    			  // 페어링 된 블루투스 장치의 이름 목록 작성
    			  
    			  List<String> listItems = new ArrayList<String>();
    			  for (BluetoothDevice device : mDevices) {
    			   listItems.add(device.getName());
    			  }
    			  listItems.add("취소");// 취소 항목 추가
    			  
    			  final CharSequence[] items =listItems.toArray(new CharSequence[listItems.size()]);
    			  
    			  builder.setItems(items, new DialogInterface.OnClickListener(){
    			   public void onClick(DialogInterface dialog, int item){
    			    if(item == mPairedDeviceCount){
    			     // 연결할 장치를 선택하지 않고 ‘취소’를 누른 경우
    			     Toast.makeText(getApplicationContext(),"연결할 장치를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
    			     finish();
    			    }else{
    			     // 연결할 장치를 선택한 경우
    			     // 선택한 장치와 연결을 시도함
    			     connectToSelectedDevice(items[item].toString());
    			     sendData("a");
    			    }
    			   }
    			  });
    			  builder.setCancelable(false); // 뒤로 가기 버튼 사용 금지
    			  AlertDialog alert = builder.create();
    			  alert.show();

    		}
    	}
    };
    BluetoothDevice getDeviceFromBondedList(String name){ //해당 블루투스 장치 객체를 페어링 된 장치 목록에서 찾아내기
    	  BluetoothDevice selectedDevice = null;
    	  for (BluetoothDevice device : mDevices) 
    	  {
    	   if(name.equals(device.getName()))
    	   {
    	    selectedDevice = device;
    	    break;
    	   }
    	  }
    	  return selectedDevice;
    	 }
    	 
    	 void sendData(String msg){  //데이터 전송
    	  msg += mStrDelimiter; // 문자열 종료 표시
    	  try{
    	   mOutputStream.write(msg.getBytes()); // 문자열 전송
    	  }catch(Exception e){
    	   // 문자열 전송 도중 오류가 발생한 경우
    	   Toast.makeText(getApplicationContext(),"데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
    	   finish();
    	  }
    	 }
    	 
    	 void connectToSelectedDevice(String selectedDeviceName){ //소켓
    	  mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
    	  UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    	  try{
    	   // 소켓 생성
    	   mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
    	   // RFCOMM 채널을 통한 연결
    	   mSocket.connect();
    	   
    	   Toast.makeText(getApplicationContext(),"블루투스 연결이 성공하였습니다.", Toast.LENGTH_LONG).show();
    	   
    	   
    	   // 데이터 송수신을 위한 스트림 얻기
    	   mOutputStream = mSocket.getOutputStream();
    	   mInputStream = mSocket.getInputStream();
    	   
    	   // 데이터 수신 준비
    	   beginListenForData();
    	   }catch(Exception e){
    	    // 블루투스 연결 중 오류 발생
    	    Toast.makeText(getApplicationContext(),"블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
    	    finish(); // 어플리케이션 종료
    	   }
    	  }
    	 
    	 void beginListenForData(){ //데이터 수신
    	  final Handler handler = new Handler();
    	  
    	  readBufferPosition = 0; // 버퍼 내 수신 문자 저장 위치
    	  readBuffer = new byte[1024]; // 수신 버퍼
    	  
    	  // 문자열 수신 쓰레드
    	  mWorkerThread = new Thread(new Runnable(){
    	   public void run(){
    	    while(!Thread.currentThread().isInterrupted()){
    	     try {
    	      int bytesAvailable = mInputStream.available(); // 수신 데이터 확인
    	      if(bytesAvailable > 0){         // 데이터가 수신된 경우
    	       byte[] packetBytes = new byte[bytesAvailable];
    	       mInputStream.read(packetBytes);
    	       for(int i = 0; i < bytesAvailable; i++){
    	        byte b = packetBytes[i];
    	        if(b == mCharDelimiter){
    	         byte[] encodedBytes = new byte[readBufferPosition];
    	         System.arraycopy(readBuffer, 0,encodedBytes, 0, encodedBytes.length);
    	         final String data = new String(encodedBytes, "utf-8");
    	         readBufferPosition = 0;
    	         handler.post(new Runnable(){
    	          public void run(){
    	           // 수신된 문자열 데이터에 대한 처리 작업
    	           mEditReceive.setText(mEditReceive.getText().toString()+ data + mStrDelimiter);
    	          }
    	         });
    	        }
    	        else{
    	         readBuffer[readBufferPosition++] = b;
    	        }
    	       }
    	      }
    	     }
    	     catch (IOException ex){
    	      // 데이터 수신 중 오류 발생
    	      Toast.makeText(getApplicationContext(),"데이터 수신 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
    	      finish();
    	     }
    	    }
    	   }
    	  });
    	  mWorkerThread.start();
    	 }
    	
    	   
    	 protected void onDestroy() { //어플리케이션이 종료될때  호출되는 함수
    		 try{
    		  mWorkerThread.interrupt(); // 데이터 수신 쓰레드 종료
    		  mInputStream.close();
    		  mOutputStream.close();
    		  mSocket.close();
    		  }catch(Exception e){}
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