package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.tv.Network;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.oncaphillis.whatsontv.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

class TvSeriesListAdapter extends ArrayAdapter<TvSeries> {

	private List<TvSeries> _list      = null; 
	private Bitmap         _defBitmap = null;
	private Activity       _activity  = null;
	
	private static SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy");
	
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
		
		
	    if (the_series != null && Tmdb.get().api()!=null) {
	    	LinearLayout ll = (LinearLayout)the_view.findViewById(R.id.series_nearest_episode);
	    	
	    	ll.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent myIntent = new Intent(_activity, EpisodePagerActivity.class);
					Bundle b = new Bundle();
					
					/* synchronized(MainActivity.ListAdapters[_idx]) {
						int[]    ids   = new int[MainActivity.ListAdapters[_idx].getCount()];
						String[] names = new String[MainActivity.ListAdapters[_idx].getCount()];
						
						for(int ix=0;ix<MainActivity.ListAdapters[_idx].getCount();ix++) {
							ids[ix]   = MainActivity.ListAdapters[_idx].getItem(ix).getId();
							names[ix] = MainActivity.ListAdapters[_idx].getItem(ix).getName();
						}
						
						b.putString(SeriesObjectFragment.ARG_TITLE, MainActivity.Titles[_idx]);
						b.putIntArray("ids", ids);
						b.putStringArray("names", names);
						b.putInt("ix", position);
					*/
						myIntent.putExtras(b);
						_activity.startActivity(myIntent);
					// }
				}
	    	});
	    	
	    	TextView   tt_series_title = (TextView) the_view.findViewById(R.id.series_title);
	    	TextView   tt_first_aired  = (TextView) the_view.findViewById(R.id.series_list_firstaired);
	    	
	    	TextView   tt1 = (TextView)     the_view.findViewById(R.id.series_network);
	    	TextView   tt2 = (TextView)     the_view.findViewById(R.id.series_time);
	    	TextView   tt3 = (TextView)     the_view.findViewById(R.id.series_last_episode);
	    	TextView   tt4 = (TextView)     the_view.findViewById(R.id.series_airing_state);
	    	
	    	ImageView       ii = (ImageView)    the_view.findViewById(R.id.series_list_image);
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

	    	String fa = "XXXX"; 
	    	
	    	if(the_series.getFirstAirDate()!=null) {
	    		Calendar c = Calendar.getInstance();
	    		try {
					c.setTime( Tmdb.DateFormater.parse(the_series.getFirstAirDate()) );
		    		fa = Integer.toString(c.get(Calendar.YEAR));
				} catch (ParseException e) {
				}
	    	}
	    	
	    	tt_series_title.setText( the_series.getName() );
	    	tt_first_aired.setText( fa );
	    	
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
    		tt4.setText("...");
    		
	    	new SeriesInfoDownLoaderTask(tt1,tt2, tt3,tt4,_activity).execute();
 	    }
	    return the_view;
	}
}