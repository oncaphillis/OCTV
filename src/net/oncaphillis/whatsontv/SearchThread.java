package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Tread responsible for the download of TvSeries data (search or list)
 * page by page.
 * 
 * The concrete download call is managed via injection by a  Pager
 * class which loads one page of data at a time.
 * 
 * @author kloska
 *
 */
public class SearchThread extends Thread {
	
	static final int MAX_SEARCH = 2000;
	private Semaphore _lock       = new Semaphore(0);
	
	private Activity                 _activity;
	private ArrayAdapter<TvSeries>[] _listAdapters;
	private Pager[]                  _pagers;

	private int _list    = -1;
	private int _counts[] = null;
	private int _totals[] = null;
	
	public class Current {
		public Current(int l,int c,int t) {
			list  = l;
			count = c;
			total = t; 
		}
		public Current() {
		}
		
		public int list  = -1;
		public int count = -1;
		public int total = -1;
	};
	
	public SearchThread(Activity a,ArrayAdapter<TvSeries>[] listAdapters,Pager[] pagers ) {
		_activity    = a;		
		_listAdapters= listAdapters;
		_pagers      = pagers;

		_counts = new int[listAdapters.length];
		_totals = new int[listAdapters.length];

		for(int i=0;i<_counts.length;i++)
			_counts[i] =  -1;
		
		for(int i=0;i<_totals.length;i++)
			_totals[i] =  -1;
	}
	
	
	public SearchThread(SearchActivity a, ArrayAdapter<TvSeries> listadapter, Pager pager ) {
		this(a,new ArrayAdapter[]{listadapter},new Pager[]{pager} );
	}

	@Override
	public void run() {
		
		synchronized(this) {
			_list  =  0;
		}
		
		long t0 = TimeTool.getNow().getTime();
		
		for(_list=0; _list < _pagers.length ;) {
			final int fj = _list;

			Set<Integer> doubleSet = new TreeSet<Integer>();

			final Semaphore mutex = new Semaphore(0);

			Runnable r;
			_activity.runOnUiThread(new Runnable() {
		        @Override
		        public void run() {
					_listAdapters[fj].clear();
					_listAdapters[fj].notifyDataSetChanged();
			        mutex.release();					
		        }
		    });						

			try {
			    mutex.acquire();
			} catch (InterruptedException e) {
			}

			int page = 1;
			int n    = 0;
			int s    = 0;

			List<TvSeries> li_page= new ArrayList<TvSeries>();
			
			_pagers[_list].start();
			
			synchronized(this) {
				_counts[_list] = 0;
				_totals[_list] = 0;
			}

			List<TvSeries> o_li = new ArrayList<TvSeries>();
			
			
			while( ( _listAdapters[_list].getCount()) < MAX_SEARCH) {
				li_page=_pagers[_list].getPage(page++);
				if(li_page != null) {
					if(page==2) {
						synchronized(this) {
							_totals[_list]  = _pagers[_list].getTotal();
						}
					}
					Iterator<TvSeries> i = li_page.iterator();
					// #46 Eliminate doubles in list
					
					int d=0;
					while(i.hasNext()) {
						TvSeries tvs = i.next();
						if(!doubleSet.contains(tvs.getId())) {
							o_li.add(tvs);
							doubleSet.add(tvs.getId());
						} else {
							synchronized(this) {
								if(_totals[_list]  > 1) {
									_totals[_list]--;
									d++;
								}
							}
						}
					}
					synchronized(this) {
						_counts[_list]  += li_page.size()-d;
					}
				}
			

				
				if(TimeTool.getNow().getTime() - t0 > 4000 || _listAdapters[_list].getCount()+o_li.size()>=MAX_SEARCH || li_page == null || li_page.size() == 0) {

					lock();

					final List<TvSeries> li = o_li;
					_activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
						_listAdapters[fj].addAll(li);	
							updateAdapter(fj);
						}
					});
					
					release();
						
					o_li = new ArrayList<TvSeries>();
					
					t0 = TimeTool.getNow().getTime();
				}
				if(li_page == null || li_page.size() == 0)
					break;
			} 
						
			_pagers[_list].end();

			synchronized(this) {
				_list++;
			}
		}
	}
	
	// Return the current count of results
	public int getCount() {
		synchronized(this) {
			int n= -1;
			for(int i=0;i<_counts.length;i++) {
				n =  _counts[i] == -1 ? n : n == -1 ? _counts[i] : n + _counts[i];
			}
			return n;
		}
	}
	
	public int getCount(int list) {
		synchronized(this) {
			return list>=0 && list<_counts.length ? _counts[list] : -1;
		}
	}

	/** Holds the read Thread by acquireing the 
	 * associated semaphore.
	 */

	public void lock() {
		try {
			_lock.acquire();
		} catch (Exception ex) {
		}
	}
	
	public void release() {
		_lock.release();
	}

	public Current getCurrent() {
		synchronized(this) {
			if(this._list < this._counts.length)
				return new Current(this._list,this._counts[this._list],this._totals[this._list] > MAX_SEARCH ? MAX_SEARCH : this._totals[this._list]);
			else
				return new Current();
		}
	}

	private void updateAdapter(int list) {

		_listAdapters[list].sort(new Comparator<TvSeries>() {
			@Override
			public int compare(TvSeries o1, TvSeries o2) {
				String n1 = o1.getName().toUpperCase();
				String n2 = o2.getName().toUpperCase();
				
				for(String a : Environment.getArticles()) {
					String a0 = a.toUpperCase()+" ";
					if(n1.length() > a0.length() && n1.substring(0, a0.length()).equals(a0))  {
						n1 = n1.substring(a0.length());
						break;
					}
				}
				
				for(String a : Environment.getArticles()) {
					String a0 = a.toUpperCase()+" ";
					if(n2.length() > a0.length() && n2.substring(0, a0.length()).equals(a0))  {
						n2 = n2.substring(a0.length());
						break;
					}
				}
				
				return n1.compareTo(n2);
			}
		});
		_listAdapters[list].notifyDataSetChanged();
	}
};