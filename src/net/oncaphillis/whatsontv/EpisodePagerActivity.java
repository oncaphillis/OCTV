package net.oncaphillis.whatsontv;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

public class EpisodePagerActivity extends FragmentActivity {

	private EpisodeCollectionPagerAdapter _episodePagerAdapter = null;
	public ViewPager _viewPager;

	private TableLayout _drawerTable;
	private ScrollView _drawerScrollView;
	private ProgressBar _progressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_episode_pager);
		Bundle b = getIntent().getExtras();
		
		_progressBar = (ProgressBar) this.findViewById(R.id.episode_load_progress);
		_progressBar.setIndeterminate(true);
		
		TaskObserver progressObserver = new TaskObserver() {
			@Override
			void beginProgress() {
				EpisodePagerActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						_progressBar.setVisibility(View.VISIBLE);
					}
				});
			}

			void endProgress() {
				EpisodePagerActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						_progressBar.setVisibility(View.GONE);
					}
				});
			}
		};

		int series = b.getInt("series");
		String series_name = b.getString("series_name");
		boolean nearest = b.getBoolean("nearest");
		series_name = series_name == null ? "" : series_name;
		
		String episodes_for = getResources().getString(R.string.episodes_for);
		this.setTitle(String.format(episodes_for,series_name));

        ActionBar actionBar = getActionBar();
	    
        if(actionBar!=null)
	    	actionBar.setDisplayHomeAsUpEnabled(true);
	    
		_episodePagerAdapter  = new EpisodeCollectionPagerAdapter(getSupportFragmentManager(),
				this,series,nearest,progressObserver);
		
		_viewPager = (ViewPager) findViewById(R.id.series_page_layout);
        _viewPager.setAdapter(_episodePagerAdapter);
        _viewPager.setCurrentItem(0);
        
        TextView tv_seasons_count = ((TextView)this.findViewById(R.id.episode_seasons_count));
        
		TableLayout tl = (TableLayout) this.findViewById(R.id.episode_pager_info_table);
		LinearLayout ll = (LinearLayout) this.findViewById(R.id.episode_seasons_tree);
		
		// _DrawerLayout = (DrawerLayout) findViewById(R.id.episodes_drawer_layout);
	    _drawerTable  = (TableLayout) findViewById(R.id.episodes_drawer_table);
		_drawerScrollView = (ScrollView) findViewById(R.id.episodes_drawer_scrollview);
		
		SeasonsInfoThread sit = null;
	    if(Environment.getColumns(this)==1) {
			ll.setVisibility(View.GONE);
			sit = new SeasonsInfoThread(this,_drawerTable,1,false,series,tv_seasons_count,progressObserver);
		} else {
			_drawerScrollView.setVisibility(View.GONE);
			sit = new SeasonsInfoThread(this,tl,1,false,series,tv_seasons_count,progressObserver);
		}
	    
	    sit.start();
	    progressObserver.add(sit);
	    
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.episode_pager, menu);
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
