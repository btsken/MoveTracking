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
		db.execSQL("DROP TABLE IF EXISTS config"); // �R���¦�����ƪ�
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
		Cursor cursor = db.query(true, TABLE_NAME, // ��ƪ�W��
				COLUMNS, // ���W��
				COLUMNS[1] + "=" + id, // WHERE
				null, // WHERE ���Ѽ�
				null, // GROUP BY
				null, // HAVING
				null, // ORDOR BY
				null // ����^�Ǫ�rows�ƶq
				);

		// �`�N�G���g�|�X��
		if (cursor != null) {
			cursor.moveToFirst(); // �N���в���Ĥ@�����
		}
		return cursor;
	}

	public boolean update(int id, ContentValues args) {
		return db.update(TABLE_NAME, // ��ƪ�W��
				args, // VALUE
				COLUMNS[1] + "=" + id, // WHERE
				null // WHERE���Ѽ�
				) == 1;
	}

	public boolean delete(int id) {
		return db.delete(TABLE_NAME, // ��ƪ�W��
				COLUMNS[1] + "=" + id, // WHERE
				null // WHERE���Ѽ�
				) == 1;
	}
}
