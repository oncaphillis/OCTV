package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.tv.Network;
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
	
	/** Load a bitmap from an URL.
	 * 
	 * @param url
	 * @return Bitmap or null on failure
	 */
	
	public TvSeriesListAdapter(Context context, int resource, List<TvSeries> objects,Bitmap defBitmap,Activity ac) {
		super(context, resource, objects);

		_defBitmap = defBitmap;
		_list      = objects;
		_activity  = ac;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View the_view = convertView;
		
		if(the_view == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        the_view  = vi.inflate(R.layout.series_list_entry, null);						
		}
		
		TvSeries the_series = getItem(position);
		
		// v.setBackgroundColor(R.id.action_bar);
		
	    if (the_series != null && Tmdb.get().api()!=null) {
	    	TextView   tt0 = (TextView)     the_view.findViewById(R.id.series_title);
	    	TextView   tt1 = (TextView)     the_view.findViewById(R.id.series_network);
	    	TextView   tt2 = (TextView)     the_view.findViewById(R.id.series_time);
	    	TextView   tt3 = (TextView)     the_view.findViewById(R.id.series_last_episode);
	    	
	    	ImageView       ii = (ImageView)    the_view.findViewById(R.id.series_list_image);
	    	TextView   tt_vote = (TextView) the_view.findViewById(R.id.series_rating);
	    	ProgressBar     pb = (ProgressBar)  the_view.findViewById(R.id.series_wait_bar);

	    	pb.setVisibility(View.INVISIBLE);
	    	
	    	if(the_series.getPosterPath() != null) {
		    	if(ii.getTag()==null || !ii.getTag().toString().equals(the_series.getPosterPath())) { 
		    		ii.setImageBitmap(_defBitmap);
		    	}
	    		ii.setTag(the_series.getPosterPath());
	    		new BitmapDownloaderTask(ii,_activity,pb,_defBitmap,null).execute();
    		} else {
	    		ii.setTag(null);
    			ii.setImageBitmap(_defBitmap);
    		}

	    	tt0.setText( the_series.getName() );
	    	
	    	if(tt1.getTag() == null || !(tt1.getTag() instanceof Integer) || !((Integer)tt1.getTag()).equals(the_series.getId()) ) {
    			tt1.setTag(new Integer(the_series.getId()));
	    	}

	    	if(tt2.getTag() == null || !(tt2.getTag() instanceof Integer) || !((Integer)tt2.getTag()).equals(the_series.getId()) ) {
    			tt2.setTag(new Integer(the_series.getId()));
	    	}

	    	if(tt3.getTag() == null || !(tt3.getTag() instanceof Integer) || !((Integer)tt3.getTag()).equals(the_series.getId()) ) {
    			tt3.setTag(new Integer(the_series.getId()));
	    	}

	    	tt1.setText("...");
	    	tt2.setText("...");
	    	tt3.setText("...");
    		
	    	new SeriesInfoDownLoaderTask(tt1,tt2, tt3,_activity).execute();
 
	    	tt_vote.setText( String.format("%.1f", the_series.getVoteAverage())+"/"+Integer.toString(the_series.getVoteCount()));
	    }
	    return the_view;
	}
}