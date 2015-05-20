package com.example.rec;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import org.achartengine.GraphicalView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
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
	LinearLayout layout, dynamicGraphLayout, staticGraphLayout;
	//녹음을 위한 변수
	RecordAudio recordTask;
	RecPage_AudioReader AudioReader;
	//계산된 Decibel을 받아오기 위한 변수
	static int realdB;
	//실시간 그래프를 그리기 위한 변수
	private static GraphicalView dynamicView;
	private RecPage_LineGraph dynamicLine;
	private RecPage_Point dynamicPoint;
	int blockSize;
	//재생,정지버튼
	Button mStartBtn, mPlayBtn;
	//녹음상태를 나타내기위한 변수 true=녹음중, false=녹음중아님
	boolean isRecording;
	//녹음파일이 저징될 위치
	String recordingFile;
	//녹음시간을 나타내기 위한 변수
	Chronometer cm;
	//decibel를 나타내기위한 텍스트변수
	TextView decibel;
	//decibel을 저장하기 위한 배열
	ArrayList<Integer> saveDecibel;
	//saveDecibel의 index
	int index;
	
	//그래프를 저장하기 위한 변수들
	private static GraphicalView staticView;
	private RecPage_LineGraph staticLine;
	private RecPage_Point staticPoint;
	private graphDraw DrawTask;
	private Bitmap bitmap;
	private File file;
	private String graphPath;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//액티비티 상단의 제목표시줄(TitleBar)를 없애줌
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.recui);
		layout = (LinearLayout)findViewById(R.id.RecUI);
		dynamicGraphLayout = (LinearLayout)findViewById(R.id.danamicGraphLayout);
		staticGraphLayout = (LinearLayout)findViewById(R.id.staticGraphLayout);
		layout.setBackgroundResource(R.drawable.backimg);
		mStartBtn = (Button)findViewById(R.id.recorded);
		mPlayBtn = (Button)findViewById(R.id.play);
		cm = (Chronometer)findViewById(R.id.chronometer1);
		decibel = (TextView)findViewById(R.id.decibel);
		mPlayBtn.setEnabled(false);
		isRecording = false;
		realdB = 0;
		
		dynamicPoint = new RecPage_Point();
		dynamicLine = new RecPage_LineGraph(1);
		dynamicView = dynamicLine.getView(this);
		dynamicGraphLayout.addView(dynamicView);
		
		staticPoint = new RecPage_Point();
		staticLine = new RecPage_LineGraph(2);
		staticView = staticLine.getView(this);
		staticGraphLayout.addView(staticView);
		
		saveDecibel = new ArrayList<Integer>();
		
		mStartBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (isRecording == false) {//녹음 중이 아니라면, 녹음시작 버튼을 눌렀다면
					dynamicLine.clearAll();
					staticLine.clearAll();
					saveDecibel.clear();
					AudioReader = new RecPage_AudioReader();
					AudioReader.initReader();
					isRecording = true;
					recordTask = new RecordAudio();
					recordTask.execute();
					cm.setBase(SystemClock.elapsedRealtime());
					cm.start();
					mPlayBtn.setEnabled(false);
					mStartBtn.setText("녹음중지");
				}//end if
				else if(isRecording == true) {// 녹음 중이라면
					Toast.makeText(RecPage.this, "배열의 크기"+saveDecibel.size(), Toast.LENGTH_LONG).show();
					cm.stop();
					isRecording = false;
					recordTask.cancel(true);
					recordingFile = AudioReader.getRecordingFile();
					graphPath = recordingFile.replace(".pcm", ".png");
					AudioReader.stopReader();
					AudioReader = null;
					mPlayBtn.setEnabled(true);
					mStartBtn.setText("녹음시작");
				}//end else if
			}//end onClick(View v)
		});
		
		mPlayBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View V) {
				mPlayBtn.setEnabled(false);
				dynamicGraphLayout.setVisibility(View.GONE);
				DrawTask = new graphDraw();
	    		DrawTask.execute();
				Intent PlayActivity = new Intent(RecPage.this, MediaPlay.class);
				PlayActivity.putExtra("pcmPath", recordingFile);
				PlayActivity.putExtra("graphPath", graphPath);
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
					dynamicLine.mRenderer.setXAxisMin(i-15);
					dynamicLine.mRenderer.setXAxisMax(i + 1);
					publishProgress(i++);
					realdB = AudioReader.getdB() + 100;
					//0.1초에 한번씩 데시벨을 배열에 저장
					//배열이 필요한 이유는 재생할때 X축-시간 Y축-데시벨을 나타내기 위해
					saveDecibel.add(realdB);
					Thread.sleep(100);
				} catch (InterruptedException e) { e.printStackTrace(); }
			}//end while
			return null;
		}//end doInBackground

		protected void onProgressUpdate(Integer... params) {
			dynamicPoint.setXY(params[0], realdB);
			dynamicLine.addNewPoints(dynamicPoint);// Add it to our graph
			dynamicView.repaint();
			decibel.setText(String.valueOf(realdB));
		}//end onProgressUpdate
	}

    private class graphDraw extends AsyncTask<Void, Void, Void>{
		protected Void doInBackground(Void... params) {
			staticLine.mRenderer.setXAxisMin(0);
			staticLine.mRenderer.setXAxisMax(saveDecibel.size());
			publishProgress();
			return null;
		}
		protected void onProgressUpdate(Void... params) {
			for(int i=0; i<saveDecibel.size(); i++){
				staticPoint.setXY(i, saveDecibel.get(i));
				staticLine.addNewPoints(staticPoint);
			}
			staticView.repaint();
			bitmap= staticView.toBitmap();
			String FileName = graphPath;
			try {
				file = new File(FileName);
				OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
				bitmap.compress(CompressFormat.PNG, 100, output);
				output.close();
			}//try
			catch (IOException e) { System.out.println("저장 실패!!"); }
			staticGraphLayout.setVisibility(View.GONE);
	    	dynamicGraphLayout.setVisibility(View.VISIBLE);
		}
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