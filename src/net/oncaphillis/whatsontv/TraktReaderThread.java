package net.oncaphillis.whatsontv;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import com.uwetrottmann.trakt.v2.entities.Episode;
import com.uwetrottmann.trakt.v2.entities.SearchResult;
import com.uwetrottmann.trakt.v2.enums.Extended;
import com.uwetrottmann.trakt.v2.enums.IdType;

public class TraktReaderThread extends Thread {
	private Semaphore _lock          = new Semaphore(0);
	private int _N = 0;
	private Map<Integer,Episode> _map = new HashMap<Integer,Episode>(); 
	private LinkedList<Integer> _list = new LinkedList<Integer>();
	
	@Override
	public void run()  {
		while(true) {
			try {
				_lock.acquire();
				int n = 0;
				synchronized(this) {
					n = _list.peekFirst();
				}
				try {
					List<SearchResult> l;
					l = Tmdb.get().trakt().search().idLookup(IdType.TMDB,Integer.toString(n), 1, null);
					
					if(l != null) {
						for(SearchResult r : l) {
							if(r.type.equals("episode") ) {
								Episode eps = Tmdb.get().trakt().episodes().summary(Integer.toString(r.show.ids.trakt),r.episode.season, 
										r.episode.number, Extended.FULL);
								if(eps.first_aired!=null) {
									synchronized(this) {
										_map.put(n,eps);
									}
								}
							}
						}
					}
				} catch(Throwable t) {
					t.printStackTrace();
				}
				
				synchronized(this) {
					_list.removeFirst();
				}
				
				Thread.sleep(100);
				_N++;
			} catch (InterruptedException e) {
			}
			synchronized(this) {
				_N++;
			}
		}
	}
	
	public Episode get(int id) {
		synchronized(this) {
			add(id);
			return _map.get(id);
		}
	}

	private void add(int id) {
		synchronized(this) {
			if(!_map.containsKey( id )) {
				_map.put( id ,null);
				_list.add(id);
				_lock.release();
			}
		}
	}
}
