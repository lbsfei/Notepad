package com.feige.notepad;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotesDB extends SQLiteOpenHelper{
	
	public static final String TABLE_NAME = "notes";
	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String ID = "_id";
	public static final String TIME = "time";
	public static final String IMAGE_PATHS = "imagepaths";
	public static final String VIDEO_PATHS ="videopaths";
	

	public NotesDB(Context context) {
		super(context, "notes", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE "+TABLE_NAME+ "("
				+ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+IMAGE_PATHS+" TEXT,"
				+VIDEO_PATHS+" TEXT ,"
				+TITLE+" TEXT ,"
				+CONTENT+" TEXT ,"
				+TIME+" TEXT NOT NULL)"
				);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	
	
	
	
	
	
	
	
	
	
	

}
