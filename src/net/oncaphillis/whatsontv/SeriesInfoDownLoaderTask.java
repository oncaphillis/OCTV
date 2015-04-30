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
		SeriesInfo si;
	}

	@Override
	protected SeriesInfo doInBackground(String... params) {
		
		SeriesInfo si = null;
		if(_networkText != null && _networkText.get() != null && _networkText.get().getTag()!=null && _networkText.get().getTag() instanceof Integer) {
			TvSeries s = Tmdb.get().loadSeries((Integer)_networkText.get().getTag());
			si = SeriesInfo.fromSeries(s);
		}
		return si;
	}	

	@Override
	protected void onPostExecute(SeriesInfo si) {
		
		// dates reported by Tmdb are in EST. We need this to compare
		// the last aired
		if(si!=null && _timeText != null && _timeText.get() != null && _timeText.get().getTag()!=null && _timeText.get().getTag() instanceof Integer) {
			refresh(_activity,si,_timeText.get(),_timeStateText.get(),_networkText.get(),_lastEpisodeText.get(),false);
		}
	}

	static public void refresh(Activity act,SeriesInfo si, TextView timeText, TextView timeStateText,
			TextView networkText, TextView lastEpisodeText, boolean std) {
		if(si.getNearestAiring()!=null) {
			
			DateFormat df = si.hasClock() ? Environment.TimeFormater : Environment.TmdbDateFormater;
			
			Date now = TimeTool.getNow();
			timeText.setText(df.format(si.getNearestAiring()) );					
			
			if(si.getNearestAiring().before(now)) {
				timeText.setTextColor(act.getResources().getColor(R.color.actionbar_text_color));
				if(timeStateText!=null)
					timeStateText.setText("last");
			} else {
				if(timeStateText !=null)
					timeStateText.setText("next");

				if(!si.hasClock()) {

					final SeriesInfo _si = si;
					final Activity  _act = act;
					final TextView  _timeText = timeText;
					
					Runnable r = new Runnable() {
						final Runnable   _me = this;
						
						@Override
						public void run() {
							
							DateFormat df = _si.hasClock() ? Environment.TimeFormater : Environment.TmdbDateFormater;
							
							final String t = df.format(_si.getNearestAiring());
									
							_act.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if( _timeText != null && _timeText.getTag() != null && _timeText.getTag() == _me) {
										_timeText.setText(Environment.isDebug() ? "@"+t : t);
									}
								}
							});
						}
					};
					timeText.setTag(r);
					Tmdb.get().trakt_reader().register(r);
				}
				timeText.setTextColor(act.getResources().getColor(R.color.oncaphillis_orange));
			}
		}

		if( networkText != null && networkText.getTag()!=null && networkText.getTag() instanceof Integer) {
			 networkText.setText(si.getNetworks());
		}

		if( lastEpisodeText != null &&  lastEpisodeText.getTag()!=null && lastEpisodeText.getTag() instanceof Integer) {
			String s = Integer.toString(si.getNearestEpisodeSeason())+"x"+Integer.toString(si.getNearestEpisodeNumber());
			lastEpisodeText.setText(s+" "+si.getNearestEpisodeTitle());
		}
	}
}
