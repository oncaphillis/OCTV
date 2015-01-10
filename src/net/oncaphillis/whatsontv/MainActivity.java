package net.oncaphillis.whatsontv;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.oncaphillis.whatsontv.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerTitleStrip;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import info.movito.themoviedbapi.*;
import info.movito.themoviedbapi.model.tv.*;

public class MainActivity extends FragmentActivity {
	
	private ViewPager _viewPager = null;
	private MainPagerAdapter _mainPagerAdapter = null;
	
	public static final String[] Titles={"Today","On the Air","Hi Vote","Popular"};

	static public ArrayAdapter<TvSeries>[]          ListAdapters = new ArrayAdapter[Titles.length];
	
	static public HashMap<Integer,List<TvSeries>>[] StoredResults = new HashMap[Titles.length];
	
	static public SearchThread[] SearchThreads=new SearchThread[Titles.length];
	
	static public int Counts[] = new int[Titles.length];
	
	static private List<TvSeries>[] MainList = new List[Titles.length];
	
	static private Pager[] ThePager = new Pager[Titles.length];
	
	static private Bitmap _defBitmap = null;
	
	/** Spit out a simple alert dialog
	 * 
	 * @param txt message to show
	 */
	class ListPager implements Pager {
		int _type=0;
		public ListPager(int t) {
			_type = t;
		}
		
		@Override
		public List<TvSeries> getPage(int page) {

			List<TvSeries> l;
				
			synchronized(StoredResults )  {					
				if( (l=StoredResults[_type].get(page))!=null)
					return l;
			}	
			
			while(true) {
				if(!isOnline()) {
					try {
						Thread.sleep(1000);
					} catch(Exception ex1) {
					}
					continue;
				}
				
				try {
					if(_type==0)
						l=Tmdb.get().api().getTvSeries().getAiringToday(null, page);
					else if(_type==1)
						l=Tmdb.get().api().getTvSeries().getOnTheAir(null, page);
					else if(_type==2)
						l=Tmdb.get().api().getTvSeries().getTopRated(null, page);
					else
						l=Tmdb.get().api().getTvSeries().getPopular(null, page);

					if(l!=null) {
						synchronized(StoredResults )  {					
							StoredResults[_type].put(page, l);
							Counts[_type]+=l.size();
							return l;
						}
					}	
				} catch(Exception ex0) {
					try {
						Thread.sleep(1000);
					} catch(Exception ex1) {
					}
				}
			}
		}

		@Override
		public void start() {
		}

		@Override
		public void end() {
		}
		
	}
	private void checkOnline() { 
		if(!isOnline()) {
			final MainActivity a=this;
			new AlertDialog.Builder(this)
			.setTitle("Alert")
			.setMessage("We are not online")
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					a.checkOnline();
				}
			})
			.setIcon(android.R.drawable.ic_dialog_alert).show();
		}
	}
	
	/** main activity after startup
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main_pager);
		
		/*if(savedInstanceState!=null) {
			synchronized(StoredResults) {
				StoredResults = (HashMap<Integer,List<TvSeries>>[])savedInstanceState.getSerializable("savedstate");
			}
		}*/
		
		_defBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);
		
		int c = 1;
			{
				DisplayMetrics displaymetrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
				
				int width  = displaymetrics.widthPixels;
	
				if(width>400)
					c=2;
				if(width>600)
					c=3;
				if(width>700)
					c=4;
			}
			

		Bundle b = getIntent().getExtras();
		
		_mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(),this);
		
		_viewPager = (ViewPager) findViewById(R.id.main_pager_layout);
        _viewPager.setAdapter(_mainPagerAdapter);
        _viewPager.setCurrentItem(0);
        
        checkOnline();
        
        for(int i=0;i<Titles.length;i++) {
			final int j = i;
        	if(StoredResults[i]==null)
				StoredResults[i] = new HashMap<Integer,List<TvSeries>>();

        	if(MainList[i]==null)
				MainList[i] = new ArrayList<TvSeries>();
			
        	if(ListAdapters[i]==null)
				ListAdapters[i] = new TvSeriesListAdapter(this,
					android.R.layout.simple_list_item_1,MainList[i],_defBitmap,this,c);

	       SearchThreads[i] = new SearchThread(this,ListAdapters[i],ThePager[i] = new ListPager(i),null,null);
	       
	       SearchThreads[i].start();
        }
	}
	
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override 
	protected void 
	onStart() {
		super.onStart();
	}
	@Override
	protected void	onPause() {
		for(int i=0;i<SearchThreads.length;i++)  {
			SearchThreads[i].lock();
		}
		super.onPause();
	}
	
	@Override
	protected void	onResume() {
		for(int i=0;i<SearchThreads.length;i++)  {
			SearchThreads[i].release();
		}
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
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
	protected void  onSaveInstanceState (Bundle outState){
		synchronized(StoredResults) {
			outState.putSerializable("savedstate", StoredResults);
		}
		super.onSaveInstanceState (outState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView       = (SearchView) menu.findItem(R.id.search).getActionView();
	    
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    searchView.setIconifiedByDefault(true);

	    return super.onCreateOptionsMenu(menu);
	}

	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    return netInfo != null && netInfo.isConnectedOrConnecting();
	}

}
