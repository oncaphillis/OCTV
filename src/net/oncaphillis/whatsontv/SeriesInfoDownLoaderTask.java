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

public class SeriesInfoDownLoaderTask extends AsyncTask<String, Void, String> {
	private WeakReference<TextView> _textView;
	public SeriesInfoDownLoaderTask(TextView tt1, Activity _activity,
			ProgressBar pb, Bitmap _defBitmap, Object object) {
		_textView = new WeakReference(tt1);
	}

	@Override
	protected String doInBackground(String... params) {
		String nws = "";
		
		if(_textView != null && _textView.get() != null && _textView.get().getTag()!=null && _textView.get().getTag() instanceof Integer) {
			TvSeries s = Tmdb.get().loadSeries((Integer)_textView.get().getTag());
			for(Network nw : s.getNetworks() )  {
				nws += (nws.isEmpty() ? "" : " ") + nw.getName();
			}
			
			if(s.getLastAirDate()!=null) {
				nws += " ["+s.getLastAirDate()+"] ";
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
		        DateFormat formater = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.MEDIUM,Locale.getDefault());
		        
		        nws += " (" + formater.format(c.getTime()) +")";
			} else {
				nws += " (???)";
			}
		}
		return nws;
	}	

	@Override
	// Once the image is downloaded, associates it to the imageView
	protected void onPostExecute(String nws) {
		if(_textView != null && _textView.get() != null && _textView.get().getTag()!=null && _textView.get().getTag() instanceof Integer) {
			_textView.get().setText(nws);
		}
	}
}
