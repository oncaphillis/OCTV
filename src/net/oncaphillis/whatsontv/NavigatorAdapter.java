package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NavigatorAdapter extends ArrayAdapter<String> {
	private String[] _Array;
	public NavigatorAdapter(Context context, int textViewResourceId,String[] stra) {
		super(context, textViewResourceId, stra);
		_Array = stra;
	}
	
	@Override
	public View getView(int idx, View fromView, ViewGroup parent) {
		View v = fromView;
				
		if(v == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v  = vi.inflate(R.layout.nav_list_entry, null);						
		}
		
		String s = getItem(idx);
		
		// v.setBackgroundColor(R.id.action_bar);
		
	    if (s != null) {
	    	TextView   tt  = (TextView)     v.findViewById(R.id.nav_list_text);
	    	ImageView   ii = (ImageView)    v.findViewById(R.id.nav_list_image);
    		tt.setText( s );
	    }
		return v;
	}
}
