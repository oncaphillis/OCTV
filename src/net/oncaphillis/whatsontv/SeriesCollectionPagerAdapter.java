package net.oncaphillis.whatsontv;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class SeriesCollectionPagerAdapter extends FragmentStatePagerAdapter {
	private int[]    _ids;
	private String[] _names;
	private int      _ix;
	private TaskObserver _progressObserver = null;
	public SeriesCollectionPagerAdapter(FragmentManager fm,int ix,int[] ids,String[] names, TaskObserver progressObserver) {
	    super(fm);
	    _ix    = ix;
	    _ids   = ids;
		_names = names;
		_progressObserver = progressObserver;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new SeriesObjectFragment(_progressObserver);
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
        return (Environment.isDebug() ? "#"+(position+1)+"_" : "" )+_names[position];
    }
}