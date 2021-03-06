package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigatorAdapter extends BaseExpandableListAdapter {
	
	// private ArrayList<String>_Array;
	
	private Activity _Activity = null;

	static final int LOGIN   = 0;
	static final int REFRESH = 1;
	static final int LISTS   = 2;
	static final int SETUP   = 3;
	static final int ABOUT   = 4;
	
	private String[] _Groups = null;
	
	private String[][] _Children  = null;
	
	private int[] _GroupImageId = new int[] {
			R.drawable.ic_action_person,
			R.drawable.refresh,
			R.drawable.ic_action_view_as_list,
			R.drawable.preferences,
			R.drawable.ic_action_about
	};
	
	private Context _Context = null;
	
	public NavigatorAdapter(Context c) {
		_Context = c;
	}

	private String[] getGroups() {
		if(_Groups == null) {
			_Groups = new String[] {
				_Context.getResources().getString(R.string.login),
				_Context.getResources().getString(R.string.refresh),
				_Context.getResources().getString(R.string.lists),
				_Context.getResources().getString(R.string.setup),
				_Context.getResources().getString(R.string.about)
			};
		}
		return _Groups;
	}
	
	private String[][] getChildren() {
		if(_Children  == null) {
			_Children  = new String[][] {
					{},
					{},
					{	_Context.getResources().getString(R.string.today),
						_Context.getResources().getString(R.string.on_the_air)
						/*,_Context.getResources().getString(R.string.hi_voted),
						_Context.getResources().getString(R.string.popular)*/
					},
					{},
					{}
			};
		}
		return _Children ;
	}
	
	@Override
	public int getGroupCount() {
		return getGroups().length;
	}

	@Override
	public int getChildrenCount(int idx) {
		return getChildren()[idx].length;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return _Groups[groupPosition];
	}

	@Override
	public Object getChild(int idxp, int idxc) {
		return getChildren()[idxp][idxc];
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
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
	    		ii1.setVisibility(View.INVISIBLE);
    		
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
		return true;
	}
}
