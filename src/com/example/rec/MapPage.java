package com.example.rec;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class MapPage extends Activity {
 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mapui);
    }
}
