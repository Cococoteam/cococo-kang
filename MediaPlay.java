package com.example.rec;

import java.io.*;
import java.util.*;

import android.app.*;
import android.media.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class MediaPlay extends Activity {
	Button play,stop; //player 기능 버튼
	TextView mp3Filename; //mp3 파일 이름
	SeekBar mp3SeekBar; //mp3 진행바
	String sdPath; //sd카드 경로
	File sdDir; //sd카드 디렉토리폴더
	ArrayList<String> mp3List; //sd카드의 mp3파일 목록
	int position; //mp3 파일 재생위치
	ListView mp3ListView; //재생 전체 목록을 보여줄 ListView
	ArrayAdapter<String> mp3ListViewAdapter;
	MediaPlayer mediaPlayer;
	boolean isPlaying;
	LinearLayout layout;
	
	//mp3 한곡이 재생되는 위치를 0.1 초마다 받아와서 progessBar의 진행위치를 갱신
		Handler handler = new Handler(){			
			public void handleMessage(Message msg){
				if(mediaPlayer==null)return;
				if(mediaPlayer.isPlaying()){
					mp3SeekBar.setProgress(mediaPlayer.getCurrentPosition());
				}
				handler.sendEmptyMessageDelayed(0, 100);
				}
		};
				
		//액티비티 종료시 재생 강제 종료
		public void onDestroy(){
			super.onDestroy();
			if(mediaPlayer !=null){
				mediaPlayer.release();
				mediaPlayer = null;
			}
		}
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mediaplay);
        layout=(LinearLayout) findViewById(R.id.mediaplay_ui);
        layout.setBackgroundResource(R.drawable.backimg);
        
        //1. findViewById
        play = (Button) findViewById(R.id.play);
        stop =(Button) findViewById(R.id.stop);
        mp3Filename = (TextView) findViewById(R.id.filename);
        mp3SeekBar = (SeekBar) findViewById(R.id.progress);
        
/*
      //3. SD카드 경로에서 FilenameFilter 사용하여 mp3 파일 추출 .pcm
        sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        sdDir = new File(sdPath); //sd 카드 디렉터리 폴더 File sdDir
        FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".pcm");
			}
		};
*/		
		//4. 추출한  mp3파일들을 파일목록List에 저장
				String[] mplist = sdDir.list(filter);
				if(mplist.length==0){
					Toast.makeText(this, "재생할 파일이 없습니다.", 1).show();
					finish();
					return;
				}
				mp3List = new ArrayList<String>();
				for(String s: mplist){
					mp3List.add(sdPath +"/"+s);
				
				}
				position = 0;
				
		//5. mp3 파일 목록을 나타낼 ListView 설정
			mp3ListViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,mp3List);
			mp3ListView.setAdapter(mp3ListViewAdapter);
			
		//6. mediaPlayer 설정
			mediaPlayer = new MediaPlayer();
			handler.sendEmptyMessageDelayed(0, 100);	
		
			//6. 재생목록에서 첫번째 mp3 파일 불러오기
			setDataSourcePrepare(position);
			
			/***********이벤트처리 ************/
			//재생 (일시정지) 버튼 이벤트 처리
			play.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mediaPlayer.isPlaying()==false){
						mediaPlayer.start();
						play.setText("∥");
					}else{
						mediaPlayer.pause();
						play.setText("▶");
					}
				}
			});
			
			//재생멈춤 버튼 이벤트 처리
			stop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mediaPlayer.stop();
					play.setText("▶");
					mediaPlayer.reset();
					mp3SeekBar.setProgress(0);
					setDataSourcePrepare(position);
				}
			});
			 //progressBar 변경시 재생되는 곡의 재생위치 변경
			 mp3SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					
				}
				// 바에 터치시에는 paues
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					isPlaying = mediaPlayer.isPlaying();
					if(isPlaying){
						mediaPlayer.pause();
					}
				}
				
				//바 변경이 발생하면 mediaPlayer.seekTo 호출
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					if(fromUser){
						//mediaPlayer의 time position 이동
						mediaPlayer.seekTo(progress);
						//mediaPlayer.SetOnSeekCompleteListener 호출됨
					}
					
				}
			});
 
			 //재생위치 변경시 호출 (seekTo() 메서드 호출시에 콜백)
			 mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
				@Override
				public void onSeekComplete(MediaPlayer mp) {
					if(isPlaying){
						mediaPlayer.start();
					}
				}
			});
			 //재생 목록 (ListView) 아이템 클릭시 해당곡 이동 or 재생
			 mp3ListView.setOnItemClickListener(new OnItemClickListener() {
				 public void onItemClick(AdapterView<?> viewGroup, View view, int position,
		                    long id) {
		                isPlaying = mediaPlayer.isPlaying();
		                mediaPlayer.reset();
		                setDataSourcePrepare(position);
		                // 재생중에  아이템을 클릭 한 경우는  선택곡 재생
		                if (isPlaying) {
		                    mediaPlayer.start();
		                    play.setText("∥");
		                }
		            }
		        });				
    }
    /****************재생목록에서 mp3 파일 플레이어에 로딩하는 메소드 *************/	
	private void setDataSourcePrepare(int position){
		try {
			mediaPlayer.setDataSource(mp3List.get(position));
			mediaPlayer.prepare();
			mp3Filename.setText("now playing : "+ mp3List.get(position));
			mp3SeekBar.setMax(mediaPlayer.getDuration());
		} catch (Exception e) {
			Log.v("player", e.getMessage()+"재생목록 mp3파일 셋팅 실패!");
			Toast.makeText(getBaseContext(), "재생목록에서 mp3파일 셋팅 실패!", Toast.LENGTH_SHORT).show();
			//activity 종료
			finish();
		}
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
}
// PlayAudio playTask;
/*
private class PlayAudio extends AsyncTask<Void, Integer, Void> {
	@Override
	protected Void doInBackground(Void... params) {
		isPlaying = true;

		int bufferSize = AudioTrack.getMinBufferSize(frequency,
				outChannelConfig, audioEncoding);
		short[] audiodata = new short[bufferSize / 4];

		try {
			DataInputStream dis = new DataInputStream(
					new BufferedInputStream(new FileInputStream(
							recordingFile)));

			AudioTrack audioTrack = new AudioTrack(
					AudioManager.STREAM_MUSIC, frequency,
					outChannelConfig, audioEncoding, bufferSize,
					AudioTrack.MODE_STREAM);

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

			//startPlaybackButton.setEnabled(false);
			//stopPlaybackButton.setEnabled(true);

		} catch (Throwable t) { Log.e("AudioTrack", "Playback Failed"); }
		return null;
	}
}
*/