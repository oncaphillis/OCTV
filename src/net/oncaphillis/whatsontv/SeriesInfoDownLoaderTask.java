package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.Network;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

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
			
			/* Calendar c = ;
			*/
			nws = " ("+Calendar.getInstance().getTimeZone().getDisplayName()+")";
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
