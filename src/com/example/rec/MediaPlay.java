package com.example.rec;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MediaPlay extends Activity {
	Button play,stop; //player 기능 버튼
	TextView mp3Filename, snore, apnea; //재생 파일 이름
	SeekBar mp3SeekBar; //재생 진행바
	String playingFile, fileInfor;
	int position; //
	boolean isPlay, isPause;
	LinearLayout layout, graphLayout;
	Intent getsdPath;
	
	//재생 음질을 위한 변수
	private int frequency = 8000;
	//재생 채널의 수를 저장하고 있는 변수
	private final int channelOutConfiguration = AudioFormat.CHANNEL_OUT_MONO;
	//PCM샘플 변수
	private final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	
	PlayAudio playTask;
	AudioTrack audioTrack;
	
	int bufferSize;
	
	DataInputStream dis;
	FileReader fr, countReader;
	int initLength;
	int nowLength;
	String[] dataSplite;
	
	Bitmap originImage = null;
	ImageView iv;
	String graphPath, countPath;
	
	int second, chooseSecond, recentSecond;
	progressbar updateProgressbar;
	
	String s = new String();
	String[] tmps;
	
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mediaplay);
        layout = (LinearLayout) findViewById(R.id.mediaplay_ui);
        layout.setBackgroundResource(R.drawable.backimg);
        
        play = (Button) findViewById(R.id.play);
        stop = (Button) findViewById(R.id.stop);
        mp3Filename = (TextView) findViewById(R.id.filename);
        snore = (TextView) findViewById(R.id.snore);
        apnea = (TextView) findViewById(R.id.apnea);
        mp3SeekBar = (SeekBar) findViewById(R.id.progress);
        
        iv = (ImageView) findViewById(R.id.graphView);
        
        play.setBackgroundResource(R.drawable.pause_icon);
        
        getsdPath = getIntent();
        playingFile = getsdPath.getStringExtra("pcmPath");
        fileInfor = getsdPath.getStringExtra("fileInforPath");
        graphPath = getsdPath.getStringExtra("graphPath");
        countPath = getsdPath.getStringExtra("countPath");
        dataSplite = playingFile.split("/");
        mp3Filename.setText("재생 파일 명: " + dataSplite[7]);
        
        try {
			countReader = new FileReader(countPath);
			char ts, ts1, ts2;
	    	ts = (char) countReader.read();
	    	ts1 = (char) countReader.read();
	    	ts2 = (char) countReader.read();
			snore.setText(String.valueOf(ts)+ " ");
			apnea.setText(String.valueOf(ts2));
		} 
        catch (FileNotFoundException e1) { e1.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
        originImage = BitmapFactory.decodeFile(graphPath);
        iv.setImageBitmap(originImage);
        
        isPlay = true; isPause = false;

        try {
        	dis = new DataInputStream(new BufferedInputStream(new FileInputStream(playingFile)));
        	fr = new FileReader(fileInfor);
        	s = ascTochar();
        	tmps = s.split(" "); // 가장마지막 파일의 크기 * 640가 전체 파일의 크기
        	tmps[0] = "0";
			initLength = dis.available();
        }
        catch (FileNotFoundException e) { System.out.println("파일 찾기 실패!!"); }
        catch (IOException e) { System.out.println("입출력 오류!!");; }
        
        mp3SeekBar.setMax(tmps.length-1);
        position = 0;
        
        bufferSize = AudioTrack.getMinBufferSize(frequency, channelOutConfiguration, audioEncoding);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelOutConfiguration, audioEncoding, bufferSize, AudioTrack.MODE_STREAM);
        
        playTask = new PlayAudio();
        playTask.execute();
        
        updateProgressbar = new progressbar();
        updateProgressbar.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
		//재생 (일시정지) 버튼 이벤트 처리
		play.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(isPause || !isPlay){ //일시정지 중이였다면 재생
						isPlay = true;
						isPause = false;
						playTask = new PlayAudio();
						playTask.execute();
						mp3SeekBar.setProgress(position);
						updateProgressbar = new progressbar();
						updateProgressbar.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, position);
						play.setBackgroundResource(R.drawable.pause_icon);
				}else{//재생중이라면 재생을 일시정지
						isPlay = false;
						isPause = true;
						audioTrack.stop();
						audioTrack.flush();
						playTask.cancel(true);
						playTask = null;
						updateProgressbar.cancel(true);
						updateProgressbar = null;
						play.setBackgroundResource(R.drawable.start_icon);
				}//end else
			}//end onClick(View v)
		});// end play.setOnClickListener
			
		//재생멈춤 버튼 이벤트 처리
		stop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(isPlay){
					try {
					isPlay = false;
					isPause = false;
					audioTrack.stop();
					audioTrack.flush();
					playTask.cancel(true);
					playTask = null;
					updateProgressbar.cancel(true);
					updateProgressbar = null;
					position = 0;
					mp3SeekBar.setProgress(0);
					dis.close();
					dis = new DataInputStream(new BufferedInputStream(new FileInputStream(playingFile)));
					play.setBackgroundResource(R.drawable.start_icon);
					}//end try 
					catch (IOException e) { e.printStackTrace(); }
				}//end if
			}//end onClick
		});//end setOnClickListener

		//progressBar 변경시 재생되는 곡의 재생위치 변경
		 mp3SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			// 바에 터치시에는 paues
				public void onStartTrackingTouch(SeekBar seekBar) {
					if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
						try {
							isPlay = false;
							isPause = true;
							dis.close();
							dis = new DataInputStream(new BufferedInputStream(new FileInputStream(playingFile)));
							audioTrack.stop();
							audioTrack.flush();
							playTask.cancel(true);
							playTask = null;
							updateProgressbar.cancel(true);
							updateProgressbar = null;
						}
						catch (IOException e) { System.out.println("available 실패!!"); }
					}//end if
				}//end onStartTrackingTouch

			 public void onStopTrackingTouch(SeekBar seekBar) {
				for(int i=1; i <= chooseSecond; i++)
					nowLength = Integer.valueOf(tmps[i]) * 640;
				try { dis.skip(nowLength); }
				catch (IOException e) { System.out.println("skip실패!!"); }
				isPlay = true;
				isPause = false;
				playTask = new PlayAudio();
				playTask.execute();
				position = chooseSecond;
				updateProgressbar = new progressbar();
				updateProgressbar.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, position);
			}//end onStopTrackingTouch
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser)
					chooseSecond = seekBar.getProgress();
			}//end onProgressChanged
		});	 

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
    
    // PlayAudio playTask;
    private class PlayAudio extends AsyncTask<Void, Integer, Void> {
    	protected Void doInBackground(Void... params) {
    		short[] audiodata = new short[bufferSize / 4];
    		try {
    			audioTrack.play();
    			while (isPlay && dis.available() > 0) {
    				int i = 0;
    				audioTrack.write(audiodata, 0, audiodata.length);
    				while (dis.available() > 0 && i < audiodata.length) {
    					audiodata[i] = dis.readShort();
    					i++;
    				}//end while
    			}// end while
    		} //end try
    		catch (Throwable t) { Log.e("AudioTrack", "Playback Failed"); }
    		
    		return null;
    	}
	}
    
    private class progressbar extends AsyncTask<Integer, Integer, Void> {
    	int index;
    	protected Void doInBackground(Integer... params) {
    		index = params[0];
    		try {
    			while(isPlay){
    				publishProgress(index++);
    				position = index;
    				Thread.sleep(1000);
    			}
			} 
    		catch (InterruptedException e) { e.printStackTrace(); }
			return null;
    	}
    	
    	protected void onProgressUpdate(Integer...value){
    		mp3SeekBar.setProgress(value[0]);
    	}
    }
    
    public String ascTochar(){
    	int i;
    	String ts = null;
    	try {
			while((i = fr.read()) != -1) ts = ts+(char)i;
		}//end try
    	catch (IOException e) { System.out.println("acsTochar오류!"); }
    	return ts;
    }//end acsTochar
    
    
    protected void onStop(){
    	super.onStop();
    	try {
    		if(playTask != null){
    			isPlay = false;
    			isPause = false;
    			playTask.cancel(true);
    			audioTrack.stop();
    			audioTrack.flush();
    			playTask = null;
    			position = 0;
    			mp3SeekBar.setProgress(0);
    			updateProgressbar.cancel(true);
    			updateProgressbar = null;
    			dis.close();
    			play.setBackgroundResource(R.drawable.start_icon);
    		}
		}//end try 
		catch (IOException e) { e.printStackTrace(); }
    }
}