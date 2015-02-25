package net.oncaphillis.whatsontv;

import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;

import org.joda.time.DateTime;

import com.uwetrottmann.trakt.v2.TraktV2;
import com.uwetrottmann.trakt.v2.entities.Episode;
import com.uwetrottmann.trakt.v2.entities.SearchResult;
import com.uwetrottmann.trakt.v2.enums.Extended;
import com.uwetrottmann.trakt.v2.enums.IdType;

import net.oncaphillis.whatsontv.Tmdb.SeasonKey;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ProgressBar;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbTV.TvMethod;
import info.movito.themoviedbapi.TmdbTvEpisodes.EpisodeMethod;
import info.movito.themoviedbapi.TmdbTvSeasons.SeasonMethod;
import info.movito.themoviedbapi.model.config.Timezone;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

abstract class CacheMap<K,V> {
	
	private int _max = 5000;
	
	class Node {
		Node(V s) {
			value = s;
		}
		public int count;
		public V value;  
	}

	private Hashtable<K,Node> _map = new Hashtable<K,Node>();	
	
	CacheMap(int max) {
		_max = max;
	}
	
	public void put(K id,V value) {
		if(_map.get(id)!=null) {
			_map.get(id).value = value;
		} else {
			_map.put(id,new Node(value));
		}
	}
	
	public V get(K key) {
		Node n;
		
		if((n = _map.get(key)) == null) {
			V v = load(key);
			if(v!=null) {
				_map.put(key,n=new Node(v));
			}
		}
		
		if(n!=null) {
			n.count++;
			return n.value;
		}
		
		return null;
	}
	
	public int size() {
		return _map.size();
	}
	abstract V load(K key);
};

/**
 * A cash for Bitmap objects we retrieve from the Web. It holds 
 * references until a threshold value is reached. After that
 * Bitmaps get deleted. 
 * 
 * @author kloska
 *
 */

class BitmapHash {
	
	private class Node {
		public int count = 0;
		public Bitmap bm = null;
		public Node(int c,Bitmap b) {
			count = c;
			bm = b;
		}
	};
	
	private HashMap<Integer,HashMap<String,Node>> _bmHash =
			new HashMap<Integer,HashMap<String,Node>>();
	
	private class NodeComparator implements Comparator<Node> {
		@Override
		public int compare(Node lhs, Node rhs) {
			return lhs.count < rhs.count ? -1 : lhs.count > rhs.count ? 1 : 
				lhs.bm.getByteCount() < rhs.bm.getByteCount() ? -1 : lhs.bm.getByteCount() > rhs.bm.getByteCount() ? 1 : 0; 
		}
	};
	
	private NodeComparator comp = new NodeComparator();
	
	private PriorityQueue<Node> _queue = new PriorityQueue<Node>(10,comp);
	
	private int _size = 0;
	
	static final int MAX_SIZE=5000000; 
	
	void put(Bitmap bm,int size,String path) {

		
		HashMap<String,Node> hm0 = null;
		
		if((hm0 = _bmHash.get(size))==null) {
			_bmHash.put(size,hm0 = new HashMap<String,Node>());
		}
		
		while( (!_queue.isEmpty()) && (_size+bm.getByteCount() > MAX_SIZE) ) {
			Node n = _queue.remove();
			Iterator<Integer> a = _bmHash.keySet().iterator();

			while(a!=null && a.hasNext()) {
				int ai=a.next();
				Iterator<String> b = _bmHash.get(ai).keySet().iterator();
				while(b.hasNext()) {
					String bs = b.next();
					if(_bmHash.get(ai).get(bs) == n ) {
						_size -= _bmHash.get(ai).get(bs).bm.getByteCount();
						_bmHash.get(ai).remove(bs);
						a = null;
						break;
					}
				}
			}
		}
		
		Node n = null;
		
		hm0.put(path, n=new Node(0,bm));
		_queue.add(n);
		
		_size += bm.getByteCount();
	}
	
	Bitmap get(int size,String path) {
		
		HashMap<String,Node> hm = _bmHash.get(size);

		Node nd = null;
		
		if(hm != null) {
			if((nd=hm.get(path))!=null) {
				nd.count++;
				return nd.bm;
			}
		}
		return null;
	}

	public int size() {
		Iterator<Integer> i = _bmHash.keySet().iterator();
		int s = 0;
		while(i.hasNext()) {
			s+=_bmHash.get(i.next()).size();
		}
		return s;
	}
}

public class Tmdb {
	private static Tmdb      _one     = null;
	private static String    _key     = null;
	private TmdbApi          _api     = null;
	private TraktV2          _trakt   = null;
	private BitmapHash       _hash    = new BitmapHash();
	private List<Timezone> _timezones = null;
	
	public class EpisodeInfo {
		private TvEpisode _tmdb_episode;
		private Episode _trakt_episode;
		EpisodeInfo (TvEpisode tmdb,Episode trakt)  {
			_tmdb_episode = tmdb;
			_trakt_episode = trakt;
			
		}
		public TvEpisode getTmdb()  {
			return _tmdb_episode;
		}
		
		public Episode getTrakt()  {
			if(_trakt_episode==null) {
				List<SearchResult> l = Tmdb.get().trakt().search().idLookup(IdType.TMDB,Integer.toString(getTmdb().getId()), 1, null);
				for(SearchResult r : l) {
					if(r.type.equals("episode") ) {
						Episode eps = trakt().episodes().summary(Integer.toString(r.show.ids.trakt),r.episode.season, r.episode.number, Extended.FULL);
						if(eps.first_aired!=null) {
							_trakt_episode=eps;
						}
					}
				}
			}
			return _trakt_episode;
		}
		
		public Calendar getAirTime() {
			if(getTrakt()!=null && getTrakt().first_aired!=null)
				return getTrakt().first_aired.toCalendar(Locale.getDefault());
			return null;
		}
	}
	class SeasonKey {
		SeasonKey(int series, int season) {
			this.series = series;
			this.season = season;
		}
		
		@Override
		public int hashCode() {
			return series << 8 | season;
		}
		
		@Override
		public boolean equals(Object o) {
			return (o instanceof SeasonKey ? ((SeasonKey)o).series == this.series && ((SeasonKey)o).season == this.season : false);
		}
		int	series;
		int season;
	};
	
	class EpisodeKey {
		EpisodeKey(int series, int season,int episode) {
			this.series  = series;
			this.season  = season;
			this.episode = episode;
		}
		
		@Override
		public int hashCode() {
			return series << 16 | season << 8 | episode;
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof EpisodeKey ? ((EpisodeKey)o).series == series && ((EpisodeKey)o).season == season && 
					((EpisodeKey)o).episode == episode : false;
		}
		
		int	series;
		int season;
		int episode;
	};
	
	private CacheMap<Integer,TvSeries> _series  = new CacheMap<Integer,TvSeries>(5000) {
		@Override
		TvSeries load(Integer key) {
			return api().getTvSeries().getSeries(key, getLanguage(), TvMethod.external_ids,TvMethod.images,TvMethod.credits);
		}
	};

	private CacheMap<SeasonKey,TvSeason>   _seasons  = new CacheMap<SeasonKey,TvSeason>(5000) {
		@Override
		TvSeason load(SeasonKey key) {
			return api().getTvSeasons().getSeason(key.series, key.season, getLanguage(),
					SeasonMethod.external_ids, SeasonMethod.credits);
		}
	};
	private CacheMap<EpisodeKey,EpisodeInfo> _episodes = new CacheMap<EpisodeKey,EpisodeInfo>(5000) {
		@Override
		EpisodeInfo load(EpisodeKey key) {
			return new EpisodeInfo(api().getTvEpisodes().getEpisode(key.series,key.season,key.episode, 
					getLanguage(), EpisodeMethod.credits,EpisodeMethod.external_ids,EpisodeMethod.images),null);
		}
	};

	private Tmdb() {
		final Semaphore mutex = new Semaphore(0);
		_key = TmdbKey.APIKEY;

		// This may trigger net access. Therefor we place it
		// into its own thread. 
		if(_api == null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					_trakt = new TraktV2();
					_trakt.setApiKey(TmdbKey.TRAKTID);
					_api = new TmdbApi(_key);
					_timezones = _api.getTimezones();
					mutex.release();
				}
			}).start();

			try {
				mutex.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Bitmap loadPoster(int size,String path,Activity act,ProgressBar pb) {
		
		if(api()==null)
			return null;
		
		if(size>=api().getConfiguration().getPosterSizes().size())
			return null;
		
		Bitmap bm = null;
		
		synchronized(this) {
			
			if(path!=null && (bm=_hash.get(size,path))==null) {
				try {
					final ProgressBar p = pb;
					if(act!=null && pb!=null) {
						act.runOnUiThread(new Runnable(){
							@Override
							public void run() {
								p.setVisibility(View.VISIBLE);
							}
						});
					}
					URL url = new URL(api().getConfiguration().getBaseUrl() + api().getConfiguration().getPosterSizes().get(size) + path);
					InputStream is = url.openConnection().getInputStream();
					bm = BitmapFactory.decodeStream(is);
					if(bm != null) {
						_hash.put(bm,size,path);
					}
					if(p!=null) {
						act.runOnUiThread(new Runnable(){
							@Override
							public void run() {
								p.setVisibility(View.INVISIBLE);
							}
						});
					}
				} catch(Exception ex) {
				}
			}

			if(act!=null && pb!=null) {
				final ProgressBar p = pb;
				act.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						p.setVisibility(View.INVISIBLE);
					}
				});
			}
		}
		
		return bm;
	}
	
	public Bitmap loadPoster(int size,String path) {
		return loadPoster(size,path,null,null);
	}

	public TvSeries loadSeries(int id) {
		return _series.get(id);
	}
	
	public TvSeason loadSeason(int series,int season) {
		return _seasons.get(new SeasonKey(series,season));
	}

	public EpisodeInfo loadEpisode(int series,int season,int episode) {
		return _episodes.get(new EpisodeKey(series,season,episode));
	}
	
	/** returns the TmdbApi object we are working with. Might be null
	 * if we currently don't have a connection.
	 *  
	 * @return
	 */
	
	TmdbApi api() {
		return _api;
	}
	
	TraktV2 trakt() {
		return _trakt;
	}
	
	static Tmdb get() {
		if( _one == null ) {
			_one=new Tmdb();
		}
		return _one;  
	}

	List<Timezone> getTimezones() {
		return _timezones;
	}

	
	public static CacheMap getSeriesCache() {
		return get()._series;
	}
	
	public static CacheMap getSeasonsCache() {
		return get()._seasons;
	}
	
	public static CacheMap getEpisodeCache() {
		return get()._episodes; 
	}
	
	public static BitmapHash getBitmapCache() {
		return get()._hash;
	}
	public static String getLanguage() {
		return "en";
	}

	public static boolean isDebug() {
		return Environment.isDebug();
	}

	public static Timezone getTimezone() {
		return null;
	}
}