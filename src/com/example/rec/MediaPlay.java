package com.example.rec;

import java.io.*;
import java.util.*;

import org.achartengine.GraphicalView;

import android.app.*;
import android.content.Intent;
import android.media.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class MediaPlay extends Activity {
	Button play,stop; //player 기능 버튼
	TextView mp3Filename; //녹음 파일 이름
	SeekBar mp3SeekBar; //녹음 진행바
	String playingFile;
	int position; //녹음 파일 재생위치
	boolean isPlaying;
	LinearLayout layout, graphLayout;
	Intent getsdPath;
	
	//녹음 음질을 위한 변수
	private int frequency = 8000;
	//녹음 채널의 수를 저장하고 있는 변수
	private final int channelOutConfiguration = AudioFormat.CHANNEL_OUT_MONO;
	//PCM샘플 변수
	private final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	
	PlayAudio playTask;
	AudioTrack audioTrack;
	
	int bufferSize;
	ArrayList<Integer> saveDecibel;
	//Graphdraw GraphTask;
	
	private static GraphicalView view;
	private MediaPlay_LineGraph line;
	
	DataInputStream dis;
	int initLength;
	int nowLength;
	String[] dataSplite;
	
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mediaplay);
        layout = (LinearLayout) findViewById(R.id.mediaplay_ui);
        layout.setBackgroundResource(R.drawable.backimg);
        graphLayout = (LinearLayout) findViewById(R.id.graphLayout);
        
        play = (Button) findViewById(R.id.play);
        stop =(Button) findViewById(R.id.stop);
        mp3Filename = (TextView) findViewById(R.id.filename);
        mp3SeekBar = (SeekBar) findViewById(R.id.progress);
        
        getsdPath = getIntent();
        playingFile = getsdPath.getStringExtra("Path");
        saveDecibel = getsdPath.getIntegerArrayListExtra("saveDecibel");
        dataSplite = playingFile.split("/");
        mp3Filename.setText("재생 파일 명: "+dataSplite[7]);
        
        mp3SeekBar.setMax(saveDecibel.size());
        
        try { 
        	dis = new DataInputStream(new BufferedInputStream(new FileInputStream(playingFile)));
			initLength = dis.available();
			nowLength = initLength;
        }
        catch (FileNotFoundException e) { System.out.println("파일 찾기 실패!!"); }
        catch (IOException e) { System.out.println("입출력 오류!!");; }
        
        position = 0;
        line = new MediaPlay_LineGraph();
        
        bufferSize = AudioTrack.getMinBufferSize(frequency, channelOutConfiguration, audioEncoding);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelOutConfiguration, audioEncoding, bufferSize, AudioTrack.MODE_STREAM);
        
        //GraphTask = new Graphdraw();
        //GraphTask.execute();
        
        playTask = new PlayAudio();
        playTask.execute();
        
		//재생 (일시정지) 버튼 이벤트 처리
		play.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!isPlaying){ //재생중이지 않다면 재생을 시작
					try {
						dis.reset();
						playTask = new PlayAudio();
						playTask.execute();
						play.setText("∥");
					} 
					catch (IOException e) { System.out.println("reset 실패!!"); }
				}else{//재생중이라면 재생을 잠시 멈춤
					try {
						nowLength = dis.available();
						dis.mark(nowLength);
						audioTrack.stop();
						audioTrack.flush();
						playTask.cancel(true);
						playTask = null;
						play.setText("▶");
					} 
					catch (IOException e) { System.out.println("available 실패!!"); }
				}//end else
			}//end onClick(View v)
		});// end play.setOnClickListener
			
		//재생멈춤 버튼 이벤트 처리
		stop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(isPlaying){
					audioTrack.stop();
					audioTrack.flush();
					playTask.cancel(true);
					playTask = null;
					nowLength = initLength;
					mp3SeekBar.setProgress(0);
					play.setText("▶");
				}
			}
		});
		 //progressBar 변경시 재생되는 곡의 재생위치 변경
		 mp3SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) { }
			// 바에 터치시에는 paues
			public void onStartTrackingTouch(SeekBar seekBar) {
				if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
					audioTrack.pause();
				}
			}
			//바 변경이 발생하면 mediaPlayer.seekTo 호출
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				//audioTrack.flush();
				
				/*
				if(fromUser){
					//mediaPlayer의 time position 이동
					mediaPlayer.seekTo(progress);
					//mediaPlayer.SetOnSeekCompleteListener 호출됨
				}
				*/
			}
		});	 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.rec, menu);
        return true;
    }
    @Override
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
/*    
    private class Graphdraw extends AsyncTask<Void, Void, Void>{
		protected Void doInBackground(Void... params) {
			line.mRenderer.setXAxisMin(0);
			line.mRenderer.setXAxisMax(saveDecibel.size());
			publishProgress();
			return null;
		}
		protected void onProgressUpdate(Void... params) {
			for(int i=0; i<saveDecibel.size(); i++){
				MediaPlay_Point p = new MediaPlay_Point(i, saveDecibel.get(i));
				line.addNewPoints(p);
			}
			view.repaint();
		}
    }
*/    
    // PlayAudio playTask;
    private class PlayAudio extends AsyncTask<Void, Integer, Void> {
    	protected Void doInBackground(Void... params) {
    		short[] audiodata = new short[bufferSize / 4];
    		int index=0;
    		isPlaying = true;
    		try {
    			audioTrack.play();
    			while (isPlaying && dis.available() > 0) {
    				int i = 0;
    				while (dis.available() > 0 && i < audiodata.length) {
    					audiodata[i] = dis.readShort();
    					i++;
    				}//end while (dis.available() > 0 && i < audiodata.length)
    				audioTrack.write(audiodata, 0, audiodata.length);
    				publishProgress(++index);
    			}// end while (isPlaying && dis.available() > 0)
    		} //end try
    		catch (Throwable t) { Log.e("AudioTrack", "Playback Failed"); }
    		isPlaying = false;
    		return null;
    	}
    	protected void onProgressUpdate(Integer...value){
    		mp3SeekBar.setProgress(value[0]);
    	}
    	protected void onCancelled(){
    		isPlaying = false;
    	}
	}
/*    
    protected void onStart(){
		super.onStart();
		view = line.getView(this);
		graphLayout.addView(view);
    }
*/   
    protected void onDestory(){
    	try {
			dis.close();
			System.out.println("파일 닫힘.");
		} 
    	catch (IOException e) { System.out.println("파일 닫기 싫패!!"); }
    }
}