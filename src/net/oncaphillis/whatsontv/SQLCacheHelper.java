package net.oncaphillis.whatsontv;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLCacheHelper extends SQLiteOpenHelper {

    private static final int    DATABASE_VERSION = 2;
    private static final String SERIES_TABLE_NAME = "SERIES";
    private static final String SEASON_TABLE_NAME = "SEASON";
    private static final String DATABASE_NAME = Environment.NAME;
    		
    private static final String SERIES_TABLE_CREATE =
            "CREATE TABLE " + SERIES_TABLE_NAME + 
            " (ID INTEGER PRIMARY KEY ASC,TIMESTAMP INTEGER NOT NULL,DATA BLOB NOT NULL);";

    private static final String SEASON_TABLE_CREATE =
            "CREATE TABLE SEASON "+
            " (SERIES    INT  NOT NULL,"+
            "  ID        INT  NOT NULL,"+
            "  TIMESTAMP INT  NOT NULL,"+
            "  DATA      BLOB NOT NULL,"+
            "  PRIMARY KEY(SERIES,ID) ,"+
            "  FOREIGN KEY(SERIES) REFERENCES SERIES(ID) ON DELETE CASCADE);";

    SQLCacheHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(SERIES_TABLE_CREATE);
        db.execSQL(SEASON_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(newVersion>1)
			db.execSQL(SEASON_TABLE_CREATE);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
	    super.onOpen(db);
	    if (!db.isReadOnly()) {
	        db.execSQL("PRAGMA foreign_keys=ON;");
	    }
	}
}
