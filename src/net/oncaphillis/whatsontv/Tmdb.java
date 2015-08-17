package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ProgressBar;

import com.uwetrottmann.trakt.v2.TraktV2;
import com.uwetrottmann.trakt.v2.entities.Episode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbTV.TvMethod;
import info.movito.themoviedbapi.TmdbTvEpisodes.EpisodeMethod;
import info.movito.themoviedbapi.TmdbTvSeasons.SeasonMethod;
import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.config.Account;
import info.movito.themoviedbapi.model.config.Timezone;
import info.movito.themoviedbapi.model.config.TokenSession;
import info.movito.themoviedbapi.model.core.SessionToken;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

abstract class CacheMap<K,V> {
	
	private int _max = 5000;
	private int _hit = 0;
	
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
	
	/** Store a new value under K. Either creates a new node
	 * or overwrites the Value in an old one.
	 * @param id
	 * @param value
	 */
	
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
		} else {
			_hit++;
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
	
	public int hit() {
		return _hit;
	}
	
	abstract V load(K key);
};

public class Tmdb {
	private static boolean   _initialized    = false;
	private static TmdbApi _tmdb = null;
	private static TraktV2   _trakt   = null;
	private static BitmapCache       _hash   = null;
	private static List<Timezone> _timezones = null;
	private static TraktReaderThread _trakt_reader = new TraktReaderThread();

	private static TtlCache<Integer,TvSeries> _seriesCache =
			new TtlCache<Integer,TvSeries>(Environment.TTL,100);

    private static Account _account = null;

    private static String _sessionId = null;

    public static TvResultsPage searchTv(String query, String lang, int page) {
		return _tmdb.getSearch().searchTv(query, lang, page);
	}

    public static TvResultsPage getAiringToday(String language, int page, Timezone timezone) {
        return _tmdb.getTvSeries().getAiringToday(language, page, timezone);
    }

    public static TvResultsPage getOnTheAir(String language, int page) {
        return _tmdb.getTvSeries().getOnTheAir(language, page);
    }


    public static TvResultsPage getTopRated(String language, int page) {
        return _tmdb.getTvSeries().getTopRated(language, page);
    }


    public static TvResultsPage getPopular(String language, int page) {
        return _tmdb.getTvSeries().getPopular(language, page);
    }


    public static Credits getCredits(TvSeries series, String lang) {
        return _tmdb.getTvSeries().getCredits(series.getId(), lang);
    }

    public static boolean login(String u, String p) {
        TokenSession s = _tmdb.getAuthentication().getSessionLogin(u, p);
        if(s != null && (_sessionId=s.getSessionId()) != null) {
            _account = _tmdb.getAccount().getAccount(new SessionToken(s.getSessionId()));
            return true;
        }
        return false;
    }

    public static String getUserName() {
        return _account == null ? null : _account.getUserName();
    }

    public static String getSessionId() {
        return _sessionId;
    }

    static
	private class SeasonNode {
		public int series;
		public int season;
		SeasonNode(int ser,int sea) {
			series = ser;
			season = sea;
		}
		
		@Override
		public int hashCode() {
			return (series << 8) | season;
		}
		
		@Override 
		public boolean equals( Object o ) {
			return o instanceof SeasonNode && 
					((SeasonNode)o).series == this.series && ((SeasonNode)o).season == this.season;
		}
	}

    static
	private TtlCache<SeasonNode,TvSeason> _seasonCache = 
			new TtlCache<SeasonNode,TvSeason>(Environment.TTL,100);
    static
	private long _sqlSelect = 0;

    static
    private long _sqlDelete = 0;

    static
    private long _sqlInsert = 0;

    static
	private int  _seriesHit = 0;

    static
    private int  _seasonHit = 0;

    /** Serves as a Proxy for {@code TvEpisode} objects. Associated
     * with a TvEpisode it returns the airing date/time as a {@code Date}
     * object and provides it as Trakt data if such data is available.
     *
     * @author Sebastian Kloska
     */

    public static class EpisodeInfo {
		private TvEpisode _tmdb_episode;
		
		EpisodeInfo (TvEpisode tmdb)  {
			_tmdb_episode = tmdb;
			
		}
		public TvEpisode getTmdb()  {
            return _tmdb_episode;
		}

		/** 
		 * Lazy loading of TraktT.TV Episode info.
		 * 
		 * @return Episode
		 */
		
		private Episode getTrakt()  {
			if(Environment.useTrakt()) {
				return Tmdb.trakt_reader().get(getTmdb().getId());
			}
			return null;
		}
		
		public Date getAirTime() {
			Episode e;
			if( ((e=getTrakt())!=null) && e.first_aired!=null) {
				return  e.first_aired.toCalendar(Locale.getDefault()).getTime();
			}
			return null;
		}
		
		public Date getAirDate() {
			Episode e;
			if( (e=getTrakt())!=null && e.first_aired!=null) {
				return TimeTool.toDate(e.first_aired.toCalendar(Locale.getDefault()).getTime());
			}
			return Tmdb.getAirDate(getTmdb());
		}
	}
	
	static Date getAirDate(TvEpisode te) {
		if(te.getAirDate()!=null) {
			try {
				return Environment.TmdbDateFormater.parse(te.getAirDate());
			} catch (ParseException e) {
			}
		}
		return null;
	}

	static Date getAirDate(TvSeason ts) {
		if(ts.getAirDate()!=null) {
			try {
				return Environment.TmdbDateFormater.parse(ts.getAirDate());
			} catch (ParseException e) {
			}
		}
		return null;
	}

    static
	private class EpisodeKey {
		
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
			return o instanceof EpisodeKey ? ((EpisodeKey)o).series == this.series && ((EpisodeKey)o).season == this.season && 
					((EpisodeKey)o).episode == this.episode : false;
		}
		
		int	series;
		int season;
		int episode;
	};

    static
	private CacheMap<EpisodeKey,EpisodeInfo> _episodes = new CacheMap<EpisodeKey,EpisodeInfo>(5000) {
		@Override
		EpisodeInfo load(EpisodeKey key) {
			try {
				return new EpisodeInfo(_tmdb.getTvEpisodes().getEpisode(key.series,key.season,key.episode,
						getLanguage(), EpisodeMethod.credits,EpisodeMethod.external_ids,EpisodeMethod.images));
			} catch(Throwable ta) {
			}
			return null;
		}
	};

	static
    public void init() throws Exception {

        if(!_initialized) {

			final Semaphore mutex = new Semaphore(0);

			// This may trigger net access. Therefor we place it
			// into its own thread. 
			
			new Thread(new Runnable() {
				String _e = null;
				@Override
				public void run() {
					try {
						_trakt = new TraktV2();
						_trakt.setApiKey(TmdbKey.TRAKTID);
						_tmdb = new TmdbApi(TmdbKey.APIKEY);
						_timezones = _tmdb.getTimezones();
					} catch(Exception ex) {
						_e = ex.getMessage();
					} finally {
						mutex.release();
					}
				}
			}).start();

			try {
				mutex.acquire();
			} catch (InterruptedException e) {
                throw e;
			}

            if(_tmdb == null || _timezones == null || _trakt == null)
                throw new Exception("Failed to set up TmDb/Trakt");

            _trakt_reader.start();

            _initialized = true;
		}
	}

	static
	public Bitmap loadPoster(int size,String path,Activity act,ProgressBar pb) {
		
		if(_hash == null) {
			_hash = new BitmapCache(act.getCacheDir());
		}

		if(size>= _tmdb.getConfiguration().getPosterSizes().size())
			return null;
		
		Bitmap bm = null;
		
		synchronized(_hash) {
			
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
					URL url = new URL(_tmdb.getConfiguration().getBaseUrl() + _tmdb.getConfiguration().getPosterSizes().get(size) + path);
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

    static
	public Bitmap loadPoster(int size,String path) {
		return loadPoster(size,path,null,null);
	}

	static <O> byte[] toByteArray(O o) throws IOException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		out.writeObject(o);
		out.close();
		byteOut.close();

		return byteOut.toByteArray();
	}
	static <O> O fromByteArray(byte[] ba) throws IOException, ClassNotFoundException {

		ByteArrayInputStream byteIn = new ByteArrayInputStream(ba);
		ObjectInputStream in = new ObjectInputStream(byteIn);
		O o = (O)in.readObject();
		in.close();
		byteIn.close();

		return o;
	}

	static
	public TvSeries loadSeries(int id) {
		Integer key = id;
		
		TvSeries tvs = _seriesCache.get(id);
		
		if(tvs != null)
			return tvs;

		try {
			ContentValues cvi = new ContentValues();
			Cursor c = Environment.CacheHelper.getReadableDatabase().query(
					"SERIES", new String[] {
							"ID","TIMESTAMP","DATA"
					}, "ID=?", new String[] {
							key.toString()
					}, null, null, null);
			_sqlSelect++;

			long n = TimeTool.getNow().getTime() / 1000;
			
			while(c.moveToNext()) {
				DatabaseUtils.cursorRowToContentValues(c, cvi);
				TvSeries ts = fromByteArray(cvi.getAsByteArray("DATA"));

				long d = cvi.getAsLong("TIMESTAMP");						
				
				if( n - d > Environment.TTL) {
					Environment.CacheHelper.getWritableDatabase().delete("SERIES", "ID=?", new String[] {
							key.toString()
					});
					_sqlDelete++;
					break;
				}
				_seriesHit  ++;
				_seriesCache.put(id, ts);
				return ts;
			}
			
			TvSeries ts = _tmdb.getTvSeries().getSeries(key, getLanguage(), TvMethod.external_ids,TvMethod.images,TvMethod.credits);

			ContentValues cvo = new ContentValues();
			 
			cvo.put("ID", key);
			cvo.put("TIMESTAMP", n);
			cvo.put("DATA",toByteArray(ts) );
			 
			Environment.CacheHelper.getWritableDatabase().insert("SERIES",null,cvo);
			_sqlInsert++;

			_seriesCache.put(id, ts);

			return ts;
	         
		} catch(Throwable ta) {
			return null;
		}
	}

	static
	public TvSeason loadSeason(int series,int season) {
		try {
			Integer ser = series;
			Integer sea = season;
			
			{
				TvSeason tvs = _seasonCache.get(new SeasonNode(ser,sea));
				
				if(tvs!=null)
					return tvs;
			}
			
			ContentValues cvi = new ContentValues();
			
			Cursor c = Environment.CacheHelper.getReadableDatabase().query(
					"SEASON", new String[] {
						"SERIES","ID","TIMESTAMP","DATA"
					}, "SERIES=? AND ID=?", new String[] {
						ser.toString(),
						sea.toString()
					}, null, null, null);
				
			_sqlSelect++;
			long n = TimeTool.getNow().getTime() / 1000;
				
			while(c.moveToNext()) {
				DatabaseUtils.cursorRowToContentValues(c, cvi);
				TvSeason ts = fromByteArray(cvi.getAsByteArray("DATA"));

				long d = cvi.getAsLong("TIMESTAMP");						
				
				if( n - d > Environment.TTL) {
					Environment.CacheHelper.getWritableDatabase().delete("SEASON", "SERIES=? AND ID=?", new String[] {
							ser.toString(),
							sea.toString()
					});
					_sqlDelete++;
					break;
				}
				_seasonHit  ++;
				_seasonCache.put(new SeasonNode(ser,sea),ts); 
				return ts;
			}

			TvSeason tvs = _tmdb.getTvSeasons().getSeason(series, season, getLanguage(),
                    SeasonMethod.external_ids, SeasonMethod.credits);

			ContentValues cvo = new ContentValues();
				 
			cvo.put("SERIES", ser);
			cvo.put("ID", sea);
			cvo.put("TIMESTAMP", n);
			cvo.put("DATA",toByteArray(tvs) );
				 
			Environment.CacheHelper.getWritableDatabase().insert("SEASON",null,cvo);
			_sqlInsert++;

			_seasonCache.put(new SeasonNode(ser,sea),tvs); 
				 
			return tvs;
		         
		} catch(Throwable ta) {
			return null;
		}
	}

	static
    public EpisodeInfo loadEpisode(int series,int season,int episode) {
		return _episodes.get(new EpisodeKey(series,season,episode));
	}

    /** The {@TraktTReaderThread} returned by this method is responsible
     * for lazy loading of {@Trakt.tv} records
     *
     * @return TraktReaderThread or null
     */

    static
	public TraktReaderThread trakt_reader() {
		return _trakt_reader;
	}

    static
	TraktV2 trakt() {
		return _trakt;
	}

    static
	List<Timezone> getTimezones() {
		return _timezones;
	}

    static
    public CacheMap<?, ?> getEpisodeCache() {
		return _episodes;
	}
	
	public static BitmapCache getBitmapCache() {
		return _hash;
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

	public static TtlCache<Integer,TvSeries> getSeriesPreCache() {
		return _seriesCache;
	}

	public static TtlCache<SeasonNode,TvSeason> getSeasonPreCache() {
		return _seasonCache;
	}
	
	public static long getSeriesCacheSize() {
		Cursor c = Environment.CacheHelper.getReadableDatabase().query("SERIES",new String[] {
			"COUNT(*)"},
			null,null,null,null,null);

		while(c.moveToNext()) {
			ContentValues cvi = new ContentValues();
			DatabaseUtils.cursorRowToContentValues(c, cvi);
			return cvi.getAsLong("COUNT(*)");
		}
		return 0;
	}

    static
	public long getSqlSelectCount() {
		return _sqlSelect;
	}

    static
	public long getSqlInsertCount() {
		return _sqlInsert;
	}

    static
	public long getSqlDelete() {
		return _sqlDelete;
	}

    static
	public int getSeriesCacheHits() {
		return _seriesHit;
	}

	static
    public  Bitmap getCachedPoster(int size,String path) {
		if(_hash == null) {
			return null;
		}	
		return _hash.get(size,path);
	}

    static
	public long getSeasonsCacheSize() {
		Cursor c = Environment.CacheHelper.getReadableDatabase().query("SEASON",new String[] {
			"COUNT(*)"},
			null,null,null,null,null);

		while(c.moveToNext()) {
			ContentValues cvi = new ContentValues();
			DatabaseUtils.cursorRowToContentValues(c, cvi);
			return cvi.getAsLong("COUNT(*)");
		}
		return 0;
	}

    static
	public int getSeasonsCacheHits() {
		return _seasonHit;
	}
}
