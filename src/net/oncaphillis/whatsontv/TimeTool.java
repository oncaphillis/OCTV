package net.oncaphillis.whatsontv;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeTool {
	private static DateFormat _toFormater   = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.SHORT,Locale.getDefault());
	private static DateFormat _fromFormater = new SimpleDateFormat("yyyy-MM-dd");

	public static Calendar toTimeZone(Calendar ci,String tz) {

		TimeZone toTimeZone = TimeZone.getTimeZone(tz);
		Calendar c = (Calendar) ci.clone();
		
		String s = c.getTimeZone().getID();
		
		
        c.add(Calendar.MILLISECOND, toTimeZone.getRawOffset());
        
        if (toTimeZone.inDaylightTime(c.getTime())) {
            c.add(Calendar.MILLISECOND, +toTimeZone.getDSTSavings() * -1);
        }
        
        return c;
	}
	
	public static Calendar getNow() {
		Calendar nw = Calendar.getInstance();
		return nw;
	}

	public static Calendar getToday() {
		Calendar nw = getNow();
		nw.set(Calendar.HOUR_OF_DAY, 0);
		nw.set(Calendar.MINUTE, 0);
		nw.set(Calendar.SECOND, 0);
		nw.set(Calendar.MILLISECOND, 0);
		return nw;
	}
	
	public static Calendar fromString(String str) {
		Calendar c = Calendar.getInstance();
		Date d = null;
		try {
			d = _fromFormater.parse(str);
		} catch (ParseException e) {
		}
		c.setTime(d);
		return c;
	}
	
	public static String toString(Calendar cw) {
		Calendar c = (Calendar)cw.clone();
		c.add(Calendar.MILLISECOND, cw.getTimeZone().getRawOffset());
		if(c.getTimeZone().inDaylightTime(c.getTime())) {
			c.add(Calendar.MILLISECOND, c.getTimeZone().getDSTSavings());
		}
		return _toFormater.format(c.getTime());
	}
}
