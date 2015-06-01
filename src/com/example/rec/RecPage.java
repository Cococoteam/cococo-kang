package com.example.rec;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.app.AlertDialog;
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
	int blockSize, apneaCount, snoringCount;
	//재생,정지버튼
	Button mStartBtn, mPlayBtn, showAndClose;
	//녹음상태를 나타내기위한 변수 true=녹음중, false=녹음중아님
	boolean isRecording;
	//녹음파일이 저징될 위치
	String recordingFile, tmpFileInforPath, tmpFileInforPath1;
	File fileInforPath;
	FileOutputStream fileInfor;
	FileWriter writer, writer1;
	
	//녹음시간을 나타내기 위한 변수
	Chronometer cm;
	//decibel를 나타내기위한 텍스트변수
	TextView decibel,decibel_Title;
	//saveDecibel의 index
	int secondLength;
	
	FileReader fr;
	String s;
	String[] tmps;
	// showAndCliseState = true 이면 그래프가 보이지 않는상태
	// showAndCliseState = false 이면 그래프가 보이는 상태
	boolean checkAndSendState, showAndCloseState, isApneaCheck;
	LinearLayout linear;
	
	//그래프를 저장하기 위한 변수들
	private static GraphicalView staticView;
	private RecPage_LineGraph staticLine;
	private RecPage_Point staticPoint;
	private graphDraw DrawTask;
	private Bitmap bitmap;
	private File file;
	private String graphPath;
	
	AlertDialog check;
	int[][] tmpdB;
	
	checkAndSend checkandsend;
	apenaTask apneaCheck;
	
	int standardValue; boolean testBoolean;
	
	String countPath;
	String countName;
	
	Button button1;
	
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
		showAndClose = (Button)findViewById(R.id.showandclose);
		cm = (Chronometer)findViewById(R.id.chronometer1);
		decibel = (TextView)findViewById(R.id.decibel);
		decibel_Title = (TextView)findViewById(R.id.decibel_title);
		button1 = (Button) findViewById(R.id.button1);
		
		button1.setText("업");
		
		mPlayBtn.setEnabled(false);
		isRecording = false;
		//true이면 checkandsend 실행중, false이면 실행중이지 않음
		checkAndSendState = false;
		showAndCloseState = false;
		isApneaCheck = false;
		realdB = 0;
		apneaCount = 0;
		snoringCount = 0;
		tmpdB = new int[20][2];
		
		standardValue = 60;
		testBoolean = true;
		
		dynamicPoint = new RecPage_Point();
		dynamicLine = new RecPage_LineGraph(1);
		dynamicView = dynamicLine.getView(this);
		dynamicGraphLayout.addView(dynamicView);
		
		staticPoint = new RecPage_Point();
		staticLine = new RecPage_LineGraph(2);
		staticView = staticLine.getView(this);
		staticGraphLayout.addView(staticView);
		
		String tmpData = "2\n";
		try {
			com.example.rec.Rec.mOutputStream.write(tmpData.getBytes());
			System.out.println("시작하자마자 블루투스에 " + tmpData + " 전송");
		} catch (IOException e) { System.out.println("데이터 전송 실패!!");	}
		
		
		button1.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v){
				if(standardValue == 60){
					button1.setText("다운");
					standardValue = 200;
				}
				else{
					button1.setText("업");
					standardValue = 60;
				}
			}
		});
		
		mStartBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (!isRecording) {//녹음 중이 아니라면, 녹음시작 버튼을 눌렀다면
					try {
						dynamicLine.clearAll();
						staticLine.clearAll();
						AudioReader = new RecPage_AudioReader();
						AudioReader.initReader();
						tmpFileInforPath = AudioReader.getRecordingFile();
						tmpFileInforPath = tmpFileInforPath.replace("pcm", "txt");
						writer = new FileWriter(tmpFileInforPath);
						tmpFileInforPath1 = tmpFileInforPath.replace("txt", "text");
						writer1 = new FileWriter(tmpFileInforPath1);
						isRecording = true;
						recordTask = new RecordAudio();
						recordTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						cm.setBase(SystemClock.elapsedRealtime());
						cm.start();
						mPlayBtn.setEnabled(false);
						mStartBtn.setBackgroundResource(R.drawable.recstop);
						}
					 catch (IOException e) { e.printStackTrace(); }
				}//end if
				else{// 녹음 중이라면
					try {
						if(checkAndSendState){
							checkandsend.cancel(true);
							checkAndSendState = false;
						}
						if(com.example.rec.Rec.pairingSuccess){
							apneaCheck.cancel(true);
						}
						writer.write(Integer.toString(AudioReader.getsecondLength()));
						writer.close();
						writer1.close();
						cm.stop();
						isRecording = false;
						recordTask.cancel(true);
						recordingFile = AudioReader.getRecordingFile();
						graphPath = recordingFile.replace("pcm", "png");
						countName = String.valueOf(snoringCount)+ " " + String.valueOf(apneaCount);
						countPath = graphPath.replace("png", "count");
						AudioReader.stopReader();
						AudioReader = null;
						mPlayBtn.setEnabled(true);
						mStartBtn.setBackgroundResource(R.drawable.recstart);
					}
					catch (IOException e) { e.printStackTrace(); }
				}//end else if
			}//end onClick(View v)
		});
		
		mPlayBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View V) {
				dynamicGraphLayout.setVisibility(View.GONE);
				
				DrawTask = new graphDraw();
	    		DrawTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	    		
	    		mStartBtn.setEnabled(false);
	    		mPlayBtn.setEnabled(false);
	    		showAndClose.setEnabled(false);
			}//end onClick
		});
		showAndClose.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View V){
				if(!showAndCloseState){
					showAndCloseState = true;
					showAndClose.setBackgroundResource(R.drawable.show);
					dynamicGraphLayout.setVisibility(View.INVISIBLE);
					staticGraphLayout.setVisibility(View.INVISIBLE);
				}
				else{
					showAndCloseState = false;
					showAndClose.setBackgroundResource(R.drawable.close);
					dynamicGraphLayout.setVisibility(View.VISIBLE);
					staticGraphLayout.setVisibility(View.VISIBLE);	
				}
			}
		});
	}
	
	private class apenaTask extends AsyncTask<Void, Void, Void>{
		int readValue;
		protected Void doInBackground(Void... params) {
			try {
				while(com.example.rec.Rec.pairingSuccess){
					readValue = com.example.rec.Rec.mInputStream.read();
					apneaCount += readValue;
				}
			}
			catch (IOException e) {
				System.out.println("값을 받는대 실패함");
				isApneaCheck = false;
			}
			System.out.println(isApneaCheck);
			return null;
		}
	}
	
	private class RecordAudio extends AsyncTask<Void, Integer, Void> {
		int i=0;
		protected Void doInBackground(Void... params) {
				try { Thread.sleep(200); }
				catch (InterruptedException e1) { e1.printStackTrace(); }
			while (isRecording) {
				try {
					if(com.example.rec.Rec.pairingSuccess && !isApneaCheck){
						apneaCheck = new apenaTask();
						apneaCheck.execute();
					}
					else if(isApneaCheck)
						apneaCheck.cancel(true);
					
					if(i % 10 == 0){
						secondLength = AudioReader.getsecondLength();
						writer.write(Integer.toString(secondLength) + " ");
					}
					dynamicLine.mRenderer.setXAxisMin(i-15);
					dynamicLine.mRenderer.setXAxisMax(i + 1);
					realdB = AudioReader.getdB() + 100;
					tmpdB[i%20][0] = i;
					tmpdB[i%20][1] = realdB;
					publishProgress(i++);
					//0.1초에 한번씩 데시벨을 파일에 저장
					//파일에 저장하는 이유는 배열에 저장하게되면 2분만 넘어가도 시스템이 느려짐
					//저장이 필요한 이유는 재생할때 보여줄 그래프를 그리기 위해
					//재생할떄 achartengine으로 그래프를 그리면 30분짜리 녹음의 그래프 그리는데
					//시간도 오래걸리고 전체적으로 시스템이 느려짐
					//재생 하기전에 그래프를 그리게 되면 시스템이 매우 느려짐.
					writer1.write(Integer.toString(realdB) + " ");
					if(realdB > standardValue  && !checkAndSendState){
						checkAndSendState = true;
						checkandsend = new checkAndSend();
						checkandsend.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
					if(checkAndSendState && checkandsend.getStatus() == AsyncTask.Status.FINISHED){
						checkAndSendState = false;
						checkandsend.cancel(true);
					}
					Thread.sleep(100);
				}
				catch (InterruptedException e) { e.printStackTrace(); }
				catch (IOException e) { e.printStackTrace(); }
			}//end while
			return null;
		}//end doInBackground

		protected void onProgressUpdate(Integer... params) {
			if(!showAndCloseState){
				if(i % 20 == 0){
					if(i != 0)
						dynamicLine.removeData(tmpdB, dynamicPoint);
				}
				dynamicPoint.setXY(params[0], realdB);
				dynamicLine.addNewPoints(dynamicPoint);// Add it to our graph
				dynamicView.repaint();
			}
			decibel.setText(String.valueOf(realdB));
		}//end onProgressUpdate
	}

    private class graphDraw extends AsyncTask<Void, Void, Void>{
		FileReader fr;
		String s;
		String[] tmps;
		FileWriter countWriter;
    	protected Void doInBackground(Void... params) {
			try {
				fr = new FileReader(tmpFileInforPath1);
				s = ascTochar();
				tmps = s.split(" ");
				tmps[0] = "0";
	    		staticLine.mRenderer.setXAxisMin(0);
				staticLine.mRenderer.setXAxisMax(tmps.length);
				publishProgress();
			} 
			catch (FileNotFoundException e) { e.printStackTrace(); }
			return null;
		}
		protected void onProgressUpdate(Void... params) {
			for(int i=0; i < tmps.length; i++){
				staticPoint.setXY(i, Integer.valueOf(tmps[i]));
				staticLine.addNewPoints(staticPoint);
			}
			
			staticView.repaint();
			
			bitmap = staticView.toBitmap();
			String FileName = graphPath;
			try {
				file = new File(FileName);
				OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
				bitmap.compress(CompressFormat.PNG, 100, output);
				output.close();
				fr.close();
				
				countWriter = new FileWriter(countPath);
				countWriter.write(countName);
				countWriter.close();
			}//try
			catch (IOException e) { System.out.println("저장 실패!!"); }
			staticGraphLayout.setVisibility(View.GONE);
	    	dynamicGraphLayout.setVisibility(View.VISIBLE);
	    	
	    	mStartBtn.setEnabled(true);
    		mPlayBtn.setEnabled(true);
    		showAndClose.setEnabled(true);
    		 	
    		Intent PlayActivity = new Intent(RecPage.this, MediaPlay.class);
			PlayActivity.putExtra("pcmPath", recordingFile);
			PlayActivity.putExtra("fileInforPath", tmpFileInforPath);
			PlayActivity.putExtra("graphPath", graphPath);
			PlayActivity.putExtra("countPath", countPath);
			startActivity(PlayActivity);
			Toast.makeText(RecPage.this, "방금 녹음한 파일이 재생됩니다.", Toast.LENGTH_LONG).show();
		}
	    public String ascTochar(){
	    	int i;
	    	String ts = null;
	    	try {
				while((i = fr.read()) != -1) 
					ts = ts+(char)i;
			}//end try
	    	catch (IOException e) { System.out.println("acsTochar오류!"); }
	    	return ts;
	    }//end acsTochar
    }

    private class checkAndSend extends AsyncTask<Void, Integer, Void>{
    	int count, loop;
    	String state="1\n";
    	protected Void doInBackground(Void... params) {
    		try {
        	  	count = 0;
        	   	loop = 0;
        		while(loop < 20){
        			if(realdB > 60)
        				count++;
        			loop++;
        			Thread.sleep(100);
        		}//end while(loop)
        		if(count > 0){
        			snoringCount++;
        			com.example.rec.Rec.mOutputStream.write(state.getBytes()); // 문자열 전송
        			System.out.println("코골아서 문자열 전송!!");
        		}//end if(count > -1)
			}//end try
    		catch (InterruptedException e) { System.out.println("슬립 오류"); }
    		// 문자열 전송 도중 오류가 발생한 경우
    		catch (Exception e) { System.out.println("전송 도중 오류"); }
    		return null;
		}//end doInBackground
    }//end checkAndSend
       
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