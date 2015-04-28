package net.oncaphillis.whatsontv;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeTool {
	private static DateFormat _toFormater   = new SimpleDateFormat("EEE, dd.MM.yyyy hh:mm");
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
	
	public static Date getNow() {
		return Calendar.getInstance().getTime();
	}

	public static Date toDate(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY,   0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date getToday() {
		Calendar c = Calendar.getInstance();
		c.setTime(getNow());
		c.set(Calendar.HOUR_OF_DAY,   0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return toDate(getNow());
	}
}
