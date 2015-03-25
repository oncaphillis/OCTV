package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

public class EpisodePagerActivity extends FragmentActivity {

	private EpisodeCollectionPagerAdapter _episodePagerAdapter = null;
	public ViewPager _viewPager;
	private DrawerLayout _DrawerLayout;
	private ListView _DrawerList;
	private TableLayout _DrawerTable;
	private ScrollView _DrawerScrollView;
	
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
        
        TextView tv_seasons_count = ((TextView)this.findViewById(R.id.episode_seasons_count));
        
		TableLayout tl = (TableLayout) this.findViewById(R.id.episode_pager_info_table);
		LinearLayout ll = (LinearLayout) this.findViewById(R.id.episode_seasons_tree);
		
		_DrawerLayout = (DrawerLayout) findViewById(R.id.episodes_drawer_layout);
	    _DrawerTable  = (TableLayout) findViewById(R.id.episodes_drawer_table);
		_DrawerScrollView = (ScrollView) findViewById(R.id.episodes_drawer_scrollview);
	    if(Environment.getColumns(this)==1) {
			ll.setVisibility(View.GONE);
			new SeasonsInfoThread(this,_DrawerTable,1,false,series,tv_seasons_count).start();
		} else {
			_DrawerScrollView.setVisibility(View.GONE);
			new SeasonsInfoThread(this,tl,1,false,series,tv_seasons_count).start();
		}
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
