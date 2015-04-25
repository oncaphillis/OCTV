package net.oncaphillis.whatsontv;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import com.uwetrottmann.trakt.v2.TraktV2;
import com.uwetrottmann.trakt.v2.entities.Episode;
import com.uwetrottmann.trakt.v2.entities.SearchResult;
import com.uwetrottmann.trakt.v2.enums.Extended;
import com.uwetrottmann.trakt.v2.enums.IdType;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
	private static Tmdb      _one     = null;
	private static String    _key     = null;
	private TmdbApi          _api     = null;
	private TraktV2          _trakt   = null;
	private BitmapCache       _hash    = null;
	private List<Timezone> _timezones = null;
	private TraktReaderThread _trakt_reader = new TraktReaderThread();
	static  Set<Integer>  _ss = new TreeSet<Integer>();
	static EpisodeKey     _d  = null;
	
	public class EpisodeInfo {
		private TvEpisode _tmdb_episode;
		private Episode _trakt_episode;
		private boolean _trakt_not_found = false;
		
		EpisodeInfo (TvEpisode tmdb,Episode trakt)  {
			_tmdb_episode = tmdb;
			_trakt_episode = trakt;
			
		}
		public TvEpisode getTmdb()  {
			return _tmdb_episode;
		}
		/** 
		 * Lazy loading of TraktT.TV Episode info.
		 * 
		 * @return Episode
		 */
		
		public Episode getTrakt()  {
			
			if(Environment.useTrakt() && _trakt_episode == null && ! _trakt_not_found) {
				_trakt_episode = Tmdb.get().trakt_reader().get(getTmdb().getId());
			}

			return _trakt_episode;
		}
		
		public Date getAirTime() {
			if(getTrakt()!=null && getTrakt().first_aired!=null) {
				return  getTrakt().first_aired.toCalendar(Locale.getDefault()).getTime();
			}
			return null;
		}
		
		public Date getAirDate() {
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
			return o instanceof EpisodeKey ? ((EpisodeKey)o).series == this.series && ((EpisodeKey)o).season == this.season && 
					((EpisodeKey)o).episode == this.episode : false;
		}
		
		int	series;
		int season;
		int episode;
	};

	private CacheMap<SeasonKey,TvSeason>   _seasons  = new CacheMap<SeasonKey,TvSeason>(5000) {
		@Override
		TvSeason load(SeasonKey key) {
			try {
				return api().getTvSeasons().getSeason(key.series, key.season, getLanguage(),
						SeasonMethod.external_ids, SeasonMethod.credits);
			} catch(Throwable ta) {
				return null;
			}
		}
	};
	private CacheMap<EpisodeKey,EpisodeInfo> _episodes = new CacheMap<EpisodeKey,EpisodeInfo>(5000) {
		@Override
		EpisodeInfo load(EpisodeKey key) {
			try {
				return new EpisodeInfo(api().getTvEpisodes().getEpisode(key.series,key.season,key.episode, 
						getLanguage(), EpisodeMethod.credits,EpisodeMethod.external_ids,EpisodeMethod.images),null);
			} catch(Throwable ta) {
			}
			return null;
		}
	};

	private int _seriesHit = 0;
	
	private Tmdb() {
		_trakt_reader.start();
	}

	private TmdbApi getApi() {
		if(_api == null || _api.getConfiguration() == null) {
			
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
						_api = new TmdbApi(TmdbKey.APIKEY);
						_timezones = _api.getTimezones();
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
				e.printStackTrace();
			}
		}
		return _api;
	}
	
	public Bitmap loadPoster(int size,String path,Activity act,ProgressBar pb) {
		
		if(_hash == null) {
			_hash = new BitmapCache(act.getCacheDir());
		}
		
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
		Integer key = new Integer(id);

		try {
			ContentValues cvi = new ContentValues();
			Cursor c = Environment.CacheHelper.getReadableDatabase().query(
					"SERIES", new String[] {
							"ID","TIMESTAMP","DATA"
					}, "ID=?", new String[] {
							key.toString()
					}, null, null, null);
			
			long n = TimeTool.getNow().getTime() / 1000;
			
			while(c.moveToNext()) {
				DatabaseUtils.cursorRowToContentValues(c, cvi);
				cvi.getAsByteArray("DATA");
				ByteArrayInputStream byteIn = new ByteArrayInputStream(cvi.getAsByteArray("DATA"));
				ObjectInputStream in = new ObjectInputStream(byteIn);
				TvSeries ts = (TvSeries)in.readObject();
				in.close();
				byteIn.close();

				long d = cvi.getAsLong("TIMESTAMP");						
				
				if( n - d > Environment.TTL) {
					Environment.CacheHelper.getWritableDatabase().delete("SERIES", "ID=?", new String[] {
							key.toString()
					});
					break;
				}
				_seriesHit  ++;
				return ts;
			}
			
			TvSeries ts = api().getTvSeries().getSeries(key, getLanguage(), TvMethod.external_ids,TvMethod.images,TvMethod.credits);
			
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(ts);
			out.close();
			byteOut.close();
			 
			ContentValues cvo = new ContentValues();
			 
			cvo.put("ID", key);
			cvo.put("TIMESTAMP", n);
			cvo.put("DATA",byteOut.toByteArray());
			 
			Environment.CacheHelper.getWritableDatabase().insert("SERIES",null,cvo);
			 
			return ts;
	         
		} catch(Throwable ta) {
			return null;
		}
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
	
	static TmdbApi api() {
		if(get()==null)
			return null;
		return get().getApi();
	}
	
	public TraktReaderThread trakt_reader() {
		return _trakt_reader;
	}
	TraktV2 trakt() {
		return _trakt;
	}
	
	static Tmdb get() {
		if( _one == null ) {
			try {
				_one=new Tmdb();
			} catch(Throwable t) {
				return null;
			}
		}
		return _one;  
	}

	List<Timezone> getTimezones() {
		return _timezones;
	}
	
	public static CacheMap<?, ?> getSeasonsCache() {
		return get()._seasons;
	}
	
	public static CacheMap<?, ?> getEpisodeCache() {
		return get()._episodes; 
	}
	
	public static BitmapCache getBitmapCache() {
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

	public static long getSeriesCacheSize() {
		Cursor c = Environment.CacheHelper.getReadableDatabase().query("SERIES",new String[] {
				"COUNT(*)"},null,null,null,null,null);
		
		while(c.moveToNext()) {
			ContentValues cvi = new ContentValues();
			DatabaseUtils.cursorRowToContentValues(c, cvi);
			return cvi.getAsLong("COUNT(*)");
		}
		return 0;
	}

	public static int getSeriesCacheHits() {
		return get()._seriesHit;
	}

	public  Bitmap getCachedPoster(int size,String path) {
		if(_hash == null) {
			return null;
		}	
		return _hash.get(size,path);
	}
}