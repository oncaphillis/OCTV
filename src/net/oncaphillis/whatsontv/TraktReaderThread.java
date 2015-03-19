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

public class TraktReaderThread extends Thread {
	private Semaphore _lock          = new Semaphore(0);

	private Map<Integer,Episode> _map = new HashMap<Integer,Episode>(); 
	private LinkedList<Integer> _list = new LinkedList<Integer>();
	private WeakHashMap<Runnable,Void> _listeners = new WeakHashMap<Runnable,Void>();
	
	
	@Override
	public void run()  {

		while(true) {
		
			try {
				_lock.acquire();
			} catch (InterruptedException e1) {
			}
			
			int n = 0;

			synchronized(this) {
				n = _list.peekFirst();
			}

			try {
				Episode eps = null;
				List<SearchResult> l;

				l = Tmdb.get().trakt().search().idLookup(IdType.TMDB,Integer.toString(n), 1, null);					

				if(l != null) {

					for(SearchResult r : l) {
						if(r.type.equals("episode") ) {

							eps = Tmdb.get().trakt().episodes().summary(Integer.toString(r.show.ids.trakt),r.episode.season, 
									r.episode.number, Extended.FULL);

							if(eps.first_aired!=null) {
								synchronized(this) {
									_map.put(n,eps);

								}
							}
						}
					}
				}

				synchronized(this) {
					_list.removeFirst();
					if(_list.isEmpty())
						inform();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
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
			}
			return _map.get(id);
		}
	}

	private void inform() {
		Runnable o;
		synchronized(this) {
			for(Runnable r : _listeners.keySet()) {
				try {
					r.run();
				} catch(Throwable t) {
					o= r;
				}
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