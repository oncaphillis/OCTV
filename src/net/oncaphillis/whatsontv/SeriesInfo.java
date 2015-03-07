package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.Network;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import net.oncaphillis.whatsontv.SeriesInfo.SeasonNode;
import net.oncaphillis.whatsontv.Tmdb.EpisodeInfo;

public class SeriesInfo {
	private TvSeries _tvs;
	private String _nws = new String("");
	private Date    _nearestAiring  = null;
	private String _nearestEpisodeTitle = new String("");
	private boolean _hasClock = false;
	private int _nearestEpisodeSeason = 0;
	private int _nearestEpisodeNumber = 0;

	public static class SeasonNode implements Serializable {
		int _season;
		public SeasonNode(int season) {
			_season = season;
		}
		public int getSeason() {
			return _season;
		}
	}
	
	public static class EpisodeNode extends SeasonNode implements Serializable {
		int _episode;
		public EpisodeNode(int season,int episode) {
			super(season);
			_episode = episode; 
		}
		public int getEpisode() {
			return _episode;
		}
	}
	
	public SeriesInfo(TvSeries s) {
		_tvs = Tmdb.get().loadSeries(s.getId());

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
	        if( ! td.after( _nearestAiring )  ) {
	        	
	        	if(s.getSeasons()!=null) {
	        		ListIterator<TvSeason> season_iterator = s.getSeasons().listIterator(s.getSeasons().size());
	        		TvEpisode episode = null;
	        		TvSeason season = null;
	        		while( ! found && season_iterator.hasPrevious() && (episode==null || td.before(getAirDate(episode))) ) {
		        		season = season_iterator.previous();
	        			season = Tmdb.get().loadSeason(s.getId(), season.getSeasonNumber());
		        		if(season.getEpisodes()!=null) {

		        			ListIterator<TvEpisode> episode_iterator = season.getEpisodes().listIterator(season.getEpisodes().size());

		        			TvEpisode le = null;
		        			
		        			while(episode_iterator.hasPrevious()) {
		        				episode = episode_iterator.previous();
		        				
		        				if(episode.getAirDate()!=null && le!=null && getAirDate(episode).before(td) ) {
			        				
		        					EpisodeInfo ei = Tmdb.get().loadEpisode(s.getId(), season.getSeasonNumber(), le.getEpisodeNumber());
			        				
			        				if(ei.getAirTime() != null) {
			        					// Too large difference between Trakt.
			        					_nearestAiring = ei.getAirTime();
			        					_hasClock = true;
			        				} else {
			        					_nearestAiring = ei.getAirDate();
			        				}
			        				_nearestEpisodeSeason = season.getSeasonNumber();
			        				_nearestEpisodeNumber = ei.getTmdb().getEpisodeNumber();
			        				_nearestEpisodeTitle = ei.getTmdb().getName();
			        				found = true;
			        				break;
			        			}
			        			le = episode;
			        		}
		        		}
		        	}
	        		
	        		if(!found && season!=null && episode!=null && episode.getAirDate()!=null) {
    					EpisodeInfo ei = Tmdb.get().loadEpisode(s.getId(), season.getSeasonNumber(), episode.getEpisodeNumber());
        				
        				if(ei.getAirTime() != null) {
        					// Too large difference between Trakt.
        					_nearestAiring = ei.getAirTime();
        					_hasClock = true;
        				} else {
        					_nearestAiring = ei.getAirDate();
        				}
        				_nearestEpisodeSeason = season.getSeasonNumber();
        				_nearestEpisodeNumber = ei.getTmdb().getEpisodeNumber();
        				_nearestEpisodeTitle  = ei.getTmdb().getName();
        			}
	        	}
	        } else {
	        	s = Tmdb.get().loadSeries(s.getId());
	        	if(s.getSeasons()!=null && s.getSeasons().size()>0) {
	        		
	        		
	        		TvSeason ts = null;
	        		int n = 1;
	        		
	        		while( s.getSeasons().size()-n >= 0 ) {
	        			ts = Tmdb.get().loadSeason(s.getId(),s.getSeasons().get(s.getSeasons().size()-n).getSeasonNumber());
	        			if(ts!=null && ts.getEpisodes().size()>0)
	        				break;
	        			n++;
	        		}
	        		
	        		if(ts.getEpisodes()!=null && ts.getEpisodes().size()>0) {
	        			TvEpisode eps = ts.getEpisodes().get(ts.getEpisodes().size()-1);
        				_nearestEpisodeSeason = ts.getSeasonNumber();
        				_nearestEpisodeNumber = eps.getEpisodeNumber();
	        			_nearestEpisodeTitle = eps.getName();
	        			_nearestAiring = Tmdb.getAirDate(eps);
	        		}
	        	}
	        }
		}
	}
	
	public TvSeason getSeason(int n) {
		if(_tvs == null || _tvs.getSeasons() == null || _tvs.getSeasons().size()>=n)
			return null;
		return Tmdb.get().loadSeason(_tvs.getId(), _tvs.getSeasons().get(n).getSeasonNumber());
	}
	
	public List<TvSeason> getSeasons() {
		List<TvSeason> l = new ArrayList<TvSeason>();
		if(_tvs!=null || _tvs.getSeasons()!=null) {
			for(TvSeason s : _tvs.getSeasons()) {
				l.add(Tmdb.get().loadSeason(_tvs.getId(), s.getSeasonNumber()));
			}
		}
		return l;
	}

	public List<? extends SeasonNode> getSeasonsEpisodeList() {
		
		List<SeasonNode> l = new ArrayList();
		
		if(_tvs == null || _tvs.getSeasons() == null || _tvs.getSeasons().isEmpty() )
			return l;

		for(TvSeason s : _tvs.getSeasons()) {
			s = Tmdb.get().loadSeason(_tvs.getId(), s.getSeasonNumber());
			l.add(new SeasonNode(s.getSeasonNumber()));
			if(s.getEpisodes()!=null && !s.getEpisodes().isEmpty()) {
				for(TvEpisode e : s.getEpisodes()) {
					l.add(new EpisodeNode(s.getSeasonNumber(),e.getEpisodeNumber()));
				}
			}
		}
		return l;
	}
	
	public Date getAirDate(TvEpisode e) {
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
	public Date getNearestAiring() {
		return _nearestAiring;
	}
	public String getNetworks() {
		return _nws;
	}
	public String getNearestEpisodeTitle() {
		return _nearestEpisodeTitle;
	}
	public int getNearestEpisodeSeason() {
		return _nearestEpisodeSeason;
	}
	public int getNearestEpisodeNumber() {
		return _nearestEpisodeNumber;
	}
	public boolean hasClock() {
		return _hasClock ;
	}
}