package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.util.DisplayMetrics;

public class Environment {
	public static String VERSION   ="1.0";
	public static String COPYRIGHT ="&copy; 2015 Sebastian Kloska (<a href='http://www.oncaphillis.net/'>www.oncaphillis.net</a>; <a href='mailto:sebastian.kloska@snafu.de'>sebastian.kloska@snafu.de</a>)";

	public static boolean isDebug() {
		return true;
	} 
	
	public static int getColumns(Activity activity) {
        if( activity!=null) {
        	
    		DisplayMetrics displaymetrics = new DisplayMetrics();
    		activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    				
    		float width  = displaymetrics.widthPixels * 160.0f / displaymetrics.xdpi;
    				
    		if(width > 1000.0f)
    			return 4;
    		if(width > 800.0f)
    			return 3;
    		if(width > 400.0f)
    			return 2;
 		}
        return 1;
	}
}
