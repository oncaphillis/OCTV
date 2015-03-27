package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/** Fills a TableLayout with all data from all seasons
 * of a given Series.
 * 
 * @author kloska
 *
 */

class SeasonsInfoThread extends Thread {


	private Activity _activity;
	private TvSeries _series = null;
	private TableLayout _table; 
	private int _maxcol = 1;
	private int _seriesN = -1;
	private TextView _count = null;
	private boolean _withHeader = false;
	private TaskObserver _taskObserver = null;
	
 	class InfoNode {
		public TableRow row = null;
		public ArrayList<ImageView>   img = new ArrayList<ImageView>();
		public ArrayList<ProgressBar> pb  = new ArrayList<ProgressBar>();
		public ArrayList<TextView>    date = new ArrayList<TextView>();
		public ArrayList<TextView>    name = new ArrayList<TextView>();

		public ArrayList<TvSeason>    se  = new ArrayList<TvSeason>();
 	};
 	
 	SeasonsInfoThread(Activity ac,TableLayout tl, int mc, boolean withHeader, TvSeries ts,TaskObserver obs) {
 		super();
 		_activity = ac;
 		_maxcol = mc;
 		_series = ts;
 		_table = tl; 
 		_withHeader = withHeader;
 		_taskObserver = obs;
 	}
 	
 	SeasonsInfoThread(Activity ac,TableLayout tl, int mc, boolean withHeader,int s0, TextView tv_seasons_count,TaskObserver obs) {
 		super();
 		_activity = ac;
 		_maxcol = mc;
 		_series =null;
 		_table = tl; 
 		_seriesN = s0;
 		_withHeader = withHeader;
 		_count =  tv_seasons_count;
 		_taskObserver = obs;
 	}

 	@Override
 	public void run() {

 		if(_series==null) {
			_series = Tmdb.get().loadSeries(_seriesN);
		}
		
		if(_series==null)
			return;

		final List<TvSeason> tsa = _series.getSeasons();

		if(tsa!=null) {
			
			_activity.runOnUiThread(new Runnable() {
		    	@Override
			    public void run() {		
	    			final LayoutInflater vi = LayoutInflater.from(_table.getContext());
	    			LinearLayout header = null;
	    			
	    			String seasons_count = _activity.getResources().getString(R.string.seasons_count);
	    			
	    			if(_count!=null) {
	    				_count.setText(String.format(seasons_count, tsa.size()));
	    			}
	    			
	    			if(tsa.size()==0)
	    				return;
	    			
	    			if(_withHeader) {
		    			header = (LinearLayout) vi.inflate(R.layout.series_table_header,null);
			        
		    			TextView txt = (TextView)header.findViewById(R.id.series_table_header_text);
		    			TableRow tr  = new TableRow(_table.getContext());
		    			tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		    			tr.addView(header);
			        
		    			TableRow.LayoutParams params = (TableRow.LayoutParams)header.getLayoutParams();
		    			params.span = _maxcol;
		    			header.setLayoutParams(params);
			        
		    			txt.setText(Integer.toString(tsa.size())+" Seasons");
		    			_table.addView(tr);
		    		}
		    		
				    int cc = 0;
				    
				    ArrayList<InfoNode> trl = new ArrayList<InfoNode>();

				    Iterator<TvSeason> i = tsa.iterator();
				    
				    TableRow tr = null;
				    
		    		while(i.hasNext()) {
		    			if(cc >= _maxcol) {
		    				cc = 0;
		    			}
		    			 
		    			if(cc == 0) {					    				
		    				tr = new TableRow(_table.getContext());
		    				tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
							tr.setOrientation(LinearLayout.HORIZONTAL);
							_table.addView(tr);
							tr.setVisibility(View.GONE);
							InfoNode nd=new InfoNode();
							nd.row = tr;
							trl.add(nd);
		    			}
				        
		    			View v = (View)vi.inflate(R.layout.season_info_grid_entry,null);
				        
		    			TvSeason tv_season = i.next();
		    										    			
	    				TextView       date = (TextView)     v.findViewById(R.id.season_date);
	    				TextView       name = (TextView)     v.findViewById(R.id.season_name);

	    				ImageView       ii = (ImageView)    v.findViewById(R.id.season_image);
	    				ProgressBar     pb = (ProgressBar)  v.findViewById(R.id.season_image_progress);

	    				setText(date,name,tv_season);
	    				
					    	
				    	ii.setTag(tv_season.getPosterPath());
	
				    	trl.get(trl.size()-1).img.add(ii);
				    	trl.get(trl.size()-1).pb.add(pb);
				    	trl.get(trl.size()-1).date.add(date);
				    	trl.get(trl.size()-1).name.add(name);

				    	trl.get(trl.size()-1).se.add(tv_season);
				    										    	
				    	tr.addView(v);
		    			
					    cc++;
		    		}
		    		
		    		final List<InfoNode> ftrl = trl;
		    		if (header!=null) {
		    			header.setOnClickListener(new OnClickListener() {
		    				private boolean expanded = false;
		    				@Override
							public void onClick(View v) {
		    					expanded = expanded ? false : true; 
								ImageView iv = (ImageView)((LinearLayout)v).findViewById(R.id.series_table_header_expand);
								iv.setImageDrawable(_activity.getResources().getDrawable(expanded ? R.drawable.down : R.drawable.right));
								loadData(ftrl);
							}
				    	});
		    		} else {
						loadData(ftrl);
		    		}
		    	}
		    	
		    	// Lazy load the data for the table rows.
		    	// displaying the seasons data.
		    	
		    	private void loadData(List<InfoNode> ftrl) {

		    		Iterator<InfoNode> i = ftrl.iterator();

		    		while(i.hasNext()) {
						InfoNode n = i.next();
						if(n.row.getVisibility()!=View.VISIBLE) {
							n.row.setVisibility(View.VISIBLE);
							if(n.img!=null) {
								Iterator<ImageView> i2 = n.img.iterator();
								for(int j = 0;j<n.img.size();j++) {
									BitmapDownloaderTask b = new BitmapDownloaderTask(n.img.get(j), _activity, n.pb.get(j), null,null);
									b.execute();
									
									if(_taskObserver != null)
										_taskObserver.add(b);
									
									final TextView _t0 = n.date.get(j);
									final TextView _t1 = n.name.get(j);
									final int _si = _series.getId();
									new AsyncTask<TvSeason,Void,TvSeason>() {
										
										@Override
										protected TvSeason doInBackground(
											TvSeason... params) {
											if(params.length>0) {
												TvSeason s =  Tmdb.get().loadSeason(_si, params[0].getSeasonNumber());
												return s;
											}
											else
												return null;
										}
										@Override

										protected void onPostExecute(TvSeason result) {
											if(result!=null && result.getEpisodes() != null) {
												setText(_t0,_t1,result);
											}
										}
									}.execute(n.se.get(j));
								}
								n.img=null;
								n.pb=null;
							}
						} else {
							n.row.setVisibility(View.GONE);
						}
					}
		    	}
		    	
		    	// Fill a pair of TextViews with data from a 
		    	// TvSeason object.
		    	
				private void setText(TextView date, TextView name,
						TvSeason tv_season) {
					
    				String s=tv_season.getAirDate();
    				if(s!=null) {
	    				try {
							Date d = Environment.TmdbDateFormater.parse(tv_season.getAirDate());
							s=Environment.DateFormater.format(d);
	    				} catch (ParseException e) {
						}
    				} else {
    					s="";
    				}
				    date.setText(s + " " + (tv_season.getEpisodes() != null && !tv_season.getEpisodes().isEmpty() ? 
			    			" "+Integer.toString(tv_season.getEpisodes().size())+" Episodes" : "") );
				    name.setText(tv_season.getName()!=null ? tv_season.getName() : "");
				}
			});
		}
	}
}