package net.oncaphillis.whatsontv;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class EpisodeCollectionPagerAdapter extends FragmentStatePagerAdapter {
	
	private List<? extends SeriesInfo.SeasonNode> _seasonList = new ArrayList<SeriesInfo.SeasonNode>();
	private int _series;
	
	public EpisodeCollectionPagerAdapter(FragmentManager fm, EpisodePagerActivity episodePagerActivity,
			int series,boolean nearest) {
		super(fm);
		_series = series;
		final boolean near = nearest;
		final FragmentStatePagerAdapter a = this;
		final EpisodePagerActivity act =  episodePagerActivity;
		new Thread(new Runnable() {

			@Override
			public void run() {
				SeriesInfo si = new SeriesInfo(Tmdb.get().loadSeries(_series));						
				 
				List<? extends SeriesInfo.SeasonNode> l = si.getSeasonsEpisodeList();
				
				final int c = near ? si.getNearestEpisodeCoordinate() : 0;
				
				synchronized(_seasonList ) {
					_seasonList = l;
				}				
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						a.notifyDataSetChanged();
						act._viewPager.setCurrentItem(c);
					}
				});
			}
		}).start();
	}
	
	@Override
	public Fragment getItem(int n) {
		Fragment fragment = new EpisodeObjectFragment();
        Bundle args       = new Bundle();
        if( _seasonList != null ) {
        	synchronized( _seasonList ) {
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
		synchronized(_seasonList) {
	        SeriesInfo.SeasonNode sn = _seasonList.get(position);
	        if(sn!=null) {
		        if(sn instanceof SeriesInfo.EpisodeNode) {
		        	SeriesInfo.EpisodeNode en = (SeriesInfo.EpisodeNode) sn;
		        	return Integer.toString(en.getSeason())+"x"+Integer.toString(en.getEpisode());
		        } else {
		        	return "#"+Integer.toString(sn.getSeason());
		        }
	        }
		}
		return "???";
	}

	@Override
	public int getCount() {
		synchronized(_seasonList) {
			return _seasonList.size();
		}
	}
}
