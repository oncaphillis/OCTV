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

import net.oncaphillis.whatsontv.Tmdb.EpisodeInfo;

public class SeriesInfo {
	private TvSeries _tvs;
	private String _nws = new String("");
	private Date    _nearestAiring  = null;
	private Date    _firstAiring  = null;
	private String _nearestEpisodeTitle = new String("");
	private boolean _hasClock = false;
	private int _nearestEpisodeSeason = 0;
	private int _nearestEpisodeNumber = 0;
	private List<SeasonNode> _seasonsEpisodeList = null;
	private EpisodeInfo _nearestEpisodeInfo;

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
	
	static SeriesInfo fromSeries(TvSeries s) {
		if(s == null)
			return null;
		return new SeriesInfo(s);
	}
	private SeriesInfo(TvSeries tvs) {
		_tvs = Tmdb.get().loadSeries(tvs.getId());

		if(_tvs.getNetworks()!=null) {
			for(Network nw : _tvs.getNetworks() )  {
				_nws += (_nws.isEmpty() ? "" : " ") + nw.getName();
			}
		}

		if(tvs.getFirstAirDate()!=null) {
			try {
				_firstAiring = Environment.TmdbDateFormater.parse(tvs.getFirstAirDate());
			} catch (ParseException e1) {
			}
		}
		
		if(tvs.getLastAirDate()!=null) {
			
			try {
				_nearestAiring = Environment.TmdbDateFormater.parse( tvs.getLastAirDate());
			} catch (ParseException e) {
			}

			// All TMDB dates are EST.
	        // We check if the Last Aired Field is >= Today
	        // I this case we search for the smallest airing date >= today
	        
	        Date td = TimeTool.getToday();
	        
	        boolean found = false;

	        if( ! td.after( _nearestAiring )  ) {
	        	
	        	if(tvs.getSeasons()!=null) {
	        		ListIterator<TvSeason> season_iterator = tvs.getSeasons().listIterator(tvs.getSeasons().size());
	        		TvEpisode episode = null;
	        		TvSeason season = null;

	        		TvEpisode le = null; // hit in last iteration
	        		TvSeason ls = null;  // hit in last iteration
	        		
	        		while( ! found && season_iterator.hasPrevious() && (episode==null || td.before(getAirDate(episode))) ) {
	        			season = season_iterator.previous();
	        			season = Tmdb.get().loadSeason(tvs.getId(), season.getSeasonNumber());

	        			if(season.getEpisodes()!=null) {

		        			ListIterator<TvEpisode> episode_iterator = season.getEpisodes().listIterator(season.getEpisodes().size());

		        			while(!found && episode_iterator.hasPrevious()) {
		        				
		        				episode = episode_iterator.previous();
		        		        
		        				if(episode.getAirDate()!=null && le!=null && getAirDate(episode).before(td) ) {
			        				
		        					EpisodeInfo ei = Tmdb.get().loadEpisode(tvs.getId(), ls.getSeasonNumber(), le.getEpisodeNumber());
			        				
			        				if(ei!=null && ei.getAirTime() != null) {
			        					_nearestAiring = ei.getAirTime();
			        					_hasClock = true;
			        				} else {
			        					_nearestAiring = ei.getAirDate();
			        				}
			        				
			        				_nearestEpisodeSeason = ls.getSeasonNumber();
			        				_nearestEpisodeNumber = ei.getTmdb().getEpisodeNumber();
			        				_nearestEpisodeTitle  = ei.getTmdb().getName();
			        				found = true;
			        				break;
			        			}
			        			le = episode;
			        			ls = season;
			        		}
		        		}
		        	}
	        		
	        		if(!found && season!=null && episode!=null && episode.getAirDate()!=null) {
    					EpisodeInfo ei = Tmdb.get().loadEpisode(tvs.getId(), season.getSeasonNumber(), episode.getEpisodeNumber());
        				
        				if(ei.getAirTime() != null) {
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
	        	tvs = Tmdb.get().loadSeries(tvs.getId());
	        	if(tvs.getSeasons()!=null && tvs.getSeasons().size()>0) {
	        		
	        		
	        		TvSeason ts = null;
	        		int n = 1;
	        		
	        		while( tvs.getSeasons().size()-n >= 0 ) {
	        			ts = Tmdb.get().loadSeason(tvs.getId(),tvs.getSeasons().get(tvs.getSeasons().size()-n).getSeasonNumber());
	        			if(ts!=null && ts.getEpisodes().size()>0)
	        				break;
	        			n++;
	        		}
	        		
	        		if(ts.getEpisodes()!=null && ts.getEpisodes().size()>0) {
	        			TvEpisode eps = ts.getEpisodes().get(ts.getEpisodes().size()-1);
        				_nearestEpisodeSeason = ts.getSeasonNumber();
        				_nearestEpisodeNumber = eps.getEpisodeNumber();
	        			_nearestEpisodeTitle = eps.getName();
	        			Date na = Tmdb.getAirDate(eps);
	        			if(na != null) {
	        				_nearestAiring = na;
	        			}
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
		if(_seasonsEpisodeList == null) {
			_seasonsEpisodeList  = new ArrayList<SeasonNode>();
			
			if(_tvs == null || _tvs.getSeasons() == null || _tvs.getSeasons().isEmpty() )
				return _seasonsEpisodeList;
	
			for(TvSeason s : _tvs.getSeasons()) {
				s = Tmdb.get().loadSeason(_tvs.getId(), s.getSeasonNumber());
				_seasonsEpisodeList.add(new SeasonNode(s.getSeasonNumber()));
				if(s.getEpisodes()!=null && !s.getEpisodes().isEmpty()) {
					for(TvEpisode e : s.getEpisodes()) {
						_seasonsEpisodeList.add(new EpisodeNode(s.getSeasonNumber(),e.getEpisodeNumber()));
					}
				}
			}
		}
		return _seasonsEpisodeList;
	}
	
	public Date getAirDate(TvEpisode e) {
		if(e.getAirDate()!=null) {
			int delta = TimeZone.getDefault().getRawOffset() - TimeZone.getTimeZone("EST").getRawOffset();
		
			try {
				Date date =  Environment.TmdbDateFormater.parse(e.getAirDate());
				date.setTime(date.getTime()+delta);
				return date;
			} catch (ParseException e1) {
			}
		}
		
		return null;
	}
	public Date getFirstAiring() {
		return _firstAiring;
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
	public int getNearestEpisodeCoordinate() {
		int n = 0;
		for(SeasonNode s : getSeasonsEpisodeList()) {
			if(s instanceof EpisodeNode && ((EpisodeNode)s).getSeason()==getNearestEpisodeSeason() && ((EpisodeNode)s).getEpisode() == getNearestEpisodeNumber() ) {
				return n;
			}
			n++;
		}
		return 0;
	}

	public EpisodeInfo getNearestEpisodeInfo() {
		if(_nearestEpisodeInfo == null) {
			_nearestEpisodeInfo = Tmdb.get().loadEpisode(this._tvs.getId(), this.getNearestEpisodeSeason(), this.getNearestEpisodeNumber());
		}
		return _nearestEpisodeInfo;
	}
	
	public TvEpisode getNearestEpisode() {
		if(getNearestEpisodeInfo()!=null)
			return getNearestEpisodeInfo().getTmdb();
		return null;
	}

	public TvSeries getTmdb() {
		return _tvs;
	}
}