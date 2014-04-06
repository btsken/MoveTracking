package com.example.running;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class Geography {

	private static final LatLng NKUT = new LatLng(23.979548, 120.696745);
	private LocationManager locationManager;
	private String provider = LocationManager.GPS_PROVIDER; // 最佳資訊提供者
	private LocationListener locationListener;
	private GpsStatus.Listener gpsListener;
	public LatLng location; // 最後位置
	public double distance;
	public double speed;

	public Geography(Context context, LocationListener locationListener,
			GpsStatus.Listener gpsListener) {
		this.locationListener = locationListener;
		this.gpsListener = gpsListener;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	public void startRecord() {
		// 取得上次已知的位置
		Location location = locationManager.getLastKnownLocation(provider);
		updateWithNewLocation(location);
		locationManager.addGpsStatusListener(gpsListener);

		// Location Listener
		int minTime = 2000;// ms
		int minDist = 2;// meter
		locationManager.requestLocationUpdates(provider, minTime, minDist, locationListener);
	}

	public double getDistance(LatLng gp1, LatLng gp2)
	{
		double earthRadius = 3958.75;
		double latDiff = Math.toRadians(gp2.latitude - gp1.latitude);
		double lngDiff = Math.toRadians(gp2.longitude - gp1.longitude);
		double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
				Math.cos(Math.toRadians(gp1.latitude)) *
				Math.cos(Math.toRadians(gp2.latitude)) *
				Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = earthRadius * c;
		int meterConversion = 1609;
		
		return distance * meterConversion;
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
		if (location != null) {
			this.location = new LatLng(location.getLatitude(), location.getLongitude());
		} else {
			this.location = NKUT;
		}
	}

	public String getTimeString(long timeInMilliseconds) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
		return format.format(timeInMilliseconds);
	}

	public PolylineOptions polylineOptionsFactory(List<LatLng> points, int color) {
		PolylineOptions polylineOpt = new PolylineOptions();
		for (LatLng latlng : points) {
			polylineOpt.add(latlng);
		}

		polylineOpt.color(color);
		return polylineOpt;
	}
}
