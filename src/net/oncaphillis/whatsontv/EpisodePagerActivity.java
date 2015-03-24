package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;

public class EpisodePagerActivity extends FragmentActivity {

	private EpisodeCollectionPagerAdapter _episodePagerAdapter = null;
	public ViewPager _viewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_episode_pager);
		
		Bundle b = getIntent().getExtras();
		
		int series = b.getInt("series");
		boolean nearest = b.getBoolean("nearest");
		
		_episodePagerAdapter  = new EpisodeCollectionPagerAdapter(getSupportFragmentManager(),this,series,nearest);
		_viewPager = (ViewPager) findViewById(R.id.series_page_layout);
        _viewPager.setAdapter(_episodePagerAdapter);
        _viewPager.setCurrentItem(0);
        
		TableLayout tl = (TableLayout) this.findViewById(R.id.episode_pager_info_table);
		LinearLayout ll = (LinearLayout) this.findViewById(R.id.episode_seasons_tree);
		
		if(Environment.getColumns(this)==1) {
			ll.setVisibility(View.GONE);
		}
		
		new SeasonsInfoThread(this,tl,1,false,series).start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.episode_pager, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void	onPause() {
		/*if(SearchThread!=null)
			SearchThread.lock();
		*/
		super.onPause();
	}
	
	@Override
	protected void	onResume() {
		/*if(SearchThread!=null)
			SearchThread.release();
		*/
		super.onResume();
	}

}
