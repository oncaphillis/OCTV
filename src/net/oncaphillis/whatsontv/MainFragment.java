package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.app.Fragment;

public class MainFragment extends Fragment {
	private Activity _activity = null;
	private View _rootView = null;
	public  ProgressBar  BigProgressBar = null;
	public  TextView     BigProgressText = null;

	private GridView _mainGridView = null;
	
	
	private int _idx;
	private int _cols;
	
	@Override
    public View onCreateView(LayoutInflater inflater,
    		ViewGroup container, Bundle savedInstanceState) {
		Bundle b = this.getArguments();
		_idx  = b.getInt("idx");
		
		int _cols = 1;
		if(_activity!=null){
			DisplayMetrics displaymetrics = new DisplayMetrics();
			_activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			
			float width  = displaymetrics.widthPixels * 160.0f / displaymetrics.xdpi;
			
			if(width > 400.0f)
				_cols=2;
			if(width > 800.0f)
				_cols=3;
			if(width > 1000.0f)
				_cols=4;
		}
		
		_rootView   = inflater.inflate(R.layout.main_fragment, container, false);
		
		/*synchronized(MainActivity.StoredResults) {
			if(savedInstanceState!=null && MainActivity.StoredResults[_idx]==null) {	
				MainActivity.StoredResults[_idx] = (HashMap<Integer,List<TvSeries>>)savedInstanceState.getSerializable("map");
			}
		}*/
		
		BigProgressBar  = (ProgressBar)_rootView.findViewById(R.id.big_progress_bar);
		BigProgressText = (TextView)_rootView.findViewById(R.id.big_progress_text);
		
		_mainGridView    = (GridView)    _rootView.findViewById(R.id.main_list);
		
		_mainGridView.setNumColumns(_cols);
		
			Object o = MainActivity.ListAdapters[_idx];
				
			_mainGridView.setAdapter(MainActivity.ListAdapters[_idx]);
			
			_mainGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					Intent myIntent = new Intent(_activity, SeriesPagerActivity.class);
					Bundle b        = new Bundle();
					synchronized(MainActivity.ListAdapters[_idx]) {
						int[]    ids   = new int[MainActivity.ListAdapters[_idx].getCount()];
						String[] names = new String[MainActivity.ListAdapters[_idx].getCount()];
						
						for(int ix=0;ix<MainActivity.ListAdapters[_idx].getCount();ix++) {
							ids[ix]   = MainActivity.ListAdapters[_idx].getItem(ix).getId();
							names[ix] = MainActivity.ListAdapters[_idx].getItem(ix).getName();
						}
						
						b.putString(SeriesObjectFragment.ARG_TITLE, MainActivity.Titles[_idx]);
						b.putIntArray("ids", ids);
						b.putStringArray("names", names);
						b.putInt("ix", position);
					
						myIntent.putExtras(b);
						startActivity(myIntent);
					}
				}
			});

			final ProgressBar pb = BigProgressBar;
			final TextView tv = BigProgressText;

			if(_activity!=null) {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						int ci=0;
						final String[] cycle={"  ."," ..","...","..*",".**","***","**.","*..","..."," .."};
						while(MainActivity.SearchThread.getState()!=Thread.State.TERMINATED) {
							final int c = ci;
		
							_activity.runOnUiThread(new Runnable(){
								
								@Override
								public void run() {
									synchronized(MainActivity.StoredResults) {
										if(MainActivity.StoredResults[_idx].size()!=0)
											tv.setText(Integer.toString(MainActivity.ListAdapters[_idx].getCount()));
										else
											tv.setText(cycle[c]);
									}										
								}
							});
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
							ci++;
							if(ci>=cycle.length)
								ci=0;
						}
						_activity.runOnUiThread(new Runnable(){
							@Override
							public void run() {
								pb.setVisibility(View.INVISIBLE);
								tv.setVisibility(View.INVISIBLE);
							}							
						});
					}
					
				}).start();
			}
        return _rootView;
    }
		
	@Override
	public void onSaveInstanceState(Bundle outState) { 
		/*synchronized(MainActivity.StoredResults) {
			if(MainActivity.StoredResults[_idx]!=null)
				outState.putSerializable("map", MainActivity.StoredResults[_idx]);
		}*/
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	
	public void onAttach(Activity act) {
        _activity = act;
        super.onAttach(act);
	}
}
