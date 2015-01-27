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
	
	static final int LOGIN = 0;
	static final int LISTS = 1;
	static final int ABOUT = 2;
	
	private String[] _Groups = new String[] {
			"Login","Lists","About"
	};
	
	private String[][] _Children = new String[][] {
			{},
			{"Today","On Air","Hi Vote","Popular"},
			{}
	};
	
	private int[] _GroupImageId = new int[] {
			R.drawable.ic_action_person,
			R.drawable.ic_action_view_as_list,
			R.drawable.ic_action_about
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
	    	TextView    tt  = (TextView)     v.findViewById(R.id.nav_list_text);
	    	
	    	ImageView   ii0 = (ImageView)    v.findViewById(R.id.nav_list_image);
	    	ImageView   ii1 = (ImageView)    v.findViewById(R.id.nav_group_expand);
	    
	    	ii0.setImageDrawable(_Context.getResources().getDrawable(_GroupImageId[idx]));;
	    	
	    	if(idx == LISTS) { 
	    		ii1.setVisibility(View.VISIBLE);
	    		if(isExpanded)
	    			ii1.setImageDrawable(_Context.getResources().getDrawable(R.drawable.down));
	    		else
	    			ii1.setImageDrawable(_Context.getResources().getDrawable(R.drawable.right));
	    	}
	    	else
	    		ii1.setVisibility(idx == LISTS ? View.VISIBLE : View.INVISIBLE);
    		
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
	    	TextView    tt  = (TextView)     v.findViewById(R.id.nav_list_text);
	    	ImageView   ii0 = (ImageView)    v.findViewById(R.id.nav_list_image);
	    	ImageView   ii1 = (ImageView)    v.findViewById(R.id.nav_group_expand);
	    	
	    	ii0.setVisibility(View.INVISIBLE);
	    	ii1.setVisibility(View.INVISIBLE);
	    	
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
