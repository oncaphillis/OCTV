package net.oncaphillis.whatsontv;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Semaphore;

import com.uwetrottmann.trakt.v2.entities.Episode;
import com.uwetrottmann.trakt.v2.entities.SearchResult;
import com.uwetrottmann.trakt.v2.enums.Extended;
import com.uwetrottmann.trakt.v2.enums.IdType;

/**
 * We use track.tv to pull the time value for a given episode
 * from the net since TMDB only provides the date.
 * However since trakt.tv appears to be slow and sometimes
 * unreliable we do a very laaazy load.
 * 
 * This Thread behaves like a container for trakt.tv episode
 * records. Whenever a record is missing we immediately return
 * null but place the key in a queue for later download.
 * Whenever all requested keys are downloaded we inform 
 * objects that they may want to update their data.
 * 
 * @author kloska
 *
 */

public class TraktReaderThread extends Thread {
	private Semaphore _lock          = new Semaphore(0);
	private Map<Integer,Episode> _map = new HashMap<Integer,Episode>(); 
	private LinkedList<Integer> _list = new LinkedList<Integer>();
	private WeakHashMap<Runnable,Void> _listeners = new WeakHashMap<Runnable,Void>();
	private String _e ;
	private long _loadCount = 0;
	private long _lookupCount = 0;
	private long _hitCount = 0;
	
	@Override
	public void run()  {

		while(true) {

			int n = 0;
		
			try {
				_lock.acquire();
				synchronized(this) {
					n = _list.peekFirst();
				}

				Episode eps = null;
				List<SearchResult> l;

				l = Tmdb.get().trakt().search().idLookup(IdType.TMDB,Integer.toString(n), 1, null);					

				synchronized(this) {
					_lookupCount++;
				}
				
				if(l != null) {

					for(SearchResult r : l) {
						if(r.type.equals("episode") ) {

							eps = Tmdb.get().trakt().episodes().summary(Integer.toString(r.show.ids.trakt),r.episode.season, 
									r.episode.number, Extended.FULL);

							if(eps.first_aired!=null) {
								synchronized(this) {
									_map.put(n,eps);
									_loadCount++;
								}
							}
						}
					}
				}

				synchronized(this) {
					try {
						_list.removeFirst();
						if(_list.isEmpty())
							inform();
					} catch(Throwable t) {
						_e = t.getMessage();
					}
				}
				Thread.sleep(100);
			} catch(Throwable t) {
				synchronized(this) {
					_list.removeFirst();
					_list.addLast(n);
					_lock.release();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public void register(Runnable r) {
		synchronized(this) {
			_listeners.put(r, null);
		}
	}
	
	public Episode get(int id) {
		synchronized(this) {
			if( !_map.containsKey(id) )  {
				add(id);
				_hitCount--;
			}
			_hitCount++;
			return _map.get(id);
		}
	}
	public long getHitCount() {
		synchronized(this) {
			return _hitCount;
		}
	}
	public long getLoadCount() {
		synchronized(this) {
			return _loadCount;
		}
	}
	public long getLookupCount() {
		synchronized(this) {
			return _lookupCount;
		}
	}
	private void inform() {
		synchronized(this) {
			for(Runnable r : _listeners.keySet()) {
				r.run();
			}
		}
	}
	private void add(int id) {
		synchronized(this) {
			if(!_map.containsKey( id )) {
				_map.put( id ,null );
				_list.add(id);
				_lock.release();
			}
		}
	}
}