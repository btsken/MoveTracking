package com.example.running;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.db.Route;
import com.example.db.RouteHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class ActivityMessenger extends Activity {

	boolean mBound = false;;
	private Button switchBtn, saveBtn;
	private TextView infoTv, speedTv, distanceTv;
	private Geography geography;
	private GoogleMap map;
	public boolean isPause; // 是否開始記錄
	private boolean isGpsOk = false;
	private static final int ZOOM = 18;
	private List<LatLng> points;
	private RouteHelper routeHelper;
	private int groupId;
	private double distance;
	private int sec;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.e("activity", "onCreate");
		init();

		// 宣告Timer
		Timer timer01 = new Timer();

		// 設定Timer(task為執行內容，0代表立刻開始,間格1秒執行一次)
		timer01.schedule(task, 0, 1000);

		geography.startRecord();
		switchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchAction();
			}
		});

		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sec = 0;
				switchAction();
				saveRecord();
				map.clear();
				saveBtn.setEnabled(false);
			}
		});
	}

	private void switchAction() {
		if (isPause) {
			if (geography.isGpsOpen()) {
				recordLocation();
				switchBtn.setText(R.string.stop);
				saveBtn.setEnabled(true);
			} else {
				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		} else {
			isPause = true;
			switchBtn.setText(R.string.start);
		}
	}

	public void recordLocation() {
		if (geography.isGpsOpen()) {
			isPause = false;
		} else {
			Intent dialogIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(dialogIntent);
		}
	}

	// TimerTask無法直接改變元件因此要透過Handler來當橋樑
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				// 計算目前已過分鐘數
				int minius = sec / 60;
				// 計算目前已過秒數
				int seconds = sec % 60;
				infoTv.setText(String.format("%02d", minius) + ":" + String.format("%02d", seconds));
				break;
			}
		}
	};

	private TimerTask task = new TimerTask() {
		@Override
		public void run() {
			if (!isPause) {
				sec++;
				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);
			}
		}

	};

	public void saveRecord() {
		for (LatLng location : points) {
			routeHelper.create(new Route(location.latitude, location.longitude, groupId));
		}
	}

	private void findViews() {
		switchBtn = (Button) findViewById(R.id.button1);
		saveBtn = (Button) findViewById(R.id.button2);
		infoTv = (TextView) findViewById(R.id.info);
		speedTv = (TextView) findViewById(R.id.speed);
		distanceTv = (TextView) findViewById(R.id.distance);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	}

	private void init() {
		findViews();
		map.setMyLocationEnabled(true);
		geography = new Geography(this, locationListener, gpsListener);
		isPause = true;
		routeHelper = new RouteHelper(this.getApplicationContext());
		groupId = routeHelper.getMaxGroup();
		points = new ArrayList<LatLng>();
	}

	@Override
	public void finish() {
		moveTaskToBack(true); // move back
	}

	private void trackToMe(double lat, double lng) {

		points.add(new LatLng(lat, lng));

		PolylineOptions polylineOpt = new PolylineOptions();
		for (LatLng latlng : points) {
			polylineOpt.add(latlng);
		}

		polylineOpt.color(Color.RED);

		map.addPolyline(polylineOpt).setWidth(10);

	}

	@Override
	protected void onResume() {

		super.onResume();
	}

	private LocationListener locationListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			// move to center
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(location.getLatitude(), location.getLongitude()), ZOOM));
			if (!isPause) {
				Log.e("activity", "onLocationChanged");

				trackToMe(location.getLatitude(), location.getLongitude());

				DecimalFormat nf = new DecimalFormat("0.00");
				speedTv.setText(nf.format(location.getSpeed()));

				int index = points.size() - 1;
				if (points.size() == 1) {
					distance += geography.getDistance(points.get(0), points.get(0));
				} else {
					distance += geography.getDistance(points.get(index - 1), points.get(index));
				}
				distanceTv.setText(nf.format(distance));
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch (status) {
			case LocationProvider.OUT_OF_SERVICE:
				infoTv.setText("gps OUT_OF_SERVICE");
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				infoTv.setText("gps TEMPORARILY_UNAVAILABLE");
				break;
			case LocationProvider.AVAILABLE:
				infoTv.setText("gps AVAILABLE");
				break;
			}
		}
	};

	private GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				infoTv.setText("gps 啟動");
				isGpsOk = false;
				break;

			case GpsStatus.GPS_EVENT_STOPPED:
				infoTv.setText("gps 停止");
				isGpsOk = false;
				break;

			case GpsStatus.GPS_EVENT_FIRST_FIX:
				infoTv.setText("gps 定位成功");
				isGpsOk = true;
				break;

			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				infoTv.setText("gps 衛星狀態改變");
				isGpsOk = false;
				break;
			}
		}
	};
}
