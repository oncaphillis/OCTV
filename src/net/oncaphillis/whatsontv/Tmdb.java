package net.oncaphillis.whatsontv;

import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ProgressBar;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.config.Timezone;

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
}

public class Tmdb {
	private static Tmdb      _one  = null;
	private static String    _key  = null;
	private TmdbApi          _api  = null;
	private BitmapHash       _hash = new BitmapHash();
	private List<Timezone> _timezones= null;

	private Tmdb() {
		final Semaphore mutex = new Semaphore(0);
		_key = TmdbKey.APIKEY;

		// This may trigger net access. Therefor we place it
		// into its own thread. 
		if(_api == null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
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
			if((bm=_hash.get(size,path))!=null)
				return bm;
		}
		synchronized(this) {
			if(path!=null) {
				try {
					if(act!=null && pb!=null) {
						final ProgressBar p = pb;
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

	/** returns the TmdbApi object we are working with. Might be null
	 * if we currently don't have a connection.
	 *  
	 * @return
	 */
	
	TmdbApi api() {
		return _api;
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
}