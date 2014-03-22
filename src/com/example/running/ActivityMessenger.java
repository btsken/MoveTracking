package com.example.running;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.running.MessengerService.LocalBinder;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class ActivityMessenger extends Activity {

	private static final String TAG = "=== Map Demo ==>";
	private MessengerService mService = null;
	boolean mBound = false;;
	private Button switchBtn;
	private Button saveBtn;
	private TextView infoTv;
	private Geography geography;
	private GoogleMap map;
	private boolean isGpsOk = false;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;

			geography.whereAmI();

			Log.e("point size", String.valueOf(mService.points.size()));

			PolylineOptions polylineOptions = new PolylineOptions();

			polylineOptions.color(Color.RED);
			polylineOptions.width(5);
			polylineOptions.addAll(mService.points);
			map.addPolyline(polylineOptions);
		}

		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	public void play() {
		if (mBound) {
			mService.recordLocation();
		}
	}

	public void pause() {
		if (mBound) {
			mService.stopRecordLocation();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.e("activity", "onCreate");
		init();
		findViews();

		switchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchAction();
			}
		});

		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchAction();
				mService.saveRecord();
				mService.groupId++;
			}
		});
	}

	private void init() {
		bindService(new Intent(this, MessengerService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		geography = new Geography(this, locationListener, gpsListener);
	}

	private void switchAction() {
		if (mService.isPause) {
			if (mService.geography.isGpsOpen()) {
				play();
				switchBtn.setText(R.string.stop);
				saveBtn.setEnabled(true);
			} else {
				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		} else {
			pause();
			saveBtn.setEnabled(false);
			switchBtn.setText(R.string.start);
		}
	}

	private void findViews() {
		switchBtn = (Button) findViewById(R.id.button1);
		saveBtn = (Button) findViewById(R.id.button2);
		infoTv = (TextView) findViewById(R.id.info);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);
	}

	@Override
	protected void onDestroy() {
		Log.e("activity", "onDestroy");
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		super.onDestroy();
	}

	@Override
	public void finish() {
		moveTaskToBack(true); // move back
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mBound) {
			Log.e("point size", String.valueOf(mService.points.size()));
		}
	}

	private LocationListener locationListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(location.getLatitude(), location.getLongitude()), 16));
			
			Log.e("point size", String.valueOf(mService.points.size()));

			PolylineOptions polylineOptions = new PolylineOptions();

			polylineOptions.color(Color.RED);
			polylineOptions.width(5);
			polylineOptions.addAll(mService.points);
			map.addPolyline(polylineOptions);
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
