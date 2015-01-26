package net.oncaphillis.whatsontv;

import java.util.ArrayList;

import info.movito.themoviedbapi.model.tv.TvSeries;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class NavigatorAdapter extends BaseExpandableListAdapter {
	
	private ArrayList<String>_Array;
	
	private final int LOGIN = 0;
	private final int LISTS = 1;
	private final int ABOUT = 2;
	
	private String[] _Groups = new String[] {
			"Login","Lists","About"
	};
	
	private String[][] _Children = new String[][] {
			{},
			{"Today","On Air","Hi Vote","Popular"},
			{}
	};
	
	private String    _Child = "Child";
	private Context _Context = null;
	
	public NavigatorAdapter(Context c) {
		_Context = c;
	}

	@Override
	public int getGroupCount() {
		return _Groups.length;
	}

	@Override
	public int getChildrenCount(int idx) {
		// TODO Auto-generated method stub
		return _Children[idx].length;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return _Groups[groupPosition];
	}

	@Override
	public Object getChild(int idxp, int idxc) {
		// TODO Auto-generated method stub
		return _Children[idxp][idxc];
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int idx, boolean isExpanded,
			View fromView, ViewGroup parent) {

		View v = fromView;
		
		if(v == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(_Context);
	        v  = vi.inflate(R.layout.nav_list_entry, null);						
		}
		
		String s = (String)getGroup(idx);

		if (s != null) {
	    	TextView   tt  = (TextView)     v.findViewById(R.id.nav_list_text);
	    	ImageView   ii = (ImageView)    v.findViewById(R.id.nav_list_image);
    		tt.setText( s );
	    }
		return v;
	}

	@Override
	public View getChildView(int idxp, int idxc,
			boolean isLastChild, View fromView, ViewGroup parent) {
		View v = fromView;
		
		if(v == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(_Context);
	        v  = vi.inflate(R.layout.nav_list_entry, null);						
		}
		
		String s = (String)getChild(idxp,idxc);

		if (s != null) {
	    	TextView   tt  = (TextView)     v.findViewById(R.id.nav_list_text);
	    	ImageView   ii = (ImageView)    v.findViewById(R.id.nav_list_image);
    		tt.setText( s );
	    }
		return v;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}
}
