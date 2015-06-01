package com.example.rec;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class RecPage_AudioReader {
	//녹음 음질을 위한 변수
	private int frequency = 8000;
	//녹음 채널의 수를 저장하고 있는 변수
	private final int channelInConfiguration = AudioFormat.CHANNEL_IN_MONO;
	//PCM샘플 변수
	private final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	//AudioSource.MIC 민감한 오디오 녹음 AudioSOurce.VIOCE_RECOGNITION 덜 민감한 오디오 녹음
	private final int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
	
	AudioRecord audioInput;
	//Decibel을 계산하기 위한 상수
	private static final float MAX_16_BIT = 32768;
	private static final float FUDGE = 0.6f;
	
	//녹음파일의 저장 경로를 얻어오기 위한 변수
	String sdPath;
	String recordingFile;
	String[] dateSplit;
	String date1;
	//파일에 저장을 하기 위한 버퍼
	short[] buffer;
	//녹음을 위한 버퍼
	int bufferSize;
	//저장을 위한 스트림
	DataOutputStream dos;
	//녹음을 시작하기 위한 쓰레드 객체
	Thread readerThread;
	//녹음 상황을 확인하기 위한 변수
	boolean running;
	//secondLength = MediPlay에서 녹음된 파일을 재생 중 프로그레스바를 움직였을 때
	//해당 위치에서 재생을 시작해야하나. AudioTrack은 바이트단위로 재생할 파일을 읽어오기에
	//프로스레스바위치에 따른 녹음 파일 내 재생 위치를 정확히 알려면 매초에서의 파일의 길이를 알아야함.
	//파일의 길이는 bufferReadResult * i(최종 반복횟수) (단 bufferReadResult == 320 이때만 반복횟수 누적
	//320인이유는 잘모름... bufferReadResult의 값을 출력해보면 320임
	int dB, secondLength;
	//폴더의 경로를 저장하기 위한 변수
	File Path;
	
	public void initReader() {
		//SD카드에 폴더를 생성하기 위해 SD카드의 경로를 불러옴
		sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		Path = new File(sdPath+"/Android/data/com.example.rec");
		//폴더가 없다면 생성, 있으면 넘김
		if( !Path.exists()) Path.mkdirs();
		//녹음을 눌렀을때의 시,분,초를 파일명으로 지정하기 위해 호출
		Date date = new Date();
		date1 = date.toString();
		dateSplit = date1.split(" ");
		date1 = dateSplit[3];
		// 폴더명/파일명.pcm
		recordingFile = Path + "/" + date1 + ".pcm";
		try{ 
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(recordingFile)));
		}
		catch(Throwable t) {}
		
		bufferSize = AudioRecord.getMinBufferSize(frequency, channelInConfiguration, audioEncoding);
		audioInput = new AudioRecord(audioSource, frequency, channelInConfiguration, audioEncoding, bufferSize);
		//저장을 하기 위한 버퍼
		buffer = new short[bufferSize];
		running = true;
		readerThread = new Thread(new Runnable() { public void run() {readerRun();} }, "Audio Reader");
		readerThread.start();
	}

	public void stopReader() {
		running = false;
		try { dos.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		try {
			if (readerThread != null)
				readerThread.join();
		}
		catch (InterruptedException e) {}
		readerThread = null;
		audioInput.release();
		audioInput = null;
	}

	private void readerRun() {
		secondLength = 0;
		if (audioInput.getState() != AudioRecord.STATE_INITIALIZED) {
			running = false;
			return;
		}
		
		audioInput.startRecording();
		
		try {
			while (running) {
				int bufferReadResult = audioInput.read(buffer, 0, bufferSize);
				secondLength++;
				for (int i = 0; i < bufferReadResult; i++)
					dos.writeShort(buffer[i]);
				dB = calculatePowerDb(buffer, 0, buffer.length);
			}//end while(running)
		}//end try
		catch (IOException e) {e.printStackTrace();}
	}//end readerRun()
	
	public int calculatePowerDb(short[] sdata, int off, int samples) {
		double sum = 0;
		double sqsum = 0;
		long v;
		for (int i = 0; i < samples; i++) {
			v = sdata[off + i];
			sum += v;
			sqsum += v * v;
		}

		// sqsum is the sum of all (signal+bias)², so
		// sqsum = sum(signal²) + samples * bias²
		// hence
		// sum(signal²) = sqsum - samples * bias²
		// Bias is simply the average value, i.e.
		// bias = sum / samples
		// Since power = sum(signal²) / samples, we have
		// power = (sqsum - samples * sum² / samples²) / samples
		// so
		// power = (sqsum - sum² / samples) / samples
		double power = (sqsum - sum * sum / samples) / samples;

		// Scale to the range 0 - 1.
		power /= MAX_16_BIT * MAX_16_BIT;

		// Convert to dB, with 0 being max power. Add a fudge factor to make
		// a "real" fully saturated input come to 0 dB.
		double result = Math.log10(power) * 10f + FUDGE;
		return (int) result;
	}
	
	public String getRecordingFile(){
		return recordingFile;
	}
	
	public String getFilePath(){
		return sdPath+"/Android/data/com.example.rec/";
	}
	public int getdB(){
		return dB;
	}
	public int getsecondLength(){
		return secondLength;
	}
}