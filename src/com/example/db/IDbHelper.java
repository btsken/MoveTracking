package com.example.db;

import java.util.List;

import android.content.ContentValues;


public interface IDbHelper<T> {	
	public boolean create(T o);
	public List<T> getAll();
	public T get(int id);
	public boolean edit(T o);
	public boolean delete(int id);
	public ContentValues contentValuesFactory(T o);
}
