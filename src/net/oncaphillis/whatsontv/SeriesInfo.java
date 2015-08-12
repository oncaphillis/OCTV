package net.oncaphillis.whatsontv;

import net.oncaphillis.whatsontv.Tmdb.EpisodeInfo;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import info.movito.themoviedbapi.model.tv.Network;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

public class SeriesInfo {
	private TvSeries _tvs;
	private String   _networks       = new String("");
	private Date     _nearestAiring  = null;
	private Date     _firstAiring    = null;
	
	private String  _nearestEpisodeTitle = new String("");
	private boolean _hasClock = false;
	private int     _nearestEpisodeSeason = 0;
	private int     _nearestEpisodeNumber = 0;
	private List<SeasonNode> _seasonsEpisodeList = null;
	private EpisodeInfo _nearestEpisodeInfo;
	private static int MAX_CACHE = 100;

	public class NearestNode {
		public  String title;
		public  String networks; 
		public  Date   date;
		boolean hasClock;
		int     season;
		int     episode;
	};

	public NearestNode getNearestNode() {
		NearestNode n = new NearestNode();
		n.title          = _nearestEpisodeTitle;
		n.networks       = _networks;
		n.date           = _nearestAiring;
		n.hasClock       = _hasClock;
		n.season  = _nearestEpisodeSeason;;
		n.episode = _nearestEpisodeNumber;
		return n;
	}

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

	
	private static class CacheNode {
		public long timestamp;
		public int id;
		public CacheNode(int i) {
			timestamp = TimeTool.getNow().getTime();
			id = i;
		}
	};

	static
	private TtlCache<Integer,SeriesInfo> _infoMap = new TtlCache<Integer,SeriesInfo>(Environment.TTL,100);
	
	private static long _hit=0;

	static SeriesInfo fromSeries(TvSeries s) {
		if(s == null)
			return null;
		
		SeriesInfo si = _infoMap.get(s.getId());
		
		if(si !=null) {
			return si;
		}
		
		si  = new SeriesInfo(s);
		
		_infoMap.put(s.getId(),si);
		
		return si;
	}
	
	public static long getCacheHits() {
		return _infoMap.getHits();
	}
	
	public static long getCacheSize() {
		return _infoMap.getSize();
	}
	private SeriesInfo(TvSeries tvs) {
		_tvs = Tmdb.loadSeries(tvs.getId());

		if(_tvs.getNetworks()!=null) {
			for(Network nw : _tvs.getNetworks() )  {
				_networks += (_networks.isEmpty() ? "" : " ") + nw.getName();
			}
		}

		if(tvs.getFirstAirDate()!=null) {
			try {
				_firstAiring = Environment.TmdbDateFormater.parse(tvs.getFirstAirDate());
			} catch (ParseException e1) {
			}
		}
		
        // If there is a last airing date given for the series
        // we and iterate backwards thru seasons and episodes 
		// to find the nearest airing  date. (next or last ever).

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
	        			season = Tmdb.loadSeason(tvs.getId(), season.getSeasonNumber());

	        			if(season != null  && season.getEpisodes()!=null) {

		        			ListIterator<TvEpisode> episode_iterator = season.getEpisodes().listIterator(season.getEpisodes().size());

		        			while(!found && episode_iterator.hasPrevious()) {
		        				
		        				episode = episode_iterator.previous();
		        		        
		        				if(episode.getAirDate()!=null && le!=null && getAirDate(episode).before(td) ) {
			        				
		        					EpisodeInfo ei = Tmdb.loadEpisode(tvs.getId(), ls.getSeasonNumber(), le.getEpisodeNumber());
			        				
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
    					EpisodeInfo ei = Tmdb.loadEpisode(tvs.getId(), season.getSeasonNumber(), episode.getEpisodeNumber());
        				
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
	        	tvs = Tmdb.loadSeries(tvs.getId());
	        	if(tvs.getSeasons()!=null && tvs.getSeasons().size()>0) {

	        		TvSeason ts = null;
	        		int n = 1;
	        		
	        		while( tvs.getSeasons().size()-n >= 0 ) {
	        			ts = Tmdb.loadSeason(tvs.getId(),tvs.getSeasons().get(tvs.getSeasons().size()-n).getSeasonNumber());
	        			if(ts!=null && ts.getEpisodes().size()>0)
	        				break;
	        			n++;
	        		}
	        		
	        		if(ts.getEpisodes()!=null && ts.getEpisodes().size()>0) {
	        			TvEpisode eps = ts.getEpisodes().get(ts.getEpisodes().size()-1);
        				_nearestEpisodeSeason = ts.getSeasonNumber();
        				_nearestEpisodeNumber = eps.getEpisodeNumber();
	        			_nearestEpisodeTitle  = eps.getName();
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
		return Tmdb.loadSeason(_tvs.getId(), _tvs.getSeasons().get(n).getSeasonNumber());
	}
	
	public List<TvSeason> getSeasons() {
		List<TvSeason> l = new ArrayList<TvSeason>();
		if(_tvs!=null || _tvs.getSeasons()!=null) {
			for(TvSeason s : _tvs.getSeasons()) {
				l.add(Tmdb.loadSeason(_tvs.getId(), s.getSeasonNumber()));
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
				s = Tmdb.loadSeason(_tvs.getId(), s.getSeasonNumber());
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
	public String getNetworks() {
		return _networks;
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
		if( _hasClock )
			return true;
		getNearestAiring();
		
		return _hasClock;
	}

	public Date getNearestAiring() {
		if(_nearestAiring == null)
			return null;
		
		Date td = TimeTool.getToday();
		
		if(td.after(_nearestAiring) || _hasClock)
			return _nearestAiring;
		
		EpisodeInfo ei = Tmdb.loadEpisode(getTmdb().getId(), this._nearestEpisodeSeason,this._nearestEpisodeNumber);

		if(ei != null && ei.getAirTime() != null) {
			_nearestAiring = ei.getAirTime();
			_hasClock      = true;
		}
		
		return _nearestAiring;
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
			_nearestEpisodeInfo = Tmdb.loadEpisode(this._tvs.getId(), this.getNearestEpisodeSeason(), this.getNearestEpisodeNumber());
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

	public int getId() {
		return _tvs == null ? 0 : _tvs.getId();
	}
	
	
}