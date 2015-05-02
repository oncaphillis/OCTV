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

	private static TtlCache<Integer,SeriesInfo.NearestNode> _nearestCache = new TtlCache<Integer,SeriesInfo.NearestNode>(Environment.TTL,500);
	
	public static SeriesInfo.NearestNode getNearest(int id) {
		synchronized(_nearestCache) {
			return _nearestCache.get(id);
		}
	}

	public static void putNearest(int id,SeriesInfo.NearestNode nn) {
		synchronized(_nearestCache) {
			_nearestCache.put(id,nn);
		}
	}
	
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
			SeriesInfo.NearestNode nn = si.getNearestNode();
			refresh(_activity,nn,_timeText.get(),_clockText.get(),_timeStateText.get(),_networkText.get(),_lastEpisodeText.get(),false);
			Date now = TimeTool.getNow();
			
			if( !si.getNearestAiring().before(now) && !si.hasClock() ) {

				final SeriesInfo _si    = si;
				final Activity  _act    = _activity;
				final TextView  _tText  = _timeText.get();
				final TextView  _cText  = _clockText.get(); 
				final boolean   _slim   = Environment.isSlim(_act);
				
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
								if( _tText != null && _tText.getTag() == _me) {
									_tText.setText(Environment.isDebug() ? "@"+t1 : t1);
								}
								if(_slim)
									if(_si.hasClock()) {
										_cText.setText(Environment.ClockFormat.format(_si.getNearestAiring()));					
										_cText.setVisibility(View.VISIBLE);
									} else {
										_cText.setText("...");
										_cText.setVisibility(View.GONE);
									}
							}
						});
					}
				};
				_timeText.get().setTag(r);
				Tmdb.get().trakt_reader().register(r);
			} else {
				putNearest(si.getId(),nn);
			}
		}
	}

	static public void refresh(Activity act,SeriesInfo.NearestNode nn, TextView timeText, TextView clockText,TextView timeStateText,
			TextView networkText, TextView lastEpisodeText, boolean std) {
		
		boolean slim = Environment.isSlim(act);
		
		if(nn.date != null) {
			
			DateFormat df = nn.hasClock ? Environment.TimeFormater : Environment.TmdbDateFormater;
			
			Date now = TimeTool.getNow();
			timeText.setText(df.format(nn.date) );					
			
			if(slim)
				if(nn.hasClock ) {
					clockText.setText(Environment.ClockFormat.format(nn.date));					
					clockText.setVisibility(View.VISIBLE);
				} else {
					clockText.setText("...");
					clockText.setVisibility(View.GONE);
				}
			
			if(nn.date.before(now)) {
				timeText.setTextColor(act.getResources().getColor(R.color.actionbar_text_color));
				if(timeStateText!=null)
					timeStateText.setText("last");
			} else {
				if(timeStateText !=null)
					timeStateText.setText("next");
				timeText.setTextColor(act.getResources().getColor(R.color.oncaphillis_orange));
			}
		}

		if( networkText != null && networkText.getTag()!=null && networkText.getTag() instanceof Integer) {
			 networkText.setText(nn.networks );
		}

		if( lastEpisodeText != null &&  lastEpisodeText.getTag()!=null && lastEpisodeText.getTag() instanceof Integer) {
			String s = Integer.toString(nn.season )+"x"+Integer.toString(nn.episode);
			lastEpisodeText.setText((std ? "@" : "" )+s+" "+nn.title);
		}
	}
}
