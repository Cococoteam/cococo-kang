package com.example.rec;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class FileList extends Activity {
	
	String sdPath; //sd카드 경로
    File sdDir; //sd카드 디렉토리폴더
    ArrayList<MyItem> recordList;
    ListView recordListView;
    MyListAdapter recordListViewAdapter;
    LinearLayout layout;
    
    @Override
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
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".pcm");
			}
        };
        
        // 추출한 mp3파일들을 파일목록 List에 저장
        String[] mplist = sdDir.list(filter);
        if(mplist.length==0){
        	Toast.makeText(this, "재생할 파일이 없습니다.", 1).show();
        	finish();
        	return;
        }
        recordList = new ArrayList<MyItem>();
        MyItem mi;
        for(String s: mplist){
        	//mi = new MyItem(R.drawable.ic_launcher, sdPath+s);
        	mi = new MyItem(R.drawable.ic_launcher, s);
        	recordList.add(mi);
        }

        recordListViewAdapter = new MyListAdapter(this,R.layout.view, recordList);
        recordListView = (ListView) findViewById(R.id.list);
        recordListView.setAdapter(recordListViewAdapter);      
    }

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
		@Override
		public int getCount() { return arSrc.size(); }
		@Override
		public Object getItem(int position) { return arSrc.get(position).Name; }
		@Override
		public long getItemId(int position) { return position; }
		
		//각 항목의 뷰 생성
		//position 인수는 생성할 항목의 순서값, parent는 생성되는 뷰의 부모, 즉 리스트뷰
		//converView는 이전에 생성된 차일드 뷰
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int pos = position;
			if (convertView == null){
				convertView = Inflater.inflate(layout, parent,false);
			}
			ImageView img = (ImageView) convertView.findViewById(R.id.img);
			img.setImageResource(arSrc.get(position).Icon);
			
			TextView txt = (TextView) convertView.findViewById(R.id.text);
			txt.setText(arSrc.get(position).Name);
			
			Button btn = (Button) convertView.findViewById(R.id.btn);
			btn.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View v) {
					String str = arSrc.get(pos).Name +"를 전송합니다..";
					Toast.makeText(maincon, str, Toast.LENGTH_SHORT).show();
				}
			});	
			return convertView;
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
