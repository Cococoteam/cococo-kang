package com.example.rec;

import android.graphics.*;
import android.os.*;
import android.support.v4.app.*;
import android.util.*;

import com.google.android.gms.common.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.*;

public class MapPage extends FragmentActivity implements OnMapClickListener {
	private GoogleMap mGoogleMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapui);
		
		
		// BitmapDescriptorFactory 생성하기 위한 소스

		MapsInitializer.initialize(getApplicationContext());
		
		mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		GooglePlayServicesUtil.isGooglePlayServicesAvailable(MapPage.this);
		mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		
		// mGoogleMap.addMarker(new MarkerOptions().position(new
		// LatLng(37.38391, 126.64385)));//송도 센트럴 이비인후과
		MarkerOptions opt1 = new MarkerOptions();
		opt1.position(new LatLng(37.38391, 126.64385));// 위도 • 경도
		opt1.title("송도 센트럴 이비인후과");// 제목 미리보기
		opt1.snippet("032-831-9972");
		// optFirst.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		mGoogleMap.addMarker(opt1).showInfoWindow();

		// mGoogleMap.addMarker(new MarkerOptions().position(new
		// LatLng(37.39526, 126.65188)));//에코이비인후과
		MarkerOptions opt2 = new MarkerOptions();
		opt2.position(new LatLng(37.39526, 126.65188));// 위도 • 경도
		opt2.title("김지범 이비인후과");// 제목 미리보기
		opt2.snippet("032-831-9972");
		// optFirst.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		mGoogleMap.addMarker(opt2).showInfoWindow();

		// mGoogleMap.addMarker(new MarkerOptions().position(new
		// LatLng(37.39341, 126.64615)));//코아이비인후과
		MarkerOptions opt3 = new MarkerOptions();
		opt3.position(new LatLng(37.39341, 126.64615));// 위도 • 경도
		opt3.title("코아이비인후과");// 제목 미리보기
		opt3.snippet("032-831-9972");
		// optFirst.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		mGoogleMap.addMarker(opt3).showInfoWindow();

		// mGoogleMap.addMarker(new MarkerOptions().position(new
		// LatLng(37.40716, 126.67226)));//굿모닝이비인후과
		MarkerOptions opt4 = new MarkerOptions();
		opt4.position(new LatLng(37.40716, 126.67226));// 위도 • 경도
		opt4.title("굿모닝이비인후과");// 제목 미리보기
		opt4.snippet("032-831-9972");
		// optFirst.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		mGoogleMap.addMarker(opt4).showInfoWindow();

		// mGoogleMap.addMarker(new MarkerOptions().position(new
		// LatLng(37.40814, 126.67149)));//수이비인후과
		MarkerOptions opt5 = new MarkerOptions();
		opt5.position(new LatLng(37.40814, 126.67149));// 위도 • 경도
		opt5.title("수이비인후과");// 제목 미리보기
		opt5.snippet("032-831-9972");
		// optFirst.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		mGoogleMap.addMarker(opt5).showInfoWindow();

		// mGoogleMap.addMarker(new MarkerOptions().position(new
		// LatLng(37.41213, 126.67737)));//선아이비인후과
		MarkerOptions opt6 = new MarkerOptions();
		opt6.position(new LatLng(37.41213, 126.67737));// 위도 • 경도
		opt6.title("선아이비인후과");// 제목 미리보기
		opt6.snippet("032-831-9972");
		// optFirst.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		mGoogleMap.addMarker(opt6).showInfoWindow();

		// mGoogleMap.addMarker(new MarkerOptions().position(new
		// LatLng(37.41362, 126.67628)));//성수이비인후과
		MarkerOptions opt7 = new MarkerOptions();
		opt7.position(new LatLng(37.41362, 126.67628));// 위도 • 경도
		opt7.title("성수이비인후과");// 제목 미리보기
		opt7.snippet("032-831-9972");
		// optFirst.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		mGoogleMap.addMarker(opt7).showInfoWindow();

		// mGoogleMap.addMarker(new MarkerOptions().position(new
		// LatLng(37.41507, 126.67778)));//한빛이비인후과
		MarkerOptions opt8 = new MarkerOptions();
		opt8.position(new LatLng(37.41507, 126.67778));// 위도 • 경도
		opt8.title("한빛이비인후과");// 제목 미리보기
		opt8.snippet("032-831-9972");
		// optFirst.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		mGoogleMap.addMarker(opt8).showInfoWindow();

		// mGoogleMap.addMarker(new MarkerOptions().position(new
		// LatLng(37.42177, 126.67009)));//신이비인후과
		MarkerOptions opt9 = new MarkerOptions();
		opt9.position(new LatLng(37.42177, 126.67009));// 위도 • 경도
		opt9.title("신이비인후과");// 제목 미리보기
		opt9.snippet("032-831-9972");
		// optFirst.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		mGoogleMap.addMarker(opt9).showInfoWindow();
		init();
	}

	/** Map 클릭시 터치 이벤트 */
	public void onMapClick(LatLng point) {

		// 현재 위도와 경도에서 화면 포인트를 알려준다
		Point screenPt = mGoogleMap.getProjection().toScreenLocation(point);

		// 현재 화면에 찍힌 포인트로 부터 위도와 경도를 알려준다.
		LatLng latLng = mGoogleMap.getProjection().fromScreenLocation(screenPt);

		Log.d("맵좌표", "좌표: 위도(" + String.valueOf(point.latitude) + "), 경도("
				+ String.valueOf(point.longitude) + ")");
		Log.d("화면좌표", "화면좌표: X(" + String.valueOf(screenPt.x) + "), Y("
				+ String.valueOf(screenPt.y) + ")");
	}

	/**
	 * 초기화
	 * 
	 * @author
	 */
	private void init() {

		GooglePlayServicesUtil.isGooglePlayServicesAvailable(MapPage.this);
		mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		// 맵의 이동
		// mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,
		// 15));

		GpsInfo gps = new GpsInfo(MapPage.this);
		// GPS 사용유무 가져오기
		if (gps.isGetLocation()) {
			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();

			// Creating a LatLng object for the current location
			LatLng latLng = new LatLng(latitude, longitude);

			// Showing the current location in Google Map
			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

			// Map 을 zoom 합니다.
			mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(13));

			// 마커 설정.
			MarkerOptions optFirst = new MarkerOptions();
			optFirst.position(latLng);// 위도 • 경도
			optFirst.title("현재 위치");// 제목 미리보기
			optFirst.snippet("안웅모");
			optFirst.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.ic_launcher));
			mGoogleMap.addMarker(optFirst).showInfoWindow();
		}
	}
}
