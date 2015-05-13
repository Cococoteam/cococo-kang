package com.example.rec;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.Intent;
import android.media.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class MediaPlay extends Activity {
	Button play,stop; //player 기능 버튼
	TextView mp3Filename; //녹음 파일 이름
	SeekBar mp3SeekBar; //녹음 진행바
	String playingFile;
	int position; //녹음 파일 재생위치
	boolean isPlaying;
	LinearLayout layout;
	Intent getsdPath;
	
	//녹음 음질을 위한 변수
	private final int High = 44100;
	private final int Middle = 11025;
	private final int Low = 8000;
	//사용자가 설정한 녹음 음질을 저장 있는 변수
	private int frequency = Middle;
	//녹음 채널의 수를 저장하고 있는 변수
	private final int channelOutConfiguration = AudioFormat.CHANNEL_OUT_MONO;
	//PCM샘플 변수
	private final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	
	PlayAudio playTask;
	
	int bufferSize;
	AudioTrack audioTrack;
	
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mediaplay);
        layout=(LinearLayout)findViewById(R.id.mediaplay_ui);
        layout.setBackgroundResource(R.drawable.backimg);
        
        play = (Button) findViewById(R.id.play);
        stop =(Button) findViewById(R.id.stop);
        mp3Filename = (TextView) findViewById(R.id.filename);
        mp3SeekBar = (SeekBar) findViewById(R.id.progress);
        
        getsdPath = getIntent();
        playingFile = getsdPath.getStringExtra("Path");
        position = 0;
        
        bufferSize = AudioTrack.getMinBufferSize(frequency, channelOutConfiguration, audioEncoding) * 2;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelOutConfiguration, audioEncoding, bufferSize, AudioTrack.MODE_STREAM);
        
        playTask = new PlayAudio();
        playTask.execute();
        
		//재생 (일시정지) 버튼 이벤트 처리
		play.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!isPlaying){ //재생중이지 않다면 재생을 시작
					isPlaying = true;
					audioTrack.play();
					play.setText("∥");
				}else{//재생중이라면 재생을 잠시 멈춤
					isPlaying = false;
					audioTrack.pause();
					play.setText("▶");
				}
			}
		});
			
		//재생멈춤 버튼 이벤트 처리
		stop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				isPlaying = false;
				audioTrack.stop();
				audioTrack.flush();
				mp3SeekBar.setProgress(0);
				play.setText("▶");
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
    
    // PlayAudio playTask;
    private class PlayAudio extends AsyncTask<Void, Integer, Void> { 	
    	protected Void doInBackground(Void... params) {
    		short[] audiodata = new short[bufferSize / 4];
    		try {
    			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(playingFile)));

    			audioTrack.play();
    			
    			while (isPlaying && dis.available() > 0) {
    				int i = 0;
    				while (dis.available() > 0 && i < audiodata.length) {
    					audiodata[i] = dis.readShort();
    					i++;
    				}
    				audioTrack.write(audiodata, 0, audiodata.length);
    			}
    			dis.close();
    		} catch (Throwable t) { Log.e("AudioTrack", "Playback Failed"); }
    		return null;
    	}
    	protected void onProgressUpdate(Integer...value){
    	//mp3 한곡이 재생되는 위치를 0.1 초마다 받아와서 progessBar의 진행위치를 갱신
    	/*	Handler handler = new Handler(){			
    			public void handleMessage(Message msg){
    				if(isPlaying){
    					mp3SeekBar.setProgress(mediaPlayer.getCurrentPosition());
    				}
    				handler.sendEmptyMessageDelayed(0, 100);
					}
    		};
    	*/
    	}
	}
}