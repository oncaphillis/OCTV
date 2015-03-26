package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.DisplayMetrics;
import android.widget.ArrayAdapter;

public class Environment {
	
	protected static final int CAST    = 0;
	protected static final int CREW    = 1;
	protected static final int CREATOR = 2;
	protected static final int GUEST   = 3;
	
	public static String VERSION   ="1.0";
	public static String NAME ="";
	public static String COPYRIGHT ="&copy; 2015 Sebastian Kloska (<a href='http://www.oncaphillis.net/'>www.oncaphillis.net</a>; <a href='mailto:sebastian.kloska@snafu.de'>sebastian.kloska@snafu.de</a>)";

	static public String[] Titles = null;
	static public ArrayAdapter<TvSeries>[]      ListAdapters  = null;
	static public Map<Integer,List<TvSeries>>[] StoredResults = null;
	static public List<TvSeries>[] MainList = null;

	public static boolean isDebug() {
		return true;
	} 
	
	
	static DateFormat TmdbDateFormater   = new SimpleDateFormat("yyyy-MM-dd") {
		{
			this.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
	};

	static DateFormat TimeFormater   = new SimpleDateFormat("EEE, dd.MM.yyyy HH:mm") {
		{
			this.setTimeZone(TimeZone.getDefault());
		}
	};
	
	static DateFormat DateFormater   = new SimpleDateFormat("EEE, dd.MM.yyyy") {
		{
			this.setTimeZone(TimeZone.getDefault());
		}
	};
	
	public static int getColumns(Activity activity) {
        
		if( activity!=null) {
        	
    		DisplayMetrics displaymetrics = new DisplayMetrics();
    		activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    				
    		float width  = displaymetrics.widthPixels / displaymetrics.xdpi;
 
    		if(width > 7.0f)
    			return 3;
    		
    		if(width > 4.0f)
    			return 2;
        }
        return 1;
	}

	public static boolean useTrakt() {
		return true;
	}


	public static void init(Activity a) {
		
		try {
			VERSION = a.getPackageManager().getPackageInfo(a.getPackageName(), 0).versionName;
			NAME = a.getResources().getString(R.string.app_name);
			
		} catch (NameNotFoundException e) {
		}
		
		if(Titles==null) {
			String today    = a.getResources().getString(R.string.today);
			String on_air   = a.getResources().getString(R.string.on_the_air);
			String hi_voted = a.getResources().getString(R.string.hi_voted);
			String popular  = a.getResources().getString(R.string.popular);
			
			Titles = new String[] {today,on_air,hi_voted,popular };
		}
		
		if(ListAdapters==null)
			ListAdapters  = new ArrayAdapter[Titles.length];
		
		if(StoredResults==null)
			StoredResults = new HashMap[Titles.length];
		
		if(MainList==null)
			MainList = new List[Titles.length];
	}
}
