package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.oncaphillis.whatsontv.SearchThread.Current;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.app.Fragment;

public class MainFragment extends Fragment {
	private Activity     _activity    = null;
	private View         _theView    = null;
	private ProgressBar  _progressBar = null;

	private GridView _mainGridView = null;
	
	private int _idx;
	
	@Override
    public View onCreateView(LayoutInflater inflater,
    		ViewGroup container, Bundle savedInstanceState) {
		Bundle b = this.getArguments();
		
		_idx  = b.getInt("idx");
		
		int cols = Environment.getColumns(_activity);

		_theView   = inflater.inflate(R.layout.main_fragment, container, false);
				
		_progressBar  = (ProgressBar)_theView.findViewById(R.id.load_progress);
		
		
		_mainGridView    = (GridView)    _theView.findViewById(R.id.main_list);
		
		_mainGridView.setNumColumns(cols);
		
			Object o = Environment.ListAdapters[_idx];
				
			_mainGridView.setAdapter(Environment.ListAdapters[_idx]);
			
			_mainGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					Intent myIntent = new Intent(_activity, SeriesPagerActivity.class);
					Bundle b        = new Bundle();
					synchronized(Environment.ListAdapters[_idx]) {
						int[]    ids   = new int[Environment.ListAdapters[_idx].getCount()];
						String[] names = new String[Environment.ListAdapters[_idx].getCount()];
						
						for(int ix=0;ix<Environment.ListAdapters[_idx].getCount();ix++) {
							ids[ix]   = Environment.ListAdapters[_idx].getItem(ix).getId();
							names[ix] = Environment.ListAdapters[_idx].getItem(ix).getName();
						}
						
						b.putString(SeriesObjectFragment.ARG_TITLE, Environment.Titles[_idx]);
						b.putIntArray("ids", ids);
						b.putStringArray("names", names);
						b.putInt("ix", position);
					
						myIntent.putExtras(b);
						startActivity(myIntent);
					}
				}
			});
			
			if(_activity!=null && _activity instanceof MainActivity) {
				
				final MainActivity act = (MainActivity)_activity;
				final MainFragment f = this;
				final LinearLayout ll = (LinearLayout)_theView.findViewById(R.id.no_list_found_layout);
				
				ll.setVisibility(View.GONE);
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						while(act!=null && act.SearchThread!=null && act.SearchThread.getState()!=Thread.State.TERMINATED) {
							act.runOnUiThread(new Runnable(){
								@Override
								public void run() {
									Current ci = act.SearchThread.getCurrent();
									synchronized(act) {
										if( f.getIdx() < ci.list) {
											f.setProgressBarVisibility(false);							
											if(act.SearchThread.getCount(f.getIdx()) == 0)
												ll.setVisibility(View.VISIBLE);
										} else if(f.getIdx()==ci.list && ci.count>0) {
											f.setProgressBarVisibility(true);
											f.setProgressBarIndeterminate(false);
											f.setProgress(ci.count * 10000 / ci.total);
										} else {
											f.setProgressBarVisibility(true);
											f.setProgressBarIndeterminate(true);
										}
									}
								}
							});
							
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
						}
						
						act.runOnUiThread(new Runnable(){
							@Override
							public void run() {
								if(act.SearchThread.getCount(f.getIdx()) == 0)
									ll.setVisibility(View.VISIBLE);
								f.setProgressBarIndeterminate(false);
								f.setProgressBarVisibility(false);
							}
						});
					}
				}).start();
			}
			
        return _theView;
    }
		
	@Override
	public void onSaveInstanceState(Bundle outState) { 
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onAttach(Activity act) {
        _activity = act;
        super.onAttach(act);
	}

	public int getIdx() {
		return _idx;
	}

	public void setProgressBarVisibility(boolean b) {
		if(_progressBar!=null) {
			_progressBar.setVisibility( b ? View.VISIBLE : View.GONE);
		}
	}

	public void setProgressBarIndeterminate(boolean b) {
		if(_progressBar!=null) {
			_progressBar.setIndeterminate(b);
		}
	}

	public void setProgress(int i) {
		if(_progressBar!=null) {
			_progressBar.setProgress(i);
		}
	}
}
