package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.Network;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;

class SeriesInfo {
	private TvSeries _tvs;
	private String _nws = new String("");
	private Calendar _lastAired = null;
	private String _lastAiredStr = null;
	
	public SeriesInfo(TvSeries s) {
		_tvs = s;
		if(_tvs.getNetworks()!=null) {
			for(Network nw : _tvs.getNetworks() )  {
				_nws += (_nws.isEmpty() ? "" : " ") + nw.getName();
			}
		}
		if(Environment.isDebug()) 
			_nws += " ["+s.getLastAirDate()+"] ";
		
		if(s.getLastAirDate()!=null) {
			Calendar c = Calendar.getInstance();
			
			DateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

			try {
				c.setTime(f.parse(s.getLastAirDate()));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			TimeZone fromTimeZone = TimeZone.getDefault();
	        TimeZone toTimeZone   = TimeZone.getTimeZone("EST");

	        c.setTimeZone(fromTimeZone);
	        c.add(Calendar.MILLISECOND, fromTimeZone.getRawOffset() * -1);

	        if (fromTimeZone.inDaylightTime(c.getTime())) {
	            c.add(Calendar.MILLISECOND, c.getTimeZone().getDSTSavings() * -1);
	        }

	        c.add(Calendar.MILLISECOND, toTimeZone.getRawOffset());
	        
	        if (toTimeZone.inDaylightTime(c.getTime())) {
	            c.add(Calendar.MILLISECOND, toTimeZone.getDSTSavings());
	        }
	        
	        DateFormat formater = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.SHORT,Locale.getDefault());
	        
	        _lastAired    = c;
	        _lastAiredStr = formater.format(c.getTime());
		}
	}

	Calendar getLastAired() {
		return _lastAired;
	}

	String getLastAiredStr() {
		return _lastAiredStr;
	}
	
	String getNetworks() {
		return _nws;
	}
};

public class SeriesInfoDownLoaderTask extends AsyncTask<String, Void, SeriesInfo> {
	private WeakReference<TextView> _textView;
	private Activity _activity;
	private DateFormat _formater = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.SHORT,Locale.getDefault());
	
	public SeriesInfoDownLoaderTask(TextView tt1, Activity activity) {
		_textView = new WeakReference(tt1);
		_activity = activity;
	}

	@Override
	protected SeriesInfo doInBackground(String... params) {
		SeriesInfo si = null;
		if(_textView != null && _textView.get() != null && _textView.get().getTag()!=null && _textView.get().getTag() instanceof Integer) {
			TvSeries s = Tmdb.get().loadSeries((Integer)_textView.get().getTag());
			si = new SeriesInfo(s);			
		}
		return si;
	}	

	private Calendar toTimeZone(Calendar ci,String tz) {
		
		TimeZone fromTimeZone = TimeZone.getDefault();
        TimeZone toTimeZone   = TimeZone.getTimeZone("EST");
        Calendar c = (Calendar) ci.clone();
        c.setTimeZone(fromTimeZone);
        c.add(Calendar.MILLISECOND, fromTimeZone.getRawOffset() * -1);

        if (fromTimeZone.inDaylightTime(c.getTime())) {
            c.add(Calendar.MILLISECOND, c.getTimeZone().getDSTSavings() * -1);
        }

        c.add(Calendar.MILLISECOND, toTimeZone.getRawOffset());
        
        if (toTimeZone.inDaylightTime(c.getTime())) {
            c.add(Calendar.MILLISECOND, toTimeZone.getDSTSavings());
        }
        
        return c;
	}
	
	private Calendar getToday() {
		Calendar nw = Calendar.getInstance();
		nw.set(Calendar.HOUR, 0);
		nw.set(Calendar.MINUTE, 0);
		nw.set(Calendar.SECOND, 0);
		nw.set(Calendar.MILLISECOND, 0);
		return nw;
	}
	
	@Override
	protected void onPostExecute(SeriesInfo si) {
		
		
		if(si!=null && _textView != null && _textView.get() != null && _textView.get().getTag()!=null && _textView.get().getTag() instanceof Integer) {
			_textView.get().setText(si.getNetworks()+" "+(si.getLastAiredStr()== null ? "" : si.getLastAiredStr())+"::"+
					_formater.format(toTimeZone(getToday(),"EST").getTime()));

			if(si.getLastAired()==null || si.getLastAired().before(toTimeZone(getToday(),"EST"))) {
				_textView.get().setTextColor(_activity.getResources().getColor(R.color.actionbar_text_color));
			} else {
				_textView.get().setTextColor(_activity.getResources().getColor(R.color.oncaphillis_orange));
			}
		}
	}
}
