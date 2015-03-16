package net.oncaphillis.whatsontv;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.Activity;
import android.util.DisplayMetrics;

public class Environment {
	
	protected static final int CAST    = 0;
	protected static final int CREW    = 1;
	protected static final int CREATOR = 2;
	protected static final int GUEST   = 3;
	
	public static String VERSION   ="1.0";
	public static String COPYRIGHT ="&copy; 2015 Sebastian Kloska (<a href='http://www.oncaphillis.net/'>www.oncaphillis.net</a>; <a href='mailto:sebastian.kloska@snafu.de'>sebastian.kloska@snafu.de</a>)";

	public static boolean isDebug() {
		return true;
	} 
	
	
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
    				
    		float width  = displaymetrics.widthPixels * 160.0f / displaymetrics.xdpi;
 
    		if(width > 1200.0f)
    			return 4;
    		
    		if(width > 900.0f)
    			return 3;
    		
    		if(width > 600.0f)
    			return 2;
        }
        return 1;
	}
}
