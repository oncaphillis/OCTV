package net.oncaphillis.whatsontv;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MainPagerAdapter extends FragmentStatePagerAdapter {
	private MainActivity _activity = null;
	private int _cols;
	public MainPagerAdapter(FragmentManager fm, MainActivity mainActivity) {
	    super(fm);
	    _activity = mainActivity;
	}

    @Override
    public android.support.v4.app.Fragment getItem(int i) {
        MainFragment fragment = new MainFragment();
        Bundle args       = new Bundle();
        args.putInt("idx", i);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return Environment.Titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Environment.Titles[position];
    }
}
