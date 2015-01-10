package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import net.oncaphillis.whatsontv.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SeriesObjectFragment extends Fragment {
    public static final String ARG_IX    = "ix";
    public static final String ARG_NAMES = "names";
    public static final String ARG_IDS   = "ids";
    public static final String ARG_TITLE   = "actiontitle";
	private static final String _prefix = "<html>"+
										   " <body>";
	private static final String _postfix = "</body></html>";
	
	private Activity _activity;
	private View _rootView;
	private int  _seriesId;
	private String _seriesName;
	
	@Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

		_rootView   = inflater.inflate(R.layout.series_fragment, container, false);
        Bundle args = getArguments();

        final TableLayout info_table = ((TableLayout) _rootView.findViewById(R.id.series_page_info_table));

        _seriesId   = args.getIntArray(ARG_IDS)[args.getInt(ARG_IX)];
        _seriesName = args.getStringArray(ARG_NAMES)[args.getInt(ARG_IX)];
        String t;
        
        if( (t = args.getString(ARG_TITLE))!=null) {
        	this.getActivity().setTitle(t);
        }
        
		final WebView  overview_webview    = ((WebView) _rootView.findViewById(R.id.series_fragment_overview));
        final TextView tv_diag             = ((TextView) _rootView.findViewById(R.id.series_fragment_id));
        final TextView tv_header           = ((TextView) _rootView.findViewById(R.id.series_header));
        final TextView tv_rating           = ((TextView) _rootView.findViewById(R.id.series_page_voting));
        final TextView tv_first_aired      = ((TextView) _rootView.findViewById(R.id.series_page_first_aired));
        final TextView tv_last_aired       = ((TextView) _rootView.findViewById(R.id.series_page_last_aired));
        final TextView tv_genres           = ((TextView) _rootView.findViewById(R.id.series_page_genres));
        final ProgressBar tv_progress      = ((ProgressBar) _rootView.findViewById(R.id.series_fragment_progress));
        
        tv_header.setText(_seriesName);
        tv_diag.setText("#"+Integer.toString(_seriesId));
        overview_webview.loadData("","text/html; charset=utf-8;", "utf-8");
        
        int mc = 1;
 		
        if(_activity!=null) {
 			DisplayMetrics displaymetrics = new DisplayMetrics();
 			_activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			
 			int width  = displaymetrics.widthPixels;

			if(width>400)
				mc=2;
			if(width>600)
				mc=3;
			if(width>700)
				mc=3;
 		}
 		
 		final int maxcol = mc;
 		
 		new Thread(new Runnable() {
        	
			@Override
			public void run() {
				
				if(Tmdb.get().api()==null) {
					return;
				}
				
				TvSeries t=null;
				try {
					t = Tmdb.get().api().getTvSeries().getSeries(_seriesId,null);
				} catch (Exception ex) {
					return;
				}
				
				final TvSeries  ts = t;;
				final String    name           = ts.getName();
				final String    overview       = ts.getOverview();
				final String    poster         = ts.getPosterPath();

				/*
				List<TvSeason> tsa = ts.getSeasons();
				if(tsa!=null) {
					Iterator<TvSeason> i = tsa.iterator();
					
					while(i.hasNext()) {
						TvSeason tv_season = i.next();
						tv_season = Tmdb.get().getTvSeasons().getSeason(_id, tv_season.getSeasonNumber(), null);

						// Tmdb.get().getTvEpisodes().getEpisode(_id, tv_season.getSeasonNumber(), , language, appendToResponse)
						
						// get(_id, tv_season.getSeasonNumber(), null, null);

						List<TvEpisode> episode_list = tv_season.getEpisodes();

						if(episode_list!=null) {
							Iterator<TvEpisode> ii = episode_list.iterator();
							while(ii.hasNext()) {
								TvEpisode tv_episode = ii.next();
							}
						}
					}
				}
 				*/
				
				// Load main image and HTML code
				//
				
				final Bitmap bm = Tmdb.get().loadPoster(1, ts.getPosterPath());
				
				if(_activity!=null) {
					_activity.runOnUiThread(new Runnable() {
				        @Override
				        public void run() {
				        	if(ts!=null) {
					        	List<Genre> gl = ts.getGenres();
					        	if(gl!=null) {
					        		Iterator<Genre> it = gl.iterator();
					        		int i=0;
					        		String gs = "";
					        		while(it.hasNext()) {
					        			gs += (i++==0 ? "" : ",")+it.next().getName();
					        		}
					        		tv_genres.setText(gs);
					        	}
					        	
					        	String fa = ts.getFirstAirDate();
					        	if(fa != null)
					        		tv_first_aired.setText("first aired:"+fa);
					        	
					        	String la = ts.getLastAirDate();

					        	if(la!=null)
					        		tv_last_aired.setText("last aired:"+la);

					        	if(ts.getVoteCount()!=0)
					        		tv_rating.setText(String.format("%.1f",ts.getVoteAverage())+"/"+Integer.toString(ts.getVoteCount()) );
					        	
					        	tv_diag.setText("#"+Integer.toString(_seriesId)+((poster==null) ? " \u2205" : "\u753b"));
					        	tv_header.setText(name);
				        	
					        	
					        	overview_webview.loadData(_prefix+getBitmapHtml(bm)+
					    				StringEscapeUtils.escapeHtml4(overview) +  
					        			_postfix, "text/html; charset=utf-8;", "UTF-8");

				        	} 
				        }
				    });
				
			
					// Credits
					//
					
					final String[] info_type={"Cast","Crew","Creator","Seasons"};
					Credits c;
					try {
						c = Tmdb.get().api().getTvSeries().getCredits(ts.getId(),null);
					} catch(Exception ex) {
						return;
					}
					final Credits   cr = c;
					
					if(cr!=null ){
						_activity.runOnUiThread(new Runnable() {
					    	@Override
						    public void run() {		
					    		int img_count =  cr.getCast().size()+cr.getCrew().size()+ts.getCreatedBy().size();
							    tv_progress.setTag(new Integer(img_count));

							    if(img_count==0) {
							    	tv_progress.setVisibility(View.INVISIBLE);
							    }

							    final LayoutInflater vi = LayoutInflater.from(info_table.getContext());
									
					    		for(int person_group=0;person_group<3;person_group++) {
							        
									Iterator<? extends Person> it = 
										(person_group == 0 ? cr.getCast().iterator() : 
											person_group==1 ? cr.getCrew().iterator() :
												ts.getCreatedBy().iterator());
		
									int cc=0;
									TableRow  tr=null;

									if(it.hasNext()) {
										tr = new TableRow(info_table.getContext());
								        LinearLayout header = (LinearLayout) vi.inflate(R.layout.series_table_header,null);
								        TextView txt = (TextView)header.findViewById(R.id.series_table_header_text);
								        tr.addView(header);
								        
								        TableRow.LayoutParams params = (TableRow.LayoutParams)header.getLayoutParams();
								        params.span = maxcol;
								        header.setLayoutParams(params);
								        
								        txt.setText(info_type[person_group]);
								        info_table.addView(tr);
									}
		
									while(it.hasNext()) {
										if(cc>=maxcol)
											cc=0;
										
										if(cc==0) {
											tr = new TableRow(info_table.getContext());
											tr.setOrientation(LinearLayout.HORIZONTAL);
											info_table.addView(tr);
										}
	
										String nn;
										String rr;
										String pp;
										
										if(person_group==0) {
											PersonCast p = (PersonCast)it.next();
											nn = p.getName();
											rr = p.getCharacter();
											pp = p.getProfilePath();
										} else if(person_group==1) {
											PersonCrew p = (PersonCrew)it.next();
											nn = p.getName();
											rr = p.getJob();
											pp = p.getProfilePath();
										} else  {
											Person p = (Person)it.next();
											nn = p.getName();
											rr = null;
											pp = p.getProfilePath();
										}
										
										View v = (View)vi.inflate(R.layout.series_info_grid_entry,null);
							        	
								        TextView       tt0 = (TextView)     v.findViewById(R.id.creator_name);
								        TextView       tt1 = (TextView)     v.findViewById(R.id.person_role);
									    ImageView       ii = (ImageView)    v.findViewById(R.id.creator_image);
									    ProgressBar     pb = (ProgressBar)  v.findViewById(R.id.creator_image_progress);
									    	
									    pb.setVisibility(View.INVISIBLE);
									    
									    ii.setTag(pp);
									    new BitmapDownloaderTask(ii, _activity, pb, null,tv_progress).execute();
				
									    tt0.setText(nn == null ? "" : nn);
									    tt1.setText(rr == null ? "" : rr);
								      
									    tr.addView(v);
									    cc++;
									}
					    		}
					    	}
						});
					}				
	        	}		
			}
 		
 		}).start();

        return _rootView;
    }
	
	@Override
	public void onAttach(Activity act) {
        _activity = act;
        super.onAttach(act);
	}
	private String getBitmapHtml(Bitmap bm) {
		if(bm==null)
			return "";
		
	    // Convert bitmap to Base64 encoded image for web
	    
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
	    byte[] byteArray = byteArrayOutputStream.toByteArray();
	    String imgageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
	    
	    return "<img style='padding-right:0.5cm;padding-bottom:0.5cm;float:left;' src='"+
	    		"data:image/png;base64," + imgageBase64+"' />";
	}
}