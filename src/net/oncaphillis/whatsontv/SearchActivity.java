package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.oncaphillis.whatsontv.R;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

public class SearchActivity extends Activity {

	private GridView _gridView;
	private ArrayAdapter<TvSeries> _listAdapter;
	private Bitmap                 _defBitmap;
	private List<TvSeries> _mainList = new ArrayList<TvSeries>();
	private Menu                   _menu = null;
	private SearchThread           _searchThread = null;
	private String                 _query = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
        
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
		_gridView    = (GridView)    findViewById(R.id.search_table);
		_defBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_image); 
		
	    /// Set up our special		
		_listAdapter = new TvSeriesListAdapter(this,
				android.R.layout.simple_list_item_1,_mainList,_defBitmap,this);
		
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
				TvResultsPage r = api().getSearch().searchTv(query, null, page);
				_Total = r.getTotalResults();
				return r.getResults();
			}

			@Override
			public void start() {
			}

			@Override
			public void end() {
			}

			@Override
			int getTotal() {
				// TODO Auto-generated method stub
				return _Total;
			}			
		},null,null);
		
		_searchThread.start();
		_searchThread.release();
	}
	
	@Override
	public void onResume() {
		if(_searchThread!=null) {
			_searchThread.release();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if(_searchThread!=null) {
			_searchThread.lock();
		}
		super.onPause();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
	    // Inflate the menu items for use in the action bar
	    
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView       = (SearchView) menu.findItem(R.id.search).getActionView();
	    
	    if(searchView != null) {
	    	searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    	searchView.setIconifiedByDefault(true);
	    }
	    
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
}
