package com.example.running;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.example.db.Route;
import com.example.db.RouteHelper;
import com.google.android.gms.maps.model.LatLng;

public class MessengerService extends Service {

	private final IBinder mBinder = new LocalBinder();
	public Geography geography; // 位置偵測
	private RouteHelper routeHelper;
	public boolean isPause; // 是否開始記錄
	public int groupId;
	public List<LatLng> points;
	public double distance;
	public double speed;

	public class LocalBinder extends Binder {
		MessengerService getService() {
			return MessengerService.this;
		}
	}
	
	public void recordLocation() {
		if (geography.isGpsOpen()) {
			isPause = false;
			geography.startRecord();
		} else {
			Intent dialogIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(dialogIntent);
		}
	}

	public void stopRecordLocation() {
		isPause = true;
//		geography.stopRecord();
	}
	
	public void saveRecord() {
		for(LatLng location : points) {
			routeHelper.create(new Route(location.latitude, location.longitude, groupId));
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		Log.e("onCreate", "MusicService onCreate()");
		geography = new Geography(getApplicationContext(), locationListener, gpsListener);
		isPause = true;
		routeHelper = new RouteHelper(this.getApplicationContext());
		groupId = routeHelper.getMaxGroup();
		points = new ArrayList<LatLng>();
	}
	
	private LocationListener locationListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			if(!isPause) {
//				Log.e("緯度", String.valueOf(location.getLatitude()));
//				Log.e("經度", String.valueOf(location.getLongitude()));
				
				speed = location.getSpeed();
//				Log.e("速度", speed + "");
				points.add(new LatLng(location.getLatitude(), location.getLongitude()));
				int index = points.size() - 1;
				if(index == 0) {
					distance += geography.getDistance(points.get(index), points.get(index));
				} else {
					distance += geography.getDistance(points.get(index - 1), points.get(index));
				}
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
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				break;
			case LocationProvider.AVAILABLE:
				break;
			}
		}
	};

	
	private GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				break;

			case GpsStatus.GPS_EVENT_STOPPED:
				break;

			case GpsStatus.GPS_EVENT_FIRST_FIX:
				break;

			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				break;
			}
		}
	};
}
