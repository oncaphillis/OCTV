package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

public class SeriesInfoDownLoader extends AsyncTask<String, Void, SeriesInfo> {

	private WeakReference<View> _view;

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
	
	public SeriesInfoDownLoader(int id,View v) {
		_view = new WeakReference<View>(v);
		v.setTag(new Integer(id));
	}

	@Override
	protected SeriesInfo doInBackground(String... params) {

		SeriesInfo si = null;
		if(_view != null && _view.get() != null && _view.get().getTag() != null && _view.get().getTag() instanceof Integer) {
			TvSeries s = Tmdb.get().loadSeries((Integer)_view.get().getTag());
			si = SeriesInfo.fromSeries(s);
		}
		return si;
	}	

	@Override
	protected void onPostExecute(SeriesInfo si) {
		
		// dates reported by Tmdb are in EST. We need this to compare
		// the last aired
		if(si!=null && _view!=null && _view.get()!=null && _view.get().getTag() != null 
				&& _view.get().getTag() instanceof Integer && ((Integer)_view.get().getTag()).equals(si.getId())) {
			SeriesInfo.NearestNode nn = si.getNearestNode();
			TvSeriesListAdapter.refresh(_view.get(),nn,true);
			Date now = TimeTool.getNow();
			
			if( si.getNearestAiring()!=null && !si.getNearestAiring().before(now) && !si.hasClock() ) {
				/*final SeriesInfo _si    = si;
				Runnable r = new Runnable() {
					final Runnable   _me = this;
					
					@Override
					public void run() {
						_act.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if( _tText != null && _tText.getTag() == _me) {
									_tText.setText( (Environment.isDebug() ? "@" : "") + Environment.formatDate(_si.getNearestAiring(),_si.hasClock() && ! _slim));
								}
								if(_slim)
									if(_si.hasClock()) {
										_cText.setText(Environment.formatTime(_si.getNearestAiring()));					
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
				*/
			} else {
				putNearest(si.getId(),nn);
			}
		}
	}
}
