package net.oncaphillis.whatsontv;

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
import android.widget.TableRow;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import info.movito.themoviedbapi.model.tv.TvSeries;

class TvSeriesListAdapter extends ArrayAdapter<TvSeries> {

	private List<TvSeries> _list      = null; 
	private Activity       _activity  = null;
	private static Integer _hiColor = null;
	private static Integer _loColor = null;
	private static boolean _slim    = false;
	public TvSeriesListAdapter(Context context, int resource, List<TvSeries> objects,Activity ac) {
		super(context, resource, objects);

		_list      = objects;
		_activity  = ac;
		_slim = Environment.isSlim(ac);
		
		if(_hiColor == null)
			_hiColor = new Integer(ac.getResources().getColor(R.color.oncaphillis_orange));
		if(_loColor == null)
			_loColor = new Integer(ac.getResources().getColor(R.color.actionbar_text_color));
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
		
	    if (the_series != null && (the_view.getTag() ==null || the_view.getTag() != the_series)) {
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

	    	TextView tvNetwork = (TextView)  the_view.findViewById(R.id.series_network);
	    	TextView tvDate    = (TextView)  the_view.findViewById(R.id.series_date);
	    	TextView tvClock   = (TextView)  the_view.findViewById(R.id.series_airing_clock);
	    	TextView tvState   = (TextView)  the_view.findViewById(R.id.series_airing_state);
	    	TextView tvEpisode = (TextView)  the_view.findViewById(R.id.series_last_episode);

	    	TableRow   tr_clock = (TableRow) the_view.findViewById(R.id.series_nearest_episode_clock_row);
	    	
	    	TextView   tt_series_title = (TextView) the_view.findViewById(R.id.series_title);
	    	TextView   tt_first_aired  = (TextView) the_view.findViewById(R.id.series_list_firstaired);
	    	TextView   tt_rating      = (TextView)  the_view.findViewById(slim ? R.id.series_list_entry_rating_slim : R.id.series_list_entry_rating_wide);
	    	TextView   tt_hide_rating = (TextView)  the_view.findViewById(slim ? R.id.series_list_entry_rating_wide : R.id.series_list_entry_rating_slim);

	    	tt_rating.setBackgroundColor(_activity.getResources().getColor(R.color.oncaphillis_light_grey));
	    	tt_rating.setText("-/-");

	    	tt_hide_rating.setVisibility(View.GONE);
	    	
	    	ImageView       ii = (ImageView)    the_view.findViewById(R.id.series_list_image);
	    	ProgressBar     pb = (ProgressBar)  the_view.findViewById(R.id.series_wait_bar);

	    	pb.setVisibility(View.INVISIBLE);

	    	ii.setImageBitmap(null);
	    	
	    	if(the_series.getPosterPath() != null) {
	    		ii.setTag(the_series.getPosterPath());
	    		Bitmap bm = null;
	    		
	    		if((bm = Tmdb.getCachedPoster(0,the_series.getPosterPath())) != null ) {
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
	    		tt_rating.setTextColor(_activity.getResources().getColor(R.color.oncaphillis_light_grey));
	    		tt_rating.setText("-/-");
	    	} else {
	    		tt_rating.setTextColor(_activity.getResources().getColor(R.color.oncaphillis_orange));
	    		tt_rating.setText(String.format("%2.1f/%d", the_series.getVoteAverage(),10));
	    	}
	    	
	    	tt_series_title.setText( the_series.getName() + 
	    			(the_series.getOriginalName()!=null 
	    			&& !the_series.getOriginalName().equals("") 
	    			&& !the_series.getOriginalName().equals(the_series.getName()) ? " ("+the_series.getOriginalName()+")" : ""));
	    	tt_first_aired.setText( fa );
	    	
	    	if(tvNetwork.getTag() == null || !(tvNetwork.getTag() instanceof Integer) || !((Integer)tvNetwork.getTag()).equals(the_series.getId()) ) {
    			tvNetwork.setTag(new Integer(the_series.getId()));
	    	}
	    	if(tvDate.getTag() == null || !(tvDate.getTag() instanceof Integer) || !((Integer)tvDate.getTag()).equals(the_series.getId()) ) {
    			tvDate.setTag(new Integer(the_series.getId()));
	    	}
	    	if(tvEpisode.getTag() == null || !(tvEpisode.getTag() instanceof Integer) || !((Integer)tvEpisode.getTag()).equals(the_series.getId()) ) {
    			tvEpisode.setTag(new Integer(the_series.getId()));
	    	}

	    	the_view.setTag(the_series);
	    	
	    	SeriesInfo.NearestNode nn = SeriesInfoDownLoader.getNearest(the_series.getId());
	    	if(nn!=null ) {
		    	if(slim && nn.hasClock) {
			    	tr_clock.setVisibility(View.VISIBLE);
			    	tr_clock.setVisibility(View.VISIBLE);
		    	} else {
			    	tr_clock.setVisibility(View.GONE);
		    	}
	    		refresh(the_view,nn,false); 
	    	} else {
		    	tr_clock.setVisibility(View.GONE);
		    	tvNetwork.setText("...");
		    	tvDate.setText("...");
		    	tvClock.setText("...");
		    	tvEpisode.setText("...");
	    		tvState.setText("...");
	    		new SeriesInfoDownLoader(the_series.getId(),the_view).execute();
	    	}
 	    }
	    return the_view;
	}
	
	static
	public void refresh(View view,SeriesInfo.NearestNode nn,boolean async) {
						
    	TextView tvNetwork = (TextView)  view.findViewById(R.id.series_network);
    	TextView tvDate    = (TextView)  view.findViewById(R.id.series_date);
    	TextView tvClock   = (TextView)  view.findViewById(R.id.series_airing_clock);
    	TextView tvState   = (TextView)  view.findViewById(R.id.series_airing_state);
    	TextView tvEpisode = (TextView)  view.findViewById(R.id.series_last_episode);
    	TableRow trClock   = (TableRow) view.findViewById(R.id.series_nearest_episode_clock_row);

		if(nn.date != null) {
			Date now = TimeTool.getNow();
			tvDate.setText(Environment.formatDate(nn.date,nn.hasClock && ! _slim ) );					
			
			if(_slim)
				if(nn.hasClock) {
					tvClock.setText(Environment.formatTime(nn.date));					
					trClock.setVisibility(View.VISIBLE);
				} else {
					tvClock.setText("...");
					trClock.setVisibility(View.GONE);
				}
			
			if(nn.date.before(now)) {
				tvDate.setTextColor(_loColor);
				if(tvState !=null)
					tvState.setText("last");
			} else {
				tvDate.setTextColor(_hiColor);
				if(tvState !=null)
					tvState.setText("next");
			}
		}
		
		if(nn.networks != null) {
			tvNetwork.setText( nn.networks );
		}
		
		String s = Integer.toString(nn.season )+"x"+Integer.toString(nn.episode);
		tvEpisode.setText( (async ? "@" : "" )+s+" "+nn.title);
	}
}