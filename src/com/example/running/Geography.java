package com.example.running;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Geography {

	private static final LatLng NKUT = new LatLng(23.979548, 120.696745);
	private static final String TAG = "=== Map Demo ==>";
	private LocationManager locationManager;
	private String provider = LocationManager.GPS_PROVIDER; // 最佳資訊提供者
//	private Context context;
	private LocationListener locationListener;
	private GpsStatus.Listener gpsListener;
	public LatLng location; // 最後位置
	public double speed;

	public Geography(Context context, LocationListener locationListener, 
			GpsStatus.Listener gpsListener) {
//		this.context = context;
		this.locationListener = locationListener;
		this.gpsListener = gpsListener;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	public void whereAmI() {
		// 取得上次已知的位置
		if (isGpsOpen()) {
			Location location = locationManager.getLastKnownLocation(provider);
			updateWithNewLocation(location);

			// GPS Listener
			locationManager.addGpsStatusListener(gpsListener);

			// Location Listener
			int minTime = 5000;// ms
			int minDist = 5;// meter
			locationManager.requestLocationUpdates(provider, minTime, minDist, locationListener);
		} else {
			updateWithNewLocation(null);
		}

	}

	public void stopRecord() {
		locationManager.removeGpsStatusListener(gpsListener);
	}

	public boolean isGpsOpen() {

		// 2.選擇使用GPS提供器
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
			return true;
		}

		return false;
	}

	public void updateWithNewLocation(Location location) {
		String where = "";
		if (location != null) {
			this.location = new LatLng(location.getLatitude(), location.getLongitude());
			this.speed = location.getSpeed();
			long time = location.getTime();
			String timeString = getTimeString(time);

			where = "經度: " + this.location.longitude +
					"\n緯度: " + this.location.latitude +
					"\n速度: " + speed +
					"\n時間: " + timeString +
					"\nProvider: " + provider;

		} else {
			where = "No location found.";
			this.location = NKUT;
		}
//		Log.e(TAG, where);
	}

	public String getTimeString(long timeInMilliseconds) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
		return format.format(timeInMilliseconds);
	}

}
