package net.oncaphillis.whatsontv;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLCacheHelper extends SQLiteOpenHelper {

    private static final int    DATABASE_VERSION  = 5;
    private static final String SERIES_TABLE_NAME = "SERIES";
    private static final String SEASON_TABLE_NAME = "SEASON";
    private static final String EPISODE_TABLE_NAME = "EPISODE";
    private static final String DATABASE_NAME = Environment.NAME;
    		
    private static final String SERIES_TABLE_CREATE =
            "CREATE TABLE " + SERIES_TABLE_NAME + 
            " (ID INTEGER PRIMARY KEY ASC,TIMESTAMP INTEGER NOT NULL,DATA BLOB NOT NULL);";

    private static final String SEASON_TABLE_CREATE =
            "CREATE TABLE " + SEASON_TABLE_NAME +
            " (SERIES    INT  NOT NULL,"+
            "  ID        INT  NOT NULL,"+
            "  TIMESTAMP INT  NOT NULL,"+
            "  DATA      BLOB NOT NULL,"+
            "  PRIMARY KEY(SERIES,ID) ,"+
            "  FOREIGN KEY(SERIES) REFERENCES SERIES(ID) ON DELETE CASCADE);";

    private static final String EPISODE_TABLE_CREATE =
            "CREATE TABLE " + EPISODE_TABLE_NAME +
            " (SERIES    INT  NOT NULL,"+
            "  SEASON    INT  NOT NULL,"+
            "  ID        INT  NOT NULL,"+
            "  TIMESTAMP INT  NOT NULL,"+
            "  DATA      BLOB NOT NULL,"+
            "  PRIMARY KEY(SERIES,SEASON,ID) ,"+
            "  FOREIGN KEY(SERIES,SEASON) REFERENCES SEASON(SERIES,ID) ON DELETE CASCADE);";

    private static final String SERIES_INDEX_CREATE =
            "CREATE INDEX " + SERIES_TABLE_NAME + "_IDX1 " + 
            " ON " + SERIES_TABLE_NAME+"(TIMESTAMP);";

    private static final String SEASON_INDEX_CREATE =
            "CREATE INDEX " + SEASON_TABLE_NAME + "_IDX1 " + 
            " ON " + SEASON_TABLE_NAME+"(TIMESTAMP);";

    private static final String EPISODE_INDEX_CREATE =
            "CREATE INDEX " + EPISODE_TABLE_NAME + "_IDX1 " + 
            " ON " + SEASON_TABLE_NAME+"(TIMESTAMP);";

    SQLCacheHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(SERIES_TABLE_CREATE);
        db.execSQL(SEASON_TABLE_CREATE);
        db.execSQL(EPISODE_TABLE_CREATE);

        db.execSQL(SERIES_INDEX_CREATE);
        db.execSQL(SEASON_INDEX_CREATE);
        db.execSQL(EPISODE_INDEX_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		if(newVersion<=1)
			db.execSQL(SEASON_TABLE_CREATE);
		
		if(oldVersion<=2) {
			db.execSQL(SERIES_INDEX_CREATE);
			db.execSQL(SEASON_INDEX_CREATE);
		}
		if(oldVersion<=3) {
			db.execSQL(EPISODE_TABLE_CREATE);
			db.execSQL(EPISODE_INDEX_CREATE);
		}		
		if(oldVersion<=4) {
			db.execSQL("drop index "+EPISODE_TABLE_NAME+"_IDX1");
			db.execSQL("drop table "+EPISODE_TABLE_NAME);
			db.execSQL(EPISODE_TABLE_CREATE);
			db.execSQL(EPISODE_INDEX_CREATE);
		}		
		return;
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
	    super.onOpen(db);
	    if (!db.isReadOnly()) {
	        db.execSQL("PRAGMA foreign_keys=ON;");
	    }
	}
}
