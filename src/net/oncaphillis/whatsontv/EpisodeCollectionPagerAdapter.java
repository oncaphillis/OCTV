package net.oncaphillis.whatsontv;

import java.util.List;

import net.oncaphillis.whatsontv.SeriesInfo.EpisodeNode;
import net.oncaphillis.whatsontv.SeriesInfo.SeasonNode;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class EpisodeCollectionPagerAdapter extends FragmentStatePagerAdapter {
	
	private List<? extends SeriesInfo.SeasonNode> _seasonList = null;
	
	public EpisodeCollectionPagerAdapter(FragmentManager fm, EpisodePagerActivity episodePagerActivity, List<? extends SeriesInfo.SeasonNode> seasonList) {
		super(fm);
		_seasonList = seasonList;
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
        SeriesInfo.SeasonNode sn = _seasonList.get(position);
        if(sn instanceof SeriesInfo.EpisodeNode) {
        	SeriesInfo.EpisodeNode en = (SeriesInfo.EpisodeNode) sn;
        	return Integer.toString(en.getSeason())+"x"+Integer.toString(en.getEpisode());
        } else {
        	return "#"+Integer.toString(position);
        }
	}

	@Override
	public int getCount() {
		return _seasonList.size();
	}
}
