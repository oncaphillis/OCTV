package net.oncaphillis.whatsontv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.oncaphillis.whatsontv.SeriesInfo.EpisodeNode;
import net.oncaphillis.whatsontv.SeriesInfo.SeasonNode;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class EpisodeCollectionPagerAdapter extends FragmentStatePagerAdapter {
	
	private List<? extends SeriesInfo.SeasonNode> _seasonList = new ArrayList();
	
	public EpisodeCollectionPagerAdapter(FragmentManager fm, EpisodePagerActivity episodePagerActivity,int series) {
		super(fm);
		final int s = series;
		final FragmentStatePagerAdapter a = this;
		final Activity act =  episodePagerActivity;
		new Thread(new Runnable() {

			@Override
			public void run() {
				List<? extends SeriesInfo.SeasonNode> l = new SeriesInfo(Tmdb.get().loadSeries(s)).getSeasonsEpisodeList();						

				synchronized(_seasonList ) {
					_seasonList = l;
				}				
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						a.notifyDataSetChanged();
					}
				});
			}
		}).start();
		
	}
	
	@Override
	public Fragment getItem(int n) {
		Fragment fragment = new EpisodeObjectFragment();
        Bundle args       = new Bundle();
        /*args.putInt(SeriesObjectFragment.ARG_IX, i);
        args.putIntArray("ids", _ids);
        args.putStringArray("names", _names);*/
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
		        	return "#"+Integer.toString(position);
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
