package com.example.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class RouteHelper implements IDbHelper<Route> {
	private RouteDb routeDb = null;


	public RouteHelper(Context context) {
		routeDb = RouteDb.getInstance(context);
	}
	
	public int getMaxGroup() {
		Cursor cursor = routeDb.getMaxGroup(); // 取得SQLite類別的回傳值:Cursor物件
		int max = 0;
		
		while (cursor != null && cursor.moveToNext()) {
			max = cursor.getInt(0);
		}
		return max;
	}

	@Override
	public boolean create(Route o) {
		return routeDb.create(contentValuesFactory(o));
	}

	@Override
	public List<Route> getAll() {
		Cursor cursor = routeDb.readAll(); // 取得SQLite類別的回傳值:Cursor物件
		List<Route> list = new ArrayList<Route>();
		
		if(cursor == null) {
			return list;
		}

		while (cursor.moveToNext()) {
			Route Route = new Route();
			Route.latitude = cursor.getDouble(1);
			Route.longitude = cursor.getDouble(2);
			Route.groupId = cursor.getInt(3);
			
			list.add(Route);
		}
		cursor.close(); // 關閉Cursor
		return list;
	}

	@Override
	public Route get(int id) {
		Cursor cursor = routeDb.read(id); // 取得SQLite類別的回傳值:Cursor物件
		Route Route = new Route();

		while (cursor != null && cursor.moveToNext()) {
			Route.latitude = cursor.getDouble(1);
			Route.longitude = cursor.getDouble(2);
			Route.groupId = cursor.getInt(3);
		}
		return Route;
	}

	@Override
	public boolean edit(Route o) {		
		return routeDb.update(o.id, contentValuesFactory(o));
	}

	@Override
	public boolean delete(int id) {
		return false;
	}

	@Override
	public ContentValues contentValuesFactory(Route Route) {
		ContentValues args = new ContentValues();
		args.put(routeDb.COLUMNS[1], Route.latitude);
		args.put(routeDb.COLUMNS[2], Route.longitude);
		args.put(routeDb.COLUMNS[3], Route.groupId);

		return args;
	}

}
