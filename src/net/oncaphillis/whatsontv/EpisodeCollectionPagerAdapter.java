package net.oncaphillis.whatsontv;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class EpisodeCollectionPagerAdapter extends FragmentStatePagerAdapter {
	
	public EpisodeCollectionPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int arg0) {
		return null;
	}

	@Override
	public int getCount() {
		return 100;
	}
}
