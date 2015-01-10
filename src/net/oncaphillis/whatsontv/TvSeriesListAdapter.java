package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.oncaphillis.whatsontv.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

class TvSeriesListAdapter extends ArrayAdapter<TvSeries> {

	private List<TvSeries> _list      = null; 
	private Bitmap         _defBitmap = null;
	private Activity       _activity  = null;
	private int            _cols      = 1;
	
	/** Load a bitmap from an URL.
	 * 
	 * @param url
	 * @return Bitmap or null on failure
	 */
	

	public TvSeriesListAdapter(Context context, int resource, List<TvSeries> objects,Bitmap defBitmap,Activity ac,int cols) {
		super(context, resource, objects);
		_cols = cols;
		_defBitmap = defBitmap;
		_list      = objects;
		_activity  = ac;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		
		if(v == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v  = vi.inflate(R.layout.series_list_entry, null);						
		}
		
		TvSeries s = getItem(position);
		
		// v.setBackgroundColor(R.id.action_bar);
		
	    if (s != null && Tmdb.get().api()!=null) {
	    	TextView   tt0 = (TextView)     v.findViewById(R.id.series_title);
	    	ImageView   ii = (ImageView)    v.findViewById(R.id.series_list_image);
	    	TextView   tt_vote = (TextView)     v.findViewById(R.id.series_rating);
	    	ProgressBar pb = (ProgressBar)  v.findViewById(R.id.series_wait_bar);
	    	
	    	// v.setBackgroundColor(_activity.getResources().getColor( (position / _cols) % 2 == 0 ? 
	    	//		R.color.list_item1_color : R.color.list_item0_color));

	    	pb.setVisibility(View.INVISIBLE);
	    	
	    	if(s.getPosterPath() != null) {
		    	if(ii.getTag()==null || !ii.getTag().toString().equals(s.getPosterPath())) { 
		    		ii.setImageBitmap(_defBitmap);
		    	}
	    		ii.setTag(s.getPosterPath());
	    		new BitmapDownloaderTask(ii,_activity,pb,_defBitmap,null).execute();
    		} else {
	    		ii.setTag(null);
    			ii.setImageBitmap(_defBitmap);
    		}
    		tt0.setText( s.getName() );
	    	tt_vote.setText( String.format("%.1f", s.getVoteAverage())+"/"+Integer.toString(s.getVoteCount()));
	    }
	    return v;
	}
}