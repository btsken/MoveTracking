package com.example.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RouteDb extends SQLiteOpenHelper {
	private static volatile RouteDb routeDb = null;
	private final String TABLE_NAME = "route";
	private SQLiteDatabase db;

	private final static String DB_NAME = "routedb";
	private final static int DATABASE_VERSION = 1;
	public final String[] COLUMNS = { "_ID", 
									"latitude", 
									"longitude", 
									"groupId"};

	private RouteDb(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		db = this.getWritableDatabase();
	}

	public static RouteDb getInstance(Context context) {
		if (routeDb == null) {
			synchronized (RouteDb.class) {
				if (routeDb == null) {
					routeDb = new RouteDb(context);
				}
			}
		}
		return routeDb;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String DATABASE_CREATE_TABLE = "create table IF NOT EXISTS "
				+ TABLE_NAME + " ("
				+ COLUMNS[0] + " INTEGER PRIMARY KEY, "
				+ COLUMNS[1] + " REAL, "
				+ COLUMNS[2] + " REAL, "
				+ COLUMNS[3] + " INTEGER"
				+ ");";
		Log.d("DATABASE_CREATE_TABLE", DATABASE_CREATE_TABLE);
		db.execSQL(DATABASE_CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS config"); // 刪除舊有的資料表
		onCreate(db);
	}

	public boolean create(ContentValues args) {
		return db.insert(TABLE_NAME, null, args) != -1;
	}
	
	public Cursor getMaxGroup() {
		return db.rawQuery("SELECT MAX("+COLUMNS[3]+") FROM " + TABLE_NAME, null);
	}

	public Cursor readAll() {
		return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
	}

	public Cursor read(int id) {
		Cursor cursor = db.query(true, TABLE_NAME, // 資料表名稱
				COLUMNS, // 欄位名稱
				COLUMNS[1] + "=" + id, // WHERE
				null, // WHERE 的參數
				null, // GROUP BY
				null, // HAVING
				null, // ORDOR BY
				null // 限制回傳的rows數量
				);

		// 注意：不寫會出錯
		if (cursor != null) {
			cursor.moveToFirst(); // 將指標移到第一筆資料
		}
		return cursor;
	}

	public boolean update(int id, ContentValues args) {
		return db.update(TABLE_NAME, // 資料表名稱
				args, // VALUE
				COLUMNS[1] + "=" + id, // WHERE
				null // WHERE的參數
				) == 1;
	}

	public boolean delete(int id) {
		return db.delete(TABLE_NAME, // 資料表名稱
				COLUMNS[1] + "=" + id, // WHERE
				null // WHERE的參數
				) == 1;
	}
}
