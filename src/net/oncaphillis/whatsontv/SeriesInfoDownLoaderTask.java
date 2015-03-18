package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Date;
import android.app.Activity;
import android.os.AsyncTask;
import android.widget.TextView;

public class SeriesInfoDownLoaderTask extends AsyncTask<String, Void, SeriesInfo> {
	private WeakReference<TextView> _networkText;
	private WeakReference<TextView> _timeText;
	private WeakReference<TextView> _lastEpisodeText;
	private WeakReference<TextView> _timeStateText;

	private Activity _activity;

	
	public SeriesInfoDownLoaderTask(TextView networkText, TextView timeText,TextView lastEpisodeText, TextView timeState,Activity activity) {
		_networkText = new WeakReference(networkText);
		_timeText    = new WeakReference(timeText);
		_lastEpisodeText = new WeakReference(lastEpisodeText);
		_timeStateText   = new WeakReference(timeState);
		
		_activity = activity;
	}

	@Override
	protected SeriesInfo doInBackground(String... params) {
		
		SeriesInfo si = null;
		if(_networkText != null && _networkText.get() != null && _networkText.get().getTag()!=null && _networkText.get().getTag() instanceof Integer) {
			TvSeries s = Tmdb.get().loadSeries((Integer)_networkText.get().getTag());
			si = new SeriesInfo(s);
		}
		return si;
	}	

	@Override
	protected void onPostExecute(SeriesInfo si) {
		
		// dates reported by Tmdb are in EST. We need this to compare
		// the last aired
		// Calendar today1 = TimeTool.toTimeZone(TimeTool.getToday(),"EST");

		if(si!=null) {
			if(_timeText != null && _timeText.get() != null && _timeText.get().getTag()!=null && _timeText.get().getTag() instanceof Integer) {
				if(si.getNearestAiring()!=null) {
					
					DateFormat df = si.hasClock() ? Environment.TimeFormater : Environment.DateFormater;
					
					Date td = TimeTool.getToday();
					_timeText.get().setText(df.format(si.getNearestAiring()) );					
					
					if(si.getNearestAiring().before(td)) {
						_timeText.get().setTextColor(_activity.getResources().getColor(R.color.actionbar_text_color));
						if(_timeStateText.get()!=null)
							_timeStateText.get().setText("last");
					} else {
						if(_timeStateText.get()!=null)
							_timeStateText.get().setText("next");
						_timeText.get().setTextColor(_activity.getResources().getColor(R.color.oncaphillis_orange));
					}
				}
			}

			if(_networkText != null && _networkText.get() != null && _networkText.get().getTag()!=null && _networkText.get().getTag() instanceof Integer) {
				_networkText.get().setText(si.getNetworks());
			}

			if(_lastEpisodeText != null && _lastEpisodeText.get() != null && _lastEpisodeText.get().getTag()!=null && _lastEpisodeText.get().getTag() instanceof Integer) {
				String s = Integer.toString(si.getNearestEpisodeSeason())+"x"+Integer.toString(si.getNearestEpisodeNumber());
				
				_lastEpisodeText.get().setText(s+" "+si.getNearestEpisodeTitle());
			}
		}
	}
}
