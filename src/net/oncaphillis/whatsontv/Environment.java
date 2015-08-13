package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.DisplayMetrics;
import android.widget.ArrayAdapter;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import info.movito.themoviedbapi.model.tv.TvSeries;

public class Environment {
	
	protected static final int CAST    = 0;
	protected static final int CREW    = 1;
	protected static final int CREATOR = 2;
	protected static final int GUEST   = 3;
	
	final static public int AIRING_TODAY = 0;
	final static public int ON_THE_AIR   = 1;
	final static public int HI_VOTED     = 2;
	final static public int POPULAR      = 3;

	final static int TTL = 60*60*24;
	
	public static String VERSION   ="1.0";
	public static String NAME ="";
	public static String COPYRIGHT ="&copy; 2015 Sebastian Kloska (<a href='http://www.oncaphillis.net/'>www.oncaphillis.net</a>; <a href='mailto:sebastian.kloska@snafu.de'>sebastian.kloska@snafu.de</a>)";
	public static long BUILD_DATE = 0;
	
	static public String[] Titles = null;
	static public ArrayAdapter<TvSeries>[]      ListAdapters  = null;
	static public Map<Integer,List<TvSeries>>[] StoredResults = null;
	static public List<TvSeries>[] MainList = null;
	static public SQLCacheHelper CacheHelper = null;
	
	static private String[] _articles = new String[] {
		"The"
	};
	
	static private Activity _theActivity = null;
	
	public static boolean isDebug() {
		return VERSION.substring(0,3).equals("pre");
	} 
	
	static DateFormat TmdbDateFormater = new SimpleDateFormat("yyyy-MM-dd") {
		DateFormat _outFormat = new SimpleDateFormat("EEE, dd.MM.yyyy"); 
		{
			this.setTimeZone(TimeZone.getTimeZone("EST"));
		}

		@Override
		public Date parse(String s) throws ParseException {
			Date d = super.parse(s);
			Calendar c = Calendar.getInstance();
			c.setTimeZone(TimeZone.getTimeZone("EST"));
			c.setTime(d);
			
			c.set(Calendar.HOUR_OF_DAY,23);
			c.set(Calendar.MINUTE,59);
			c.set(Calendar.SECOND,59);
			
			return c.getTime();
		}
		
		
		@Override
		public StringBuffer format(Date d, StringBuffer toAppendTo,
                FieldPosition fieldPosition) {
			Calendar c = Calendar.getInstance();
			c.setTimeZone(TimeZone.getTimeZone("EST"));
			c.setTime(d);
			
			c.set(c.HOUR_OF_DAY,0);
			c.set(c.MINUTE,0);
			c.set(c.SECOND,0);
			
			return _outFormat.format(c.getTime(),toAppendTo,fieldPosition);
		}
	};

	static String formatDate(Date d,boolean withTime) {
		DateFormat df = new SimpleDateFormat("EEE,");
		return df.format(d)+DayDateFormat.format(d)+(withTime ? " "+formatTime(d) : "");
	}

	public static CharSequence formatDate(Date d) {
		return formatDate(d,false);
	}

	static String formatTime(Date d) {
		return ClockFormat.format(d);
	}
	
	private static DateFormat ClockFormat = null;
	private static DateFormat DayDateFormat = null;
	
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


	public static void init(Activity a) throws Exception {


		try {
			VERSION = a.getPackageManager().getPackageInfo(a.getPackageName(), 0).versionName;
			NAME = a.getResources().getString(R.string.app_name);
			BUILD_DATE = a.getPackageManager().getPackageInfo(a.getPackageName(),0).lastUpdateTime;
			
			ClockFormat   = android.text.format.DateFormat.getTimeFormat(a);
			DayDateFormat = android.text.format.DateFormat.getLongDateFormat(a);
		} catch (NameNotFoundException e) {
		}
		
		if(Titles==null) {
			String today    = a.getResources().getString(R.string.today);
			String on_air   = a.getResources().getString(R.string.on_the_air);

			//String hi_voted = a.getResources().getString(R.string.hi_voted);
			//String popular  = a.getResources().getString(R.string.popular);
			
			Titles = new String[] {today,on_air/*,hi_voted,popular*/ };
		}
		
		if(CacheHelper == null) {
	         CacheHelper = new SQLCacheHelper(a);
			 long n = TimeTool.getNow().getTime() / 1000;
			 CacheHelper.getWritableDatabase().execSQL("DELETE FROM SERIES WHERE ? - TIMESTAMP > ?",new Object[] { n, TTL } ) ;
		}
		
		if(ListAdapters==null)
			ListAdapters  = new ArrayAdapter[Titles.length];
		
		if(StoredResults==null)
			StoredResults = new HashMap[Titles.length];
		
		if(MainList==null)
			MainList = new List[Titles.length];

        Tmdb.init();
	}

	public static Activity getCurrentActivity() {
		return _theActivity;
	}

	public static void setCurrentActivity(Activity a) {
		_theActivity = a;
	}

	public static float getColWidth(Activity activity) {
		if( activity!=null) {
        	
    		DisplayMetrics displaymetrics = new DisplayMetrics();
    		activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    				
    		float width  = displaymetrics.widthPixels / displaymetrics.xdpi;
    		return width / getColumns(activity);
        }
        return 0;
	}

	public static boolean isSlim(Activity activity) {
		return true;
		//return getColWidth(activity) < 2.0;
	}

	public static String[] getArticles() {
		return _articles;
	}

}
