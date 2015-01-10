package net.oncaphillis.whatsontv;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class SeriesCollectionPagerAdapter extends FragmentStatePagerAdapter {
	private int[]    _ids;
	private String[] _names;
	private int      _ix;
	public SeriesCollectionPagerAdapter(FragmentManager fm,int ix,int[] ids,String[] names) {
	    super(fm);
	    _ix    = ix;
	    _ids   = ids;
		_names = names;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new SeriesObjectFragment();
        Bundle args       = new Bundle();
        args.putInt(SeriesObjectFragment.ARG_IX, i);
        args.putIntArray("ids", _ids);
        args.putStringArray("names", _names);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return _names.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "#"+(position+1)+"_"+_names[position];
    }
}