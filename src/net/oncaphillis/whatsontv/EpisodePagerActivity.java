package net.oncaphillis.whatsontv;

import java.util.List;

import net.oncaphillis.whatsontv.SeriesInfo.SeasonNode;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class EpisodePagerActivity extends FragmentActivity {

	private EpisodeCollectionPagerAdapter _episodePagerAdapter = null;
	private ViewPager _viewPager;
	private int _series;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_episode_pager);
		
		Bundle b = getIntent().getExtras();
		
		_series = b.getInt("series");

		_episodePagerAdapter  = new EpisodeCollectionPagerAdapter(getSupportFragmentManager(),this,_series);
		
		_viewPager = (ViewPager) findViewById(R.id.series_page_layout);
        _viewPager.setAdapter(_episodePagerAdapter);
        _viewPager.setCurrentItem(0);
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
