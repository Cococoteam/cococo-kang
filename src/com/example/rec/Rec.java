package com.example.rec;

import android.app.*;
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
        findViewById(R.id.control).setOnClickListener(mClickListener);
        findViewById(R.id.setting).setOnClickListener(mClickListener);
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
    		case R.id.control : 
    			Toast.makeText(Rec.this, "아두이노 기기제어", Toast.LENGTH_SHORT).show();
    			break;
    		case R.id.setting : 
    			if(Vib)
    				Vib = false;
    			else
    				Vib = true;
    			break;
    		}
    	}
    };
   
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