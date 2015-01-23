package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SearchThread extends Thread {
	
	static final int MAX_SEARCH = 1000;
	private Semaphore _lock       = new Semaphore(0);
	
	private Activity                 _activity;
	private ArrayAdapter<TvSeries>[] _listAdapters;
	private Pager[]                  _pagers;
	private ProgressBar              _pb;
	private TextView                 _tv;
	
	public SearchThread(Activity a,ArrayAdapter<TvSeries>[] listAdapters,Pager[] pagers,ProgressBar pb,TextView tv) {
		_activity    = a;		
		_listAdapters= listAdapters;
		_pagers      = pagers;
		_pb          = pb;
		_tv          = tv;
	}
	
	
	public SearchThread(SearchActivity a, ArrayAdapter<TvSeries> listadapter,
			Pager pager, ProgressBar pb, TextView tv) {
		this(a,new ArrayAdapter[]{listadapter},new Pager[]{pager},pb,tv);
	}

	@Override
	public void run() {
		
		for(int j=0; j < _pagers.length ; j++) {
			final int fj = j;

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
			    e.printStackTrace();
			}
			
			
			int page = 1;
			int n = 0;
			int s = 0;
			
			if(Tmdb.get().api()==null) {
				return;
			}
	
			List<TvSeries> li_page= new ArrayList<TvSeries>();
			
			_pagers[j].start();
			
			int cc;
			while( (cc=_listAdapters[j].getCount()) < MAX_SEARCH) {
				
				lock();
				
				li_page=_pagers[j].getPage(page++);
				final TextView tv = _tv;
				final List<TvSeries> li = li_page;
				_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						_listAdapters[fj].addAll(li);						
						_listAdapters[fj].notifyDataSetChanged();
						if( tv != null ) {
							tv.setText(Integer.toString(_listAdapters[fj].getCount()));
						}
					}
				});
				
				release();

				if(li_page.size() == 0)
					break;
			} 
			
			_pagers[j].end();
			
			if( _pb != null || _tv != null) {
				final ProgressBar pb = _pb;
				final TextView    tv = _tv;
				_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(pb!=null)
							pb.setVisibility(View.INVISIBLE);
			        
						if(tv!=null)
							tv.setVisibility(View.INVISIBLE);
					}
				});
			}
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
	
	/** 
	 * 
	 */
	
	public void release() {
		_lock.release();
	}

};