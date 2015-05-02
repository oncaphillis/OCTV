package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import net.oncaphillis.whatsontv.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

class TvSeriesListAdapter extends ArrayAdapter<TvSeries> {

	private List<TvSeries> _list      = null; 
	private Activity       _activity  = null;

	public TvSeriesListAdapter(Context context, int resource, List<TvSeries> objects,Activity ac) {
		super(context, resource, objects);

		_list      = objects;
		_activity  = ac;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View the_view = convertView;

		boolean slim      = Environment.isSlim(_activity);
		
		if(the_view == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        the_view  = vi.inflate(R.layout.series_list_entry, null);						
		}
		
		TvSeries the_series = getItem(position);
		
		final int pos = position;
		
	    if (the_series != null && (the_view.getTag() ==null || the_view.getTag() != the_series) && Tmdb.api()!=null) {
	    	LinearLayout ll = (LinearLayout)the_view.findViewById(R.id.series_nearest_episode);
	    	
	    	ll.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent myIntent = new Intent(_activity, EpisodePagerActivity.class);
					Bundle b = new Bundle();
					
					synchronized(_list) {
						if(_list.get(pos)!=null) {
							b.putSerializable("series", _list.get(pos).getId() );
							b.putString("series_name", _list.get(pos).getName() );
							b.putBoolean("nearest",true );
							myIntent.putExtras(b);
							_activity.startActivity(myIntent);
						}
					}
				}
	    	});
	    	
	    	TextView   tt_series_title = (TextView) the_view.findViewById(R.id.series_title);
	    	TextView   tt_first_aired  = (TextView) the_view.findViewById(R.id.series_list_firstaired);
	    	
	    	TextView   tt1 = (TextView)     the_view.findViewById(R.id.series_network);
	    	TextView   ttdate   = (TextView)     the_view.findViewById(R.id.series_date);
	    	TextView   tv_clock = (TextView)  the_view.findViewById(R.id.series_airing_clock);
	    	
	    	TextView   tt3 = (TextView)     the_view.findViewById(R.id.series_last_episode);
	    	TextView   tt4 = (TextView)     the_view.findViewById(R.id.series_airing_state);
	    	
	    	TextView   tt5 = (TextView)     the_view.findViewById(slim ? R.id.series_list_entry_rating_slim : R.id.series_list_entry_rating_wide);
	    	TextView   tth = (TextView)     the_view.findViewById(slim ? R.id.series_list_entry_rating_wide : R.id.series_list_entry_rating_slim);
	    	
	    	tt5.setBackgroundColor(_activity.getResources().getColor(R.color.oncaphillis_light_grey));
	    	tt5.setText("-/-");
	    	
	    	if(!slim) {
	    		tv_clock.setVisibility(View.GONE);
	    	}
	    	
	    	tth.setVisibility(View.GONE);
	    	
	    	ImageView       ii = (ImageView)    the_view.findViewById(R.id.series_list_image);
	    	ProgressBar     pb = (ProgressBar)  the_view.findViewById(R.id.series_wait_bar);

	    	pb.setVisibility(View.INVISIBLE);

	    	ii.setImageBitmap(null);
	    	
	    	if(the_series.getPosterPath() != null) {
	    		ii.setTag(the_series.getPosterPath());
	    		Bitmap bm = null;
	    		
	    		if((bm = Tmdb.get().getCachedPoster(0,the_series.getPosterPath())) != null ) {
	    			ii.setImageBitmap(bm);
	    		} else {
	   	    		new BitmapDownloaderTask(ii,_activity,pb,null,null).execute();
	    		}
	    	} else {
    			ii.setTag(null);
    		}

	    	String fa = "XXXX"; 
	    	
	    	if(the_series.getFirstAirDate()!=null) {
	    		Calendar c = Calendar.getInstance();
	    		try {
					c.setTime( Environment.TmdbDateFormater.parse(the_series.getFirstAirDate()) );
		    		fa = Integer.toString(c.get(Calendar.YEAR));
				} catch (ParseException e) {
				}
	    	}
	    	
	    	if(the_series.getVoteCount()==0) {
	    		tt5.setTextColor(_activity.getResources().getColor(R.color.oncaphillis_light_grey));
	    		tt5.setText("-/-");
	    	} else {
	    		tt5.setTextColor(_activity.getResources().getColor(R.color.oncaphillis_orange));
	    		tt5.setText(String.format("%2.1f/%d", the_series.getVoteAverage(),10));
	    	}
	    	
	    	tt_series_title.setText( the_series.getName() + 
	    			(the_series.getOriginalName()!=null 
	    			&& !the_series.getOriginalName().equals("") 
	    			&& !the_series.getOriginalName().equals(the_series.getName()) ? " ("+the_series.getOriginalName()+")" : ""));
	    	tt_first_aired.setText( fa );
	    	
	    	if(tt1.getTag() == null || !(tt1.getTag() instanceof Integer) || !((Integer)tt1.getTag()).equals(the_series.getId()) ) {
    			tt1.setTag(new Integer(the_series.getId()));
	    	}
	    	if(ttdate.getTag() == null || !(ttdate.getTag() instanceof Integer) || !((Integer)ttdate.getTag()).equals(the_series.getId()) ) {
    			ttdate.setTag(new Integer(the_series.getId()));
	    	}
	    	if(tt3.getTag() == null || !(tt3.getTag() instanceof Integer) || !((Integer)tt3.getTag()).equals(the_series.getId()) ) {
    			tt3.setTag(new Integer(the_series.getId()));
	    	}
	    
	    	tt1.setText("...");
	    	ttdate.setText("...");
	    	tv_clock.setText("...");
	    	tt3.setText("...");
    		tt4.setText("...");
    		
    		the_view.setTag(the_series);

    		new SeriesInfoDownLoaderTask(tt1,ttdate,tv_clock,tt3,tt4,_activity).execute();
 	    }
	    return the_view;
	}
}