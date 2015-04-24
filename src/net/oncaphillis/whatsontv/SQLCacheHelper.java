package net.oncaphillis.whatsontv;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLCacheHelper extends SQLiteOpenHelper {

    private static final int    DATABASE_VERSION = 1;
    private static final String SERIES_TABLE_NAME = "SERIES";
    private static final String DATABASE_NAME = Environment.NAME;
    		
    private static final String SERIES_TABLE_CREATE =
                "CREATE TABLE " + SERIES_TABLE_NAME + 
                " (ID INTEGER PRIMARY KEY ASC,TIMESTAMP INTEGER NOT NULL,DATA BLOB NOT NULL);";

    SQLCacheHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(SERIES_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
