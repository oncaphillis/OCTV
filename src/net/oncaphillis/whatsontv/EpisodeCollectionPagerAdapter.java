package net.oncaphillis.whatsontv;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class EpisodeCollectionPagerAdapter extends FragmentStatePagerAdapter {
	
	public EpisodeCollectionPagerAdapter(FragmentManager fm, EpisodePagerActivity episodePagerActivity) {
		super(fm);
	}


	@Override
	public Fragment getItem(int arg0) {
        Fragment fragment = new EpisodeObjectFragment();
        Bundle args       = new Bundle();
        /*args.putInt(SeriesObjectFragment.ARG_IX, i);
        args.putIntArray("ids", _ids);
        args.putStringArray("names", _names);*/
        fragment.setArguments(args);
        return fragment;
    }

	@Override
	public int getCount() {
		return 100;
	}
}
