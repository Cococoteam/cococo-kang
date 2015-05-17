package com.example.rec;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import org.achartengine.GraphicalView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RecPage extends Activity {
	LinearLayout layout, graphLayout;
	//녹음을 위한 변수
	RecordAudio recordTask;
	RecPage_AudioReader AudioReader;
	//계산된 Decibel을 받아오기 위한 변수
	static int realdB;
	//실시간 그래프를 그리기 위한 변수
	private static GraphicalView view;
	private RecPage_LineGraph line = new RecPage_LineGraph();
	int blockSize;
	//재생,정지버튼
	Button mStartBtn, mPlayBtn;
	//녹음상태를 나타내기위한 변수 true=녹음중, false=녹음중아님
	boolean isRecording;
	//재생 눌렀을때 재생될 위치를 받아볼 변수
	String recordingFile;
	//녹음시간을 나타내기 위한 변수
	Chronometer cm;
	//decibel를 나타내기위한 텍스트변수
	TextView decibel;
	//decibel을 저장하기 위한 배열
	ArrayList<Integer> saveDecibel;
	//saveDecibel의 index
	int index;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//액티비티 상단의 제목표시줄(TitleBar)를 없애줌
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.recui);
		layout = (LinearLayout)findViewById(R.id.RecUI);
		graphLayout = (LinearLayout)findViewById(R.id.graphLayout);
		layout.setBackgroundResource(R.drawable.backimg);
		mStartBtn = (Button)findViewById(R.id.recorded);
		mPlayBtn = (Button)findViewById(R.id.play);
		cm = (Chronometer)findViewById(R.id.chronometer1);
		decibel = (TextView)findViewById(R.id.decibel);
		mPlayBtn.setEnabled(false);
		isRecording = false;
		realdB = 0;
		line = new RecPage_LineGraph();
		saveDecibel = new ArrayList<Integer>();
		
		mStartBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (isRecording == false) {
					AudioReader = new RecPage_AudioReader();
					AudioReader.initReader();
					isRecording = true;
					recordTask = new RecordAudio();
					recordTask.execute();
					cm.setBase(SystemClock.elapsedRealtime());
					cm.start();
					mPlayBtn.setEnabled(false);
					mStartBtn.setText("녹음중지");
				} else {
					cm.stop();
					isRecording = false;
					recordTask.cancel(true);
					recordingFile = AudioReader.getRecordingFile();
					AudioReader.stopReader();
					AudioReader = null;
					mPlayBtn.setEnabled(true);
					mStartBtn.setText("녹음시작");
				}
			}
		});

		mPlayBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View V) {
				mPlayBtn.setEnabled(false);
				Intent PlayActivity = new Intent(RecPage.this, MediaPlay.class);
				PlayActivity.putExtra("Path", recordingFile);
				PlayActivity.putIntegerArrayListExtra("saveDecibel", saveDecibel);
				startActivity(PlayActivity);
				Toast.makeText(RecPage.this, "방금 녹음한 파일이 재생됩니다.", Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private class RecordAudio extends AsyncTask<Void, Integer, Void> {
		int i=0;
		protected Void doInBackground(Void... params) {
			while (isRecording) {
				try {
					line.mRenderer.setXAxisMin(i-15);
					line.mRenderer.setXAxisMax(i + 1);
					publishProgress(i++);
					realdB = AudioReader.getdB() + 100;
					//1초에 한번씩 데시벨을 배열에 저장
					//배열이 필요한 이유는 재생할때 X축-시간 Y축-데시벨을 나타내기 위해
					if(i % 10 == 0){
						saveDecibel.add(realdB);
					}
					Thread.sleep(100);
				} catch (InterruptedException e) { e.printStackTrace(); }
			}//end while
			return null;
		}//end doInBackground

		protected void onProgressUpdate(Integer... params) {
			RecPage_Point p = new RecPage_Point(params[0], realdB);
			line.addNewPoints(p);// Add it to our graph
			view.repaint();
			decibel.setText(String.valueOf(realdB));
		}//end onProgressUpdate
	}
	
	protected void onStart(){
		super.onStart();
		view = line.getView(this);
		graphLayout.addView(view);
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