package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.Network;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;

import net.oncaphillis.whatsontv.Tmdb.EpisodeInfo;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;

class SeriesInfo {
	private TvSeries _tvs;
	private String _nws = new String("");
	private Date    _nearestAiring  = null;
	private String _nearestEpisodeTitle = new String("");
	private boolean _hasClock = false;


	public SeriesInfo(TvSeries s) {
		_tvs = s;
		if(_tvs.getNetworks()!=null) {
			for(Network nw : _tvs.getNetworks() )  {
				_nws += (_nws.isEmpty() ? "" : " ") + nw.getName();
			}
		}

		_nearestAiring    = new Date();
		
		if(s.getLastAirDate()!=null) {
			
			try {
				_nearestAiring = Tmdb.DateFormater.parse( s.getLastAirDate());
			} catch (ParseException e) {
			}

			// All TMDB dates are EST.
	        // We check if the Last Aired Field is >= Today
	        // I this case we search for the smallest airing date >= today
	        
	        Date td = TimeTool.getToday();
	        
	        String sxx=new String();
	        boolean found = false;
	        if(false && ! td.after( _nearestAiring )  ) {
	        	
	        	if(s.getSeasons()!=null) {
	        		ListIterator<TvSeason> season_iterator = s.getSeasons().listIterator(s.getSeasons().size());
	        		TvEpisode episode = null;
	        		
	        		while( ! found && season_iterator.hasPrevious() && (episode==null || td.before(getAirDate(episode))) ) {
		        		TvSeason  season = Tmdb.get().loadSeason(s.getId(), season_iterator.previous().getSeasonNumber());
		        		if(season.getEpisodes()!=null) {

		        			ListIterator<TvEpisode> episode_iterator = season.getEpisodes().listIterator(season.getEpisodes().size());

		        			TvEpisode le = null;
		        			
		        			while(episode_iterator.hasPrevious()) {
		        				episode = episode_iterator.previous();
		        				
		        				if(episode.getAirDate()!=null && le!=null && getAirDate(episode).before(td)) {
			        				EpisodeInfo ei = Tmdb.get().loadEpisode(s.getId(), season.getSeasonNumber(), le.getEpisodeNumber());
			        				
			        				if(ei.getAirTime() != null) {
			        					// Too large difference between Trakt.
			        					_nearestAiring = ei.getAirTime();
			        					_hasClock = true;
			        				} else {
			        					_nearestAiring = ei.getAirDate();
			        				}
			        				_nearestEpisodeTitle = ei.getTmdb().getName();
			        				found = true;
			        				break;
			        			}
			        			le = episode;
			        		}
		        		}
		        	}
	        	}
	        } else {
	        	s = Tmdb.get().loadSeries(s.getId());
	        	if(s.getSeasons()!=null && s.getSeasons().size()>0) {
	        		TvSeason ts = Tmdb.get().loadSeason(s.getId(),s.getSeasons().get(s.getSeasons().size()-1).getSeasonNumber());
	        		if(ts.getEpisodes()!=null && ts.getEpisodes().size()>0) {
	        			TvEpisode eps = ts.getEpisodes().get(ts.getEpisodes().size()-1);
	        			_nearestEpisodeTitle = eps.getName();
	        			_nearestAiring = Tmdb.getAirDate(eps);
	        		}
	        	}
	        }
		}
	}
	
	Date getAirDate(TvEpisode e) {
		if(e.getAirDate()!=null) {
			int delta = TimeZone.getDefault().getRawOffset() - TimeZone.getTimeZone("EST").getRawOffset();
		
			try {
				Date date =  Tmdb.DateFormater.parse(e.getAirDate());
				date.setTime(date.getTime()+delta);
				return date;
			} catch (ParseException e1) {
			}
		}
		
		return null;
	}
	
	Date getNearestAiring() {
		return _nearestAiring;
	}

	String getNetworks() {
		return _nws;
	}

	public String getNearestEpisodeTitle() {
		return _nearestEpisodeTitle;
	}

	public boolean hasClock() {
		return _hasClock ;
	}
};

public class SeriesInfoDownLoaderTask extends AsyncTask<String, Void, SeriesInfo> {
	private WeakReference<TextView> _networkText;
	private WeakReference<TextView> _timeText;
	private WeakReference<TextView> _lastEpisodeText;
	private WeakReference<TextView> _timeStateText;

	private Activity _activity;
	
	private static DateFormat _timeFormater   = new SimpleDateFormat("EEE, dd.MM.yyyy HH:mm") {
		{
			this.setTimeZone(TimeZone.getDefault());
		}
	};
	
	private static DateFormat _dateFormater   = new SimpleDateFormat("EEE, dd.MM.yyyy") {
		{
			this.setTimeZone(TimeZone.getDefault());
		}
	};
	
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
					
					DateFormat df = si.hasClock() ? _timeFormater : _dateFormater;
					
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
				_lastEpisodeText.get().setText(si.getNearestEpisodeTitle());
			}
		}
	}
}
