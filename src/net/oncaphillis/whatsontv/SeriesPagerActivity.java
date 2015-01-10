package net.oncaphillis.whatsontv;

import net.oncaphillis.whatsontv.R;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

public class SeriesPagerActivity extends FragmentActivity {
	private SeriesCollectionPagerAdapter _seriesCollectionPagerAdapter;
    private ViewPager                    _viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_series_pager);
		Bundle b = getIntent().getExtras();
        _seriesCollectionPagerAdapter =
                new SeriesCollectionPagerAdapter(
                        getSupportFragmentManager(),
                        b.getInt(SeriesObjectFragment.ARG_IX),
                        b.getIntArray(SeriesObjectFragment.ARG_IDS),
                        b.getStringArray(SeriesObjectFragment.ARG_NAMES));
                        
        
        _viewPager = (ViewPager) findViewById(R.id.series_page_layout);
        _viewPager.setAdapter(_seriesCollectionPagerAdapter);
        _viewPager.setCurrentItem(b.getInt(SeriesObjectFragment.ARG_IX));

        String t;
        if( (t = b.getString(SeriesObjectFragment.ARG_TITLE))!=null) {
        	this.setTitle(t);
        }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    
	    MenuItem mi = (MenuItem) menu.findItem(R.id.search);
	    SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView(); //new SearchView(this);
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    
	    if(searchView!=null) {
	    	MenuItemCompat.setShowAsAction(mi, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
	    	MenuItemCompat.setActionView(mi, searchView );
	    	
		    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		    searchView.setIconifiedByDefault(true);
	    }

	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int feature,MenuItem it) {
		if(it.getItemId()==R.id.about) {
			Intent myIntent = new Intent(this, AboutActivity.class);
			Bundle b        = new Bundle();
			startActivity(myIntent);
			return true;
		}
		return this.onMenuItemSelected(feature, it);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		return super.onOptionsItemSelected(item);
	}
	@Override
	public boolean onSearchRequested() {
		return super.onSearchRequested();
	}
}
