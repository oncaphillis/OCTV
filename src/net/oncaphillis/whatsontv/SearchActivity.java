package net.oncaphillis.whatsontv;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.tv.TvSeries;

public class SearchActivity extends Activity {

	private GridView _gridView;
	private ArrayAdapter<TvSeries> _listAdapter;
	private List<TvSeries> _mainList = new ArrayList<TvSeries>();


	private SearchThread           _searchThread = null;
	private String                 _query = "";
	private ProgressBar _progressBar = null;

	private TaskObserver _progressObserver = null;
	private LinearLayout _nothingFoundLayout = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
        
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
		_gridView    = (GridView)    findViewById(R.id.search_table);
		_progressBar = ( ProgressBar ) findViewById( R.id.search_progress);
		
		_nothingFoundLayout = (LinearLayout) findViewById( R.id.no_search_found_layout);
		_nothingFoundLayout.setVisibility(View.GONE);
		
		_progressBar.setIndeterminate(true);
		_progressBar.setVisibility(View.VISIBLE);
		
		final int pos = savedInstanceState != null && savedInstanceState.containsKey("top") ? 
				savedInstanceState.getInt("top") : 0;
				
		final Activity act = this;

		_progressObserver = new TaskObserver() {

			@Override
			void beginProgress() {
				if(_progressBar!=null) {
					act.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							_progressBar.setVisibility(View.VISIBLE);
						}
					});
				}
			}

			@Override
			void endProgress() {
				if(_progressBar!=null) {
					act.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							
							int cc = _searchThread.getCount();

							if(_nothingFoundLayout!=null && _searchThread.getCount()==0)
								_nothingFoundLayout.setVisibility(View.VISIBLE);
							
							_progressBar.setVisibility(View.GONE);
						
						}
					});
				}
				act.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						_gridView.invalidate();
						_gridView.requestFocusFromTouch();
						_gridView.setSelection(pos);
					}
				});
			}
		};

	    /// Set up our special		
		_listAdapter = new TvSeriesListAdapter(this,
				android.R.layout.simple_list_item_1,_mainList,this);
		
		int cols = Environment.getColumns(this);

		_gridView.setNumColumns(cols);
		
		_gridView.setAdapter(_listAdapter);
		
		_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent myIntent = new Intent(SearchActivity.this, SeriesPagerActivity.class);
				Bundle b        = new Bundle();
				
				int[]    ids   = new int[_mainList.size()];
				String[] names = new String[_mainList.size()];
				Iterator<TvSeries> i=_mainList.iterator();
				
				int ix=0;
				TvSeries tv;

				while(i.hasNext()) {
					tv = i.next();
					ids[ix] = tv.getId();
					names[ix] = tv.getName();
					ix++;
				}
				b.putIntArray("ids", ids);
				b.putStringArray("names", names);
				b.putInt("ix", position);
				b.putString(SeriesObjectFragment.ARG_TITLE, "'"+_query+"'...?");
				
				myIntent.putExtras(b);
				startActivity(myIntent);
			}
		}); 
		
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_BACK) {
	        finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			this.setTitle("'"+query+"'...?");
			doMySearch(query);
		}
	}
	
	private void doMySearch(final String query) {
		
		_query = query;
		
		_searchThread = new SearchThread(this,_listAdapter,new Pager() {
			private int _Total = -1;
			@Override
			public List<TvSeries> getPage(int page) {
				try {
					TvResultsPage r = api().getSearch().searchTv(query, null, page);
					_Total = r.getTotalResults();
					return r.getResults();
				} catch(Throwable t) {
					_Total = 0;
					return new ArrayList<TvSeries>();
				}
			}

			@Override
			public void start() {
			}

			@Override
			public void end() {
			}

			@Override
			int getTotal() {
				return _Total;
			}			
			
			void reset() {
				
			}
		});
		
		_searchThread.start();
		_progressObserver.add(_searchThread);
		_searchThread.release();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	        finish();
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onMenuItemSelected(int feature,MenuItem it) {
		return super.onMenuItemSelected(feature, it);
	}
	
	@Override
	protected void onDestroy() {
		clearReferences();
		super.onDestroy();
	}
	
	@Override
	protected void	onPause() {
		if(_searchThread!=null) {
			_searchThread.lock();
		}
		clearReferences();
		super.onPause();
	}
	
	@Override
	protected
	void onSaveInstanceState(Bundle outState) {
		if(_gridView!=null) {
			outState.putInt("top", _gridView.getFirstVisiblePosition());
		}
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void	onResume() {
		if(_searchThread!=null) {
			_searchThread.release();
		}
		super.onResume();
	    Environment.setCurrentActivity(this);
	}
	
	private void clearReferences(){
      Activity currActivity = Environment.getCurrentActivity();
      if (currActivity!=null && currActivity.equals(this))
            Environment.setCurrentActivity(null);
	}
}
