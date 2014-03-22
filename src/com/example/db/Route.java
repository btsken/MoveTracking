package com.example.db;

public class Route {
	
	public Route() {
		
	}

	public Route(double latitude, double longitude, int groupId) {
		this.groupId = groupId;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public int id;
	public double latitude;
	public double longitude;
	public int groupId;

}
