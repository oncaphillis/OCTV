package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

public class SeriesInfoDownLoaderTask extends AsyncTask<String, Void, SeriesInfo> {
	private WeakReference<TextView> _networkText;
	private WeakReference<TextView> _timeText;
	private WeakReference<TextView> _lastEpisodeText;
	private WeakReference<TextView> _timeStateText;
	private WeakReference<TextView> _clockText;
	private Activity _activity;

	
	public SeriesInfoDownLoaderTask(TextView networkText, TextView timeText,TextView clockTime,TextView lastEpisodeText, TextView timeState,Activity activity) {
		_networkText = new WeakReference(networkText);
		_timeText    = new WeakReference(timeText);
		_clockText = new WeakReference(clockTime);
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
		if(si!=null && _timeText != null && _timeText.get() != null && _clockText.get()!=null && _timeText.get().getTag()!=null && _timeText.get().getTag() instanceof Integer) {
			refresh(_activity,si,_timeText.get(),_clockText.get(),_timeStateText.get(),_networkText.get(),_lastEpisodeText.get(),false);
		}
	}

	static public void refresh(Activity act,SeriesInfo si, TextView timeText, TextView clockText,TextView timeStateText,
			TextView networkText, TextView lastEpisodeText, boolean std) {
		
		boolean slim = Environment.isSlim(act);
		
		if(si.getNearestAiring()!=null) {
			
			DateFormat df = si.hasClock() ? Environment.TimeFormater : Environment.TmdbDateFormater;
			
			Date now = TimeTool.getNow();
			timeText.setText(df.format(si.getNearestAiring()) );					
			
			if(slim)
				if(si.hasClock()) {
					clockText.setText(Environment.ClockFormat.format(si.getNearestAiring()));					
					clockText.setVisibility(View.VISIBLE);
				} else {
					clockText.setText("...");
					clockText.setVisibility(View.GONE);
				}
			
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
					final TextView  _timeText  = timeText;
					final TextView  _clockText = clockText; 
					final boolean   _slim      = slim;
					
					Runnable r = new Runnable() {
						final Runnable   _me = this;
						
						@Override
						public void run() {
							
							DateFormat df = _si.hasClock() && ! _slim ? Environment.TimeFormater : Environment.TmdbDateFormater;
							
							final String t1 = df.format(_si.getNearestAiring());
							final String t2 = Environment.ClockFormat.format(_si.getNearestAiring());
									
							_act.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if( _timeText != null && _timeText.getTag() == _me) {
										_timeText.setText(Environment.isDebug() ? "@"+t1 : t1);
									}
									if(_slim)
										if(_si.hasClock()) {
											_clockText.setText(Environment.ClockFormat.format(_si.getNearestAiring()));					
											_clockText.setVisibility(View.VISIBLE);
										} else {
											_clockText.setText("...");
											_clockText.setVisibility(View.GONE);
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
