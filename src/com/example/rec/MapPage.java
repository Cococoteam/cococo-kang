package com.example.rec;


import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;

import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class MapPage extends FragmentActivity {

	
	public static LatLng DEFAULT_GP = new LatLng(37.566500, 126.978000);// 서울

	// Minimum & maximum latitude so we can span it
	// The latitude is clamped between -80 degrees and +80 degrees inclusive
	// thus we ensure that we go beyond that number
	private double minLatitude =  +81;
	private double maxLatitude =  -81;

	// Minimum & maximum longitude so we can span it
	// The longitude is clamped between -180 degrees and +180 degrees inclusive
	// thus we ensure that we go beyond that number
	private double minLongitude = +181;
	private double maxLongitude = -181;

	protected GoogleMap mMap;
	private String errorString = "";
	private GoogleMapkiUtil httpUtil;
	private AlertDialog errorDialog;
	boolean startAsync;
	
	Location lo;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mapui);
		
		setUpMapIfNeeded();
		
		startAsync = false;
		
		// httpUtil
		httpUtil = new GoogleMapkiUtil();
		
		handler = new Handler(getMainLooper());
		goToSeoul();
		
		Toast.makeText(this, "내 위치를 찾고 있습니다...", Toast.LENGTH_LONG).show();
		
		new setMyLocation().execute();
	}
	
	private Handler handler;
	
    private void goToSeoul() {
        handler.post(findSeoul);
    }
    
    private Runnable findSeoul = new Runnable() {
        
        @Override
        public void run() {
            if(mMap != null) {
            	
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(DEFAULT_GP, 10f);
                mMap.moveCamera(cu);
                Log.d("dd", "OK seoul");
            }
            else {
                handler.postDelayed(findSeoul, 100);
            }
        }
    };
	
	public class setMyLocation extends AsyncTask<Void, Void, Void>{

		protected Void doInBackground(Void... params) {
			if(!startAsync){
				try {
					
					Thread.sleep(5000);
					publishProgress();
				} 
				catch (InterruptedException e) { 
					e.printStackTrace();
				}
			}
			return null;
		}
		protected void onProgressUpdate(Void... params) {
			lo = mMap.getMyLocation();
			String myAddr = getAddres(lo.getLatitude(), lo.getLongitude());
			System.out.println("myAddr = " + myAddr);
			httpUtil.requestMapSearch(new ResultHandler(MapPage.this), "이비인후과", myAddr);
			startAsync = true;
		}
	}
	
	private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }
	
	private void setUpMap() {
		Log.d("dd", "setUpMap");
		mMap.setMapType(MAP_TYPE_NORMAL);
		mMap.setMyLocationEnabled(true);
	}
	
	static class ResultHandler extends Handler {
		private final WeakReference<MapPage> mActivity;
		
		ResultHandler(MapPage activity) {
			mActivity = new WeakReference<MapPage>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			MapPage activity = mActivity.get();
			if(activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
	
	private void handleMessage(Message msg) {
		//progressDialog.dismiss();

		String result = msg.getData().getString(GoogleMapkiUtil.RESULT);
		ArrayList<String> searchList = new ArrayList<String>();

		if (result.equals(GoogleMapkiUtil.SUCCESS_RESULT)) {
			searchList = msg.getData().getStringArrayList("searchList");

		} else if (result.equals(GoogleMapkiUtil.TIMEOUT_RESULT)) {
			errorString = "네트워크 연결이 안됩니다.";
			errorDialog.setMessage(errorString);
			errorDialog.show();
			return;
		} else if (result.equals(GoogleMapkiUtil.FAIL_MAP_RESULT)) {
			errorString = "검색이 안됩니다.";
			errorDialog.setMessage(errorString);
			errorDialog.show();
			return;
		} else {
			errorString = httpUtil.stringData;
			errorDialog.setMessage(errorString);
			errorDialog.show();
			return;
		}
		
		String[] searches = searchList.toArray(new String[searchList.size()]);
		adjustToPoints(searches);
	}
	
	/**
	 * 주어진 위치들에 적합한 줌, 이동시킴
	 * 
	 * @param mPoints
	 */
	protected void adjustToPoints(String[] results) {
		mMap.clear();
		LatLng myStage = new LatLng(lo.getLatitude(), lo.getLongitude());
		
		int length = Integer.valueOf(results.length / 3);
		LatLng[] mPoints = new LatLng[length];
		double[][] latlngResult = new double[10][2];
		int count = 0;
		for (int i = 0; i < length; i++) {
			LatLng latlng = new LatLng( Float.valueOf(results[i * 3 + 1]), Float.valueOf(results[i * 3 + 2]));
            mMap.addMarker(new MarkerOptions().position(latlng).title(results[i * 3]).icon(BitmapDescriptorFactory.defaultMarker(i * 360 / length)));
            
            mPoints[i] = latlng;
        }
		
		
		for (LatLng ll : mPoints) {
			latlngResult[count][0] = ll.latitude - lo.getLatitude();
			latlngResult[count][1] = ll.longitude - lo.getLongitude();
			count++;
			// Sometimes the longitude or latitude gathering
			// did not work so skipping the point
			// doubt anybody would be at 0 0
			if (ll.latitude != 0 && ll.longitude != 0) {
				//// Sets the minimum and maximum latitude so we can span and zoom
				minLatitude = (minLatitude > ll.latitude) ? ll.latitude : minLatitude;
				maxLatitude = (maxLatitude < ll.latitude) ? ll.latitude : maxLatitude;
				// Sets the minimum and maximum latitude so we can span and zoom
				minLongitude = (minLongitude > ll.longitude) ? ll.longitude	: minLongitude;
				maxLongitude = (maxLongitude < ll.longitude) ? ll.longitude	: maxLongitude;
			}
		}
		Log.d("dd", minLatitude + "/" + maxLatitude + "/"+minLongitude + "/"+maxLongitude);
		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(myStage, 14f);
		mMap.animateCamera(cu);
	}

	public void onResume() {
		super.onResume();
        setUpMapIfNeeded();
	}
	
	private String getAddres(double lat, double lng) {
		Geocoder gcK = new Geocoder(getApplicationContext(), Locale.KOREA);
		String res = "정보없음";
		try {
			List<Address> addresses = gcK.getFromLocation(lat, lng, 1);
			StringBuilder sb = new StringBuilder();

			if (null != addresses && addresses.size() > 0) {
				Address address = addresses.get(0);
				// sb.append(address.getCountryName()).append("/");
				// sb.append(address.getPostalCode()).append("/");
				sb.append(address.getLocality()).append("/");
				sb.append(address.getThoroughfare()).append("/");
				sb.append(address.getFeatureName());
				res = sb.toString();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
}
