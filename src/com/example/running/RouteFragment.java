package com.example.running;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.db.Route;
import com.example.db.RouteHelper;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

public class RouteFragment extends Fragment {

	private MapView mMapView;
	private GoogleMap map;
	private Button switchBtn, saveBtn;
	private TextView infoTv, speedTv, distanceTv;

	private Geography geography;
	private RouteHelper routeHelper;
	
	private boolean isPause; // 是否開始記錄
	private static final int ZOOM = 18;
	private double distance;
	private Timer timer;
	private int sec;
	private int groupId;
	private List<LatLng> points;

	public static RouteFragment newInstance() {
		return new RouteFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		switchBtn = (Button) rootView.findViewById(R.id.button1);
		saveBtn = (Button) rootView.findViewById(R.id.button2);
		infoTv = (TextView) rootView.findViewById(R.id.info);
		speedTv = (TextView) rootView.findViewById(R.id.speed);
		distanceTv = (TextView) rootView.findViewById(R.id.distance);

		mMapView = (MapView) rootView.findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);
		mMapView.onResume();// needed to get the map to display immediately

		try {
			MapsInitializer.initialize(getActivity());
		} catch (GooglePlayServicesNotAvailableException e) {
			e.printStackTrace();
		}

		map = mMapView.getMap();
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setViews();
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
			getActivity().getApplication().startActivity(dialogIntent);
		}
	}

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

	private void setViews() {
		map.setMyLocationEnabled(true);
	}

	private void init() {
		geography = new Geography(getActivity(), locationListener, gpsListener);
		isPause = true;
		routeHelper = new RouteHelper(getActivity());
		groupId = routeHelper.getMaxGroup();
		points = new ArrayList<LatLng>();
		geography.startRecord();
		timer = new Timer();
		// 設定Timer(task為執行內容，0代表立刻開始,間格1秒執行一次)
		timer.schedule(task, 0, 1000);
	}

	private void trackToMe(double lat, double lng) {

		points.add(new LatLng(lat, lng));

		// PolylineOptions polylineOpt = new PolylineOptions();
		// for (LatLng latlng : points) {
		// polylineOpt.add(latlng);
		// }
		//
		// polylineOpt.color(Color.RED);

		map.addPolyline(geography.polylineOptionsFactory(points, Color.RED)).setWidth(10);
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
				// infoTv.setText("gps OUT_OF_SERVICE");
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				// infoTv.setText("gps TEMPORARILY_UNAVAILABLE");
				break;
			case LocationProvider.AVAILABLE:
				// infoTv.setText("gps AVAILABLE");
				break;
			}
		}
	};

	private GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				// infoTv.setText("gps 啟動");
				break;

			case GpsStatus.GPS_EVENT_STOPPED:
				// infoTv.setText("gps 停止");
				break;

			case GpsStatus.GPS_EVENT_FIRST_FIX:
				// infoTv.setText("gps 定位成功");
				break;

			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				// infoTv.setText("gps 衛星狀態改變");
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		timer.cancel();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}
}
