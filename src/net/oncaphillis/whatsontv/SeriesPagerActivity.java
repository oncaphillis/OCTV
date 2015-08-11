package net.oncaphillis.whatsontv;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class SeriesPagerActivity extends FragmentActivity {
	private SeriesCollectionPagerAdapter _seriesCollectionPagerAdapter;
    private ViewPager                    _viewPager;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_series_pager);

		final Activity act = this;

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
	   
        ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
        String t;
        if( (t = b.getString(SeriesObjectFragment.ARG_TITLE))!=null) {
        	this.setTitle(t);
        }
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int feature,MenuItem it) {
		return super.onMenuItemSelected(feature, it);
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
	public boolean onSearchRequested() {
		return super.onSearchRequested();
	}
	
	@Override
	protected void onDestroy() {
		clearReferences();
		super.onDestroy();
	}
	
	@Override
	protected void	onPause() {
		clearReferences();
		super.onPause();
	}
	
	@Override
	protected void	onResume() {
		super.onResume();
	    Environment.setCurrentActivity(this);
	}
	
	private void clearReferences(){
      Activity currActivity = Environment.getCurrentActivity();
      if (currActivity!=null && currActivity.equals(this))
            Environment.setCurrentActivity(null);
	}
}
