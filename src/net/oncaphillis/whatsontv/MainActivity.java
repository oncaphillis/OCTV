package net.oncaphillis.whatsontv;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.oncaphillis.whatsontv.R;
import net.oncaphillis.whatsontv.SearchThread.Current;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import info.movito.themoviedbapi.*;
import info.movito.themoviedbapi.model.core.ResponseStatusException;
import info.movito.themoviedbapi.model.tv.*;
import info.movito.themoviedbapi.tools.MovieDbException;

public class MainActivity extends FragmentActivity {
	
	private ViewPager _viewPager = null;
	
	private MainPagerAdapter _mainPagerAdapter = null;
	
	public static final String[] Titles={"Today","On the Air","Hi Vote","Popular" };

	static public ArrayAdapter<TvSeries>[]          ListAdapters = new ArrayAdapter[Titles.length];
	
	static public HashMap<Integer,List<TvSeries>>[] StoredResults = new HashMap[Titles.length];
	
	//static public Integer[] Counts = new Integer[Titles.length];
	
	static private List<TvSeries>[] MainList = new List[Titles.length];
	
	static private Pager[] ThePager = new Pager[Titles.length];
	
	static private Bitmap _defBitmap = null;

	public  SearchThread SearchThread = null;
	private MainFragment _actFragment = null;
	private ActionBarDrawerToggle _DrawerToggle = null;
	private DrawerLayout          _DrawerLayout = null; 
	private ExpandableListView    _DrawerList   = null;
	private NavigatorAdapter      _DrawerAdapter = null;
	
	/** Spit out a simple alert dialog
	 * 
	 * @param txt message to show
	 */
	abstract class CachingPager extends Pager {
		
		private HashMap<Integer,List<TvSeries>> _storage;
		private int _totalCount = -1;
		
		abstract public TvResults request(int page);
		
		public CachingPager(HashMap<Integer,List<TvSeries>> storage) {
			_storage = storage;
		}

		public List<TvSeries> getPage(int page) {

			List<TvSeries> l;
				
			synchronized( _storage  )  {					
				if( ( l = _storage.get(page))!=null)
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
					TvResults r =  request(page);
					l = r.getResults();
					_totalCount = r.getTotalResults();
					
					if(l!=null) {
						synchronized(_storage )  {					
							_storage.put(page, l);
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
		
		public void start() {
		}

		public void end() {
		}
		
		public int getTotal() {
			return _totalCount;
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

		initNavbar();
		
		try {			
			_defBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);

			Bundle b = getIntent().getExtras();
			
			_mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(),this);
			
			_viewPager = (ViewPager) findViewById(R.id.main_pager_layout);
	        _viewPager.setAdapter(_mainPagerAdapter);
	        _viewPager.setCurrentItem(0);
	        
	        checkOnline();
	        
	        for(int i=0;i< Titles.length ;i++) {
				final int j = i;
	        	if(StoredResults[i]==null)
					StoredResults[i] = new HashMap<Integer,List<TvSeries>>();
	
	        	if(MainList[i]==null)
					MainList[i] = new ArrayList<TvSeries>();
				
	        	if(ListAdapters[i]==null)
					ListAdapters[i] = new TvSeriesListAdapter(this,
						android.R.layout.simple_list_item_1,MainList[i],_defBitmap,this);
	        	
	        	final int idx = i;
	        	if(ThePager[i]==null) {
	        		ThePager[i] = new CachingPager(StoredResults[i]) {
						public TvResults request(int page) {
			        		switch(idx) {
			        		case 0:
		        				return api().getTvSeries().getAiringTodayPage(language(), page);

			        		case 1:
		        				return api().getTvSeries().getOnTheAirPage(language(),page);

			        		case 2:	
		        				return api().getTvSeries().getTopRatedPage(language(),page);

			        		default:
		        				return api().getTvSeries().getPopularPage(language(), page);
			        		}
						}

					};
	        	}
	        }

	        SearchThread = new SearchThread(this,ListAdapters,ThePager,null,null);
		        
		    final FragmentActivity a = this; 
		        
	        Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
	            public void uncaughtException(Thread th, Throwable ex) {
	            	
	            	Bundle b        = new Bundle();
	            	
	            	if(ex.getMessage()!=null)
	            		b.putString("txt1", ex.getMessage());
	            	else
	            		b.putString("txr1", " ? ? ? ");
	            	
	            	if(ex.getCause()!=null && ex.getCause().getMessage()!=null)
	            		b.putString("txt2", ex.getCause().getMessage());
	            	else
	            		b.putString("txt2", "...");
	            		
	    			Intent myIntent = new Intent(a, ErrorActivity.class);
	    			myIntent.putExtras(b);
	    			startActivity(myIntent);
	    			finish();
	            }
	        };
	        
	        SearchThread.setUncaughtExceptionHandler(h);
	        SearchThread.start();
	        
		} catch(Exception ex) {
			Intent myIntent = new Intent(this, ErrorActivity.class);
			Bundle b        = new Bundle();
				b.putString("txt", ex.getMessage()+" "+ex.getCause());
				myIntent.putExtras(b);
				startActivity(myIntent);
		} catch(Throwable ta) {			
		}
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
    	if (fragment instanceof MainFragment) {
    		synchronized(this) {
				_actFragment  = (MainFragment) fragment;
    		}
    	}
	}
	  
	private void initNavbar() {
        _DrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        _DrawerList  = (ExpandableListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        
        _DrawerList.setAdapter(_DrawerAdapter = new NavigatorAdapter(this));
        final Activity a = this;
        // Set the list's click listener
        
        _DrawerList.setOnGroupClickListener( new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				Intent myIntent = new Intent( a, AboutActivity.class);
				Bundle b        = new Bundle();
				startActivity(myIntent);
				return true;
				

			}
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
	    getActionBar().setHomeButtonEnabled(true);
	     
		 _DrawerToggle = new ActionBarDrawerToggle(
	            this,                   /* host Activity */
	            _DrawerLayout,          /* DrawerLayout object */
	            R.drawable.ic_drawer,   /* nav drawer image to replace 'Up' caret */
	            R.string.nav_bar_open,  /* "open drawer" description for accessibility */
	            R.string.nav_bar_close  /* "close drawer" description for accessibility */
	    ) {
	        @Override
	        public void onDrawerClosed(View drawerView) {
	        	super.onDrawerClosed(drawerView);
	            invalidateOptionsMenu();
	        }

	        @Override
	        public void onDrawerOpened(View drawerView) {
	        	super.onDrawerOpened(drawerView);
	            invalidateOptionsMenu();
	        }
	    };
	    
	    _DrawerLayout.post(new Runnable() {
	        @Override
	        public void run() {
	            _DrawerToggle.syncState();
	        }
	    });
	    _DrawerLayout.setDrawerListener(_DrawerToggle);
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
		SearchThread.lock();
		super.onPause();
	}
	
	@Override
	protected void	onResume() {
		SearchThread.release();
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	public boolean onMenuItemSelected(int feature,MenuItem it) {
		return super.onMenuItemSelected(feature, it);
	}
	
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        _DrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _DrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (_DrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
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
