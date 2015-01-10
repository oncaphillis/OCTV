package net.oncaphillis.whatsontv;

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
	
	private Activity               _activity;
	private ArrayAdapter<TvSeries> _listAdapter;
	private Pager                  _pager;
	private ProgressBar            _pb;
	private TextView               _tv;
	
	public SearchThread(Activity a,ArrayAdapter<TvSeries> l,Pager p,ProgressBar pb,TextView tv) {
		_activity    = a;		
		_listAdapter = l;
		_pager       = p;
		_pb          = pb;
		_tv          = tv;
	}
	
	@Override
	public void run() {
		
		_activity.runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
				synchronized(_listAdapter) {
					_listAdapter.clear();
					_listAdapter.notifyDataSetChanged();
				}
	        }
	    });						

		int page = 1;
		int n = 0;
		int s = 0;
		
		if(Tmdb.get().api()==null) {
			return;
		}

		List<TvSeries> li_page= new ArrayList<TvSeries>();
		
		_pager.start();

		while(true) {
			int nn=0;
			while( _listAdapter.getCount() <= MAX_SEARCH) {
				lock();
				li_page=_pager.getPage(page++);
				final TextView tv = _tv;
				final List<TvSeries> li = li_page;
				_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						_listAdapter.addAll(li);						
						if( tv != null ) {
							tv.setText(Integer.toString(_listAdapter.getCount()));
						}
					}
				});
				
				release();

				if(li_page.size() == 0)
					break;
			} 
			break;
		}
		
		_pager.end();
		
		if(_pb!=null || _tv!=null) {
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

	public void release() {
		_lock.release();
	}

	public void lock() {
		try {
			_lock.acquire();
		} catch (Exception ex) {
		}
	}
};