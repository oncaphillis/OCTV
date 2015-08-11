package net.oncaphillis.whatsontv;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for Episode Paging. Reads basic Episode and Season Info
 * of a series in background and triggers the UI as soon as possible.
 * via  notifyDataSetChanged.
 * 
 * @author kloska
 *
 */
public class EpisodeCollectionPagerAdapter extends FragmentStatePagerAdapter {
	
	private List<? extends SeriesInfo.SeasonNode> _seasonList = new ArrayList<SeriesInfo.SeasonNode>();
	private int _series;
	private TaskObserver _threadObserver;
	private String _season;
	
	public EpisodeCollectionPagerAdapter(FragmentManager fm, EpisodePagerActivity episodePagerActivity,
			int series,final boolean nearest, final int idx,TaskObserver to) {
		
		super(fm);
		_season=episodePagerActivity.getResources().getString(R.string.season);
		_threadObserver = to;
		_series = series;
		
		final FragmentStatePagerAdapter a = this;
		final EpisodePagerActivity act =  episodePagerActivity;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				SeriesInfo si = SeriesInfo.fromSeries(Tmdb.get().loadSeries(_series));						
				 
				final List<? extends SeriesInfo.SeasonNode> l = si.getSeasonsEpisodeList();
				
				final int c = nearest ? si.getNearestEpisodeCoordinate() : idx<si.getSeasonsEpisodeList().size() ? idx : si.getSeasonsEpisodeList().size()-1;
				
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						synchronized( a ) {
							_seasonList = l;
							a.notifyDataSetChanged();
						}
						act._viewPager.setCurrentItem(c);
					}
				});
			}
		});
		t.start();
		_threadObserver.add(t);
	}
	
	@Override
	public Fragment getItem(int n) {
		Fragment fragment = new EpisodeObjectFragment();
        Bundle args       = new Bundle();
        synchronized( this ) {
        	if( _seasonList != null ) {
        		if(n < _seasonList.size()) {
        			SeriesInfo.SeasonNode sn = _seasonList.get(n);
    				args.putInt("series", _series);
    				args.putInt("season", sn.getSeason());
  					if(sn instanceof SeriesInfo.EpisodeNode) {
  						args.putInt("episode", ((SeriesInfo.EpisodeNode)sn).getEpisode());
  					}
        		}
        	}
        }
        fragment.setArguments(args);
        return fragment;
    }
    
	@Override
    public CharSequence getPageTitle(int position) {
		synchronized(this) {
	        SeriesInfo.SeasonNode sn = _seasonList.get(position);
	        if(sn!=null) {
		        if(sn instanceof SeriesInfo.EpisodeNode) {
		        	SeriesInfo.EpisodeNode en = (SeriesInfo.EpisodeNode) sn;
		        	return Integer.toString(en.getSeason())+"x"+Integer.toString(en.getEpisode());
		        } else {
		        	return _season+" "+Integer.toString(sn.getSeason())+ " \u25B6";
		        }
	        }
		}
		return "???";
	}

	@Override
	public int getCount() {
		synchronized(this) {
			return _seasonList.size();
		}
	}
}
