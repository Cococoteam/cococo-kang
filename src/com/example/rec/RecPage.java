package com.example.rec;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.Toast;

public class RecPage extends Activity {
	LinearLayout layout;
	
	final int High = 44100;
	final int Middle = 11025;
	final int Low = 8000;
	RecordAudio recordTask;
	int frequency = Middle;
	int inchannelConfig = AudioFormat.CHANNEL_IN_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	
	Button mStartBtn, mPlayBtn;
	boolean isRecording;
	
	String sdPath;
	String recordingFile;
	String[] dateSplit;
	String date1;
	
	Chronometer cm;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.recui);
		layout = (LinearLayout)findViewById(R.id.RecUI);
		layout.setBackgroundResource(R.drawable.backimg);
		mStartBtn = (Button)findViewById(R.id.recorded);
		mPlayBtn = (Button)findViewById(R.id.play);
		cm = (Chronometer)findViewById(R.id.chronometer1);
		mPlayBtn.setEnabled(false);
		isRecording = false;
		
		mStartBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (isRecording == false) {
					sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
					Date date = new Date();
					date1 = date.toString();
					dateSplit = date1.split(" ");
					date1 = dateSplit[3];
					File Path = new File(sdPath+"/Android/data/com.example.rec");
					if( !Path.exists()) Path.mkdirs(); //Path경로에 디렉토리가 없다면 생성
					recordingFile = Path + "/" + date1 + ".pcm";
					recordTask = new RecordAudio();
					recordTask.execute();
					cm.setBase(SystemClock.elapsedRealtime());
					cm.start();
					isRecording = true;
					mPlayBtn.setEnabled(true);
					mStartBtn.setText("녹음중지");
				} else {
					cm.setBase(SystemClock.elapsedRealtime());
					cm.stop();
					recordTask.cancel(true);
					isRecording = false;
					mStartBtn.setText("녹음시작");
				}
			}
		});

		mPlayBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View V) {
					Intent PlayActivity = new Intent(RecPage.this, MediaPlay.class);
					PlayActivity.putExtra("Path", recordingFile);
					startActivity(PlayActivity);
					Toast.makeText(RecPage.this, "방금 녹음한 파일이 재생됩니다.", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private class RecordAudio extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... params) {
			try {
				DataOutputStream dos = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream(recordingFile)));

				int bufferSize = AudioRecord.getMinBufferSize(frequency, inchannelConfig, audioEncoding);

				AudioRecord audioRecord = new AudioRecord(
						MediaRecorder.AudioSource.MIC, frequency,
						inchannelConfig, audioEncoding, bufferSize);

				short[] buffer = new short[bufferSize];
				audioRecord.startRecording();
				while (isRecording) {
					int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
					for (int i = 0; i < bufferReadResult; i++) {
						dos.writeShort(buffer[i]);
					}
					//publishProgress(Integer.valueOf(r));
				}
				audioRecord.stop();
				dos.close();
			} 
			catch (Throwable t) { Log.e("AudioRecord", "Recording Failed"); }
			return null;
		}
/*
		protected void onProgressUpdate(Integer... progress) {
			statusText.setText(progress[0].toString());
		}
*/
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
