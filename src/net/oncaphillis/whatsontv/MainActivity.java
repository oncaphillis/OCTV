package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.tv.TvSeries;



public class MainActivity extends FragmentActivity {
	
	private ViewPager _viewPager = null;

	private MainPagerAdapter _mainPagerAdapter = null;
	
	static private Pager[] ThePager = null;
	static private NetWatchdog _watchDog = null;

	public  SearchThread SearchThread = null;
	private ActionBarDrawerToggle _DrawerToggle = null;
	private DrawerLayout          _DrawerLayout = null; 
	private ExpandableListView    _DrawerList   = null;

	public SharedPreferences Preferences;
	
	class SeriesStorage extends HashMap<Integer,List<TvSeries>> {
		@Override
		public List<TvSeries> put(Integer key, List<TvSeries> value) {
			return super.put(key,value);
		}
		@Override
		public List<TvSeries> remove(Object key) {
			return super.remove(key);
		}
		@Override
		public void clear() {
			super.clear();
		}
	};
	
	abstract class CachingPager extends Pager {
		
		private Map<Integer,List<TvSeries>> _storage;
		private int _totalCount = -1;
		
		abstract public TvResultsPage request(int page) throws Exception;
		
		public CachingPager(Map<Integer,List<TvSeries>> storage) {
			_storage = storage;
		}

		public List<TvSeries> getPage(int page) {

			List<TvSeries> series_list = new ArrayList<TvSeries>();
				
			synchronized( _storage  )  {					
				if( ( series_list = _storage.get(page))!=null)
					return series_list;
			}	
			
			while(true) {
				try {
					TvResultsPage r =  request(page);
					
					if(r==null) {
						return series_list;
					}

					series_list = r.getResults();
					_totalCount = r.getTotalResults();
					
					if(series_list!=null) {
						synchronized(_storage )  {					
							_storage.put(page, series_list);
							return series_list;
						}
					}
					
				} catch(Exception ex0) {
					try {
						Thread.sleep(1000);
						String ss = ex0.getMessage();
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
		
		public void reset() {
			synchronized(_storage) {
				_storage.clear();
				_totalCount = -1;
			}
		}
	}

	/** main activity after startup
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(_watchDog == null || _watchDog.getState() == Thread.State.TERMINATED) {
			(_watchDog = new NetWatchdog()).start();
		}
		
		try {			
					
			Environment.init(this);

			if(ThePager==null)
				ThePager = new Pager[Environment.Titles.length];

			setContentView(R.layout.activity_main_pager);

			Preferences = getPreferences(MODE_PRIVATE);
			
			initNavbar();

			_mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(),this);
			_viewPager = (ViewPager) findViewById(R.id.main_pager_layout);
	        _viewPager.setAdapter(_mainPagerAdapter);
	        _viewPager.setCurrentItem(0);

	        for(int i=0;i< Environment.Titles.length ;i++) {
				final int j = i;
	        	if(Environment.StoredResults[i]==null)
	        		Environment.StoredResults[i] = new SeriesStorage();
	
	        	if(Environment.MainList[i]==null)
	        		Environment.MainList[i] = new ArrayList<TvSeries>();
				
	        	if(Environment.ListAdapters[i]==null)
	        		Environment.ListAdapters[i] = new TvSeriesListAdapter(this,
						android.R.layout.simple_list_item_1,Environment.MainList[i],this);
	        	
	        	final int idx = i;
	        	
	        	if(ThePager[i]==null) {
	        		ThePager[i] = new CachingPager(Environment.StoredResults[i]) {
						public TvResultsPage request(int page) throws Exception {
			        		switch(idx) {
			        		case Environment.AIRING_TODAY:
		        				return Tmdb.getAiringToday(Tmdb.getLanguage(), page,Tmdb.getTimezone());

			        		case Environment.ON_THE_AIR:
			        			return Tmdb.getOnTheAir(Tmdb.getLanguage(),page);
			        			
			        		case Environment.HI_VOTED:	
			        			return Tmdb.getTopRated(Tmdb.getLanguage(),page);

			        		case Environment.POPULAR:	
			        			return Tmdb.getPopular(Tmdb.getLanguage(), page);

			        		default:
			        			return null;
			        		}
						}
					};
	        	}
	        }
	        
	        if(savedInstanceState==null)
	        	refresh(false);
	        
		} catch(Exception ex) {
			Intent myIntent = new Intent(this, ErrorActivity.class);
			Bundle b        = new Bundle();
			b.putString("txt1", ex.getMessage()+" "+ex.getCause());
			myIntent.putExtras(b);
			startActivity(myIntent);
			this.finish();
		} catch(Throwable ta) {
			Intent myIntent = new Intent(this,ErrorActivity.class);
			Bundle b = new Bundle();
			b.putString("txt1", ta.getMessage()+" "+ta.getCause());
			myIntent.putExtras(b);
			startActivity(myIntent);
			this.finish();
		}
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
	}
	  
	private void refresh(boolean clearCash) {

		if(clearCash) {
			for(ArrayAdapter<TvSeries> a : Environment.ListAdapters) {
				a.clear();
				a.notifyDataSetChanged();
			}
	
			for(Pager p : ThePager) {
				p.reset();
			}
		}
		
		SearchThread = new SearchThread(this,Environment.ListAdapters,ThePager);
	    final FragmentActivity a = this; 

	    Thread.UncaughtExceptionHandler searchThreadExceptionHandler = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
        		final Throwable _ex = ex;
            	
            	a.runOnUiThread(new Runnable() {
					@Override
					public void run() {
		            	Bundle b        = new Bundle();
		            	
		            	if(_ex.getMessage()!=null)
		            		b.putString("txt1", _ex.getMessage());
		            	else
		            		b.putString("txr1", " ? ? ? ");
		            	
		            	if(_ex.getCause()!=null && _ex.getCause().getMessage()!=null)
		            		b.putString("txt2", _ex.getCause().getMessage());
		            	else
		            		b.putString("txt2", "...");
		            		
		    			Intent myIntent = new Intent(a, ErrorActivity.class);
		    			myIntent.putExtras(b);
		    			a.startActivity(myIntent);
		    			finish();
					}
            	});
            }
        };
        SearchThread.setUncaughtExceptionHandler(searchThreadExceptionHandler);
        SearchThread.start();
	}
	
	private void initNavbar() {
        _DrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        _DrawerList  = (ExpandableListView) findViewById(R.id.left_drawer);
        _DrawerList.setGroupIndicator(null);
        
        // Set the adapter for the list view
        
        _DrawerList.setAdapter( new NavigatorAdapter(this)) ;
        final MainActivity activity = this;
        // Set the list's click listener
        _DrawerList.setOnGroupClickListener( new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				if(groupPosition==NavigatorAdapter.ABOUT) {
					Intent myIntent = new Intent( activity, AboutActivity.class);
					Bundle b        = new Bundle();
			        _DrawerLayout.closeDrawers();
					startActivity(myIntent);
					return true;
				} else if(groupPosition==NavigatorAdapter.SETUP) {
					Intent myIntent = new Intent( activity, SetupActivity.class);
					Bundle b        = new Bundle();
			        _DrawerLayout.closeDrawers();
					startActivity(myIntent);
					return true;
				} else if(groupPosition==NavigatorAdapter.REFRESH) {
			        _DrawerLayout.closeDrawers();
			        activity.refresh(true);
			        SearchThread.release();
			        return true;
				} else if(groupPosition==NavigatorAdapter.LOGIN) {
					Intent myIntent = new Intent( activity, LoginActivity.class);
					Bundle b        = new Bundle();
			        _DrawerLayout.closeDrawers();
					startActivity(myIntent);
					return true;
				}				
				return false;
			}
        });

        _DrawerList.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				if(groupPosition == NavigatorAdapter.LISTS) {
			        _DrawerLayout.closeDrawers();
					activity._viewPager.setCurrentItem(childPosition);
					return true;
				}
				return false;
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

	@Override
	protected void onDestroy() {
		clearReferences();
		super.onDestroy();
	}
	
	@Override 
	protected void 
	onStart() {
		super.onStart();
	}
	@Override
	protected void	onPause() {
		clearReferences();
		if(SearchThread!=null)
			SearchThread.lock();
		super.onPause();
	}
	
	@Override
	protected void	onResume() {
		if(SearchThread!=null)
			SearchThread.release();
		super.onResume();
	    Environment.setCurrentActivity(this);
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
        if(_DrawerToggle!=null)
        	_DrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(_DrawerToggle!=null)
        	_DrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        
    	if (_DrawerToggle!=null && _DrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

	@Override
	protected void  onSaveInstanceState (Bundle outState){
		synchronized(Environment.StoredResults) {
			outState.putSerializable("savedstate", Environment.StoredResults);
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
	private void clearReferences(){
      Activity currActivity = Environment.getCurrentActivity();
      if (currActivity!=null && currActivity.equals(this))
            Environment.setCurrentActivity(null);
	}
}
