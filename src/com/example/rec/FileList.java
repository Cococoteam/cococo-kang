package com.example.rec;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import javax.activation.FileDataSource;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileList extends Activity {
	
	String sdPath; //sd카드 경로
    File sdDir; //sd카드 디렉토리폴더
    ArrayList<MyItem> recordList;
    ListView recordListView;
    MyListAdapter recordListViewAdapter;
    LinearLayout layout;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.filelist);
        layout=(LinearLayout) findViewById(R.id.filelist_ui);
        layout.setBackgroundResource(R.drawable.backimg);
        
        // pcm 파일추출
        sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        sdDir = new File(sdPath+"/Android/data/com.example.rec/"); 
        
        FilenameFilter filter = new FilenameFilter(){
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".pcm");
			}
        };
        
        // 추출한 mp3파일들을 파일목록 List에 저장
        String[] mplist = sdDir.list(filter);
        recordList = new ArrayList<MyItem>();
        MyItem mi;
        try{
        	for(String s: mplist){
        		mi = new MyItem(R.drawable.list_icon, s);
        		recordList.add(mi);
        	}
        	recordListViewAdapter = new MyListAdapter(this,R.layout.view, recordList);
        	recordListView = (ListView) findViewById(R.id.list);
        	recordListView.setAdapter(recordListViewAdapter);
        }
        catch(NullPointerException e) {Toast.makeText(FileList.this, "녹음된 파일이 없습니다.", Toast.LENGTH_LONG).show();}
    }//end onCreate

    //리스트뷰에 출력할 항목
    class MyItem{
    	MyItem(int aIcon, String aName){
    		Icon = aIcon;
    		Name = aName;
    	}
    	int Icon;
    	String Name;
    }
    
    //어댑어 클래스
    class MyListAdapter extends BaseAdapter{
    	Context maincon;
    	LayoutInflater Inflater;
    	ArrayList<MyItem> arSrc;
    	int layout;
    	
    	public MyListAdapter(Context context, int alayout, ArrayList<MyItem> aarSrc){
    		maincon = context;
    		Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		arSrc = aarSrc;
    		layout = alayout;
    	}
		public int getCount() { return arSrc.size(); }
		public Object getItem(int position) { return arSrc.get(position).Name; }
		public long getItemId(int position) { return position; }
		
		//각 항목의 뷰 생성
		//position 인수는 생성할 항목의 순서값, parent는 생성되는 뷰의 부모, 즉 리스트뷰
		//converView는 이전에 생성된 차일드 뷰
		public View getView(int position, View convertView, ViewGroup parent) {
			final int pos = position;
			if (convertView == null){
				convertView = Inflater.inflate(layout, parent,false);
			}
			ImageView img = (ImageView) convertView.findViewById(R.id.img);
			img.setImageResource(arSrc.get(pos).Icon);
			
			TextView txt = (TextView) convertView.findViewById(R.id.text);
			txt.setText("  "+arSrc.get(pos).Name);
			
			
			Button btn = (Button) convertView.findViewById(R.id.btn);
			Button play = (Button) convertView.findViewById(R.id.play);
			btn.setOnClickListener(new Button.OnClickListener(){
				public void onClick(View v) {
					final LinearLayout linear = (LinearLayout) View.inflate(FileList.this , R.layout.mailform, null);
					String Path = arSrc.get(pos).Name;
					TextView filename = (TextView) linear.findViewById(R.id.filename);
					TextView graphName = (TextView) linear.findViewById(R.id.graphname);
					filename.setText(Path);
					
					String Path2 = sdDir.getPath()+ "/" + Path.replace("pcm", "png");
					File f2 = new File(Path2);
					if(f2.exists())
						graphName.setText(Path.replace("pcm", "png"));
					else
						graphName.setText(" ");
					
					new AlertDialog.Builder(FileList.this).setTitle("첨부파일 E-mail 보내기").setIcon(R.drawable.app_icon)
			    	.setView(linear).setNegativeButton("확인", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							EditText send = (EditText) linear.findViewById(R.id.sendemail);
							EditText password = (EditText) linear.findViewById(R.id.password);
							EditText receivemail = (EditText) linear.findViewById(R.id.receivemail);
							EditText title = (EditText) linear.findViewById(R.id.title);
													
							Email emailform = new Email();
							emailform.send = send.getText().toString();
							emailform.password = password.getText().toString();
							emailform.receivemail = receivemail.getText().toString();
							emailform.title = title.getText().toString();
							emailform.filename = sdPath+"/Android/data/com.example.rec/"+arSrc.get(pos).Name;
							String Path2 = new String((sdPath+"/Android/data/com.example.rec/"+arSrc.get(pos).Name).replace("pcm", "png"));
							File f2 = new File(Path2);
							if(f2.exists())
								emailform.filename1 = (sdPath+"/Android/data/com.example.rec/"+arSrc.get(pos).Name).replace("pcm", "png");
							else
								emailform.filename1 = "0";
							
							GMailSender sender = new GMailSender(emailform.send, emailform.password);
							try {
								sender.sendMail(emailform.title, emailform.send, emailform.receivemail, emailform.filename, emailform.filename1);
							} catch (Exception e) {
								Log.e("error", e.getMessage(), e);
							}
						}
			    	}).setPositiveButton("취소", null).show();
				}//end onClick
			});
			
			play.setOnClickListener(new Button.OnClickListener(){
				public void onClick(View v) {
					String pcmPath = sdPath+"/Android/data/com.example.rec/"+arSrc.get(pos).Name;
					String txtPath = pcmPath.replace("pcm", "txt");
					String graphPath = pcmPath.replace("pcm", "png");
					Intent PlayActivity = new Intent(FileList.this, MediaPlay.class);
					PlayActivity.putExtra("pcmPath", pcmPath);
					PlayActivity.putExtra("fileInforPath", txtPath);
					PlayActivity.putExtra("graphPath", graphPath);
					startActivity(PlayActivity);
				}
			});
			return convertView;
		}// end getView()
    }
    
    public class Email{
    	String send;
    	String password;
    	String receivemail;
    	String title;
    	String filename;
    	String filename1;
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
