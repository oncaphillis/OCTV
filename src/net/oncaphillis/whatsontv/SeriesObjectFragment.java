package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import net.oncaphillis.whatsontv.R;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SeriesObjectFragment extends EntityInfoFragment {
    public static final String ARG_IX    = "ix";
    public static final String ARG_NAMES = "names";
    public static final String ARG_IDS   = "ids";
    public static final String ARG_TITLE   = "actiontitle";
    
    private static final String _prefix = "<html>"+
										   " <body style='background-color: #000000; color: #ffffff'>";
	private static final String _postfix = "</body></html>";
	

	private View _rootView;
	private int  _seriesId;
	private String _seriesName;
	
 	class InfoNode {
		public TableRow row = null;
		public ArrayList<ImageView>   img = new ArrayList();
		public ArrayList<ProgressBar> pb  = new ArrayList();
	};
	
	@Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

		_rootView   = inflater.inflate(R.layout.series_fragment, container, false);
        Bundle args = getArguments();

        
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
        tv_progress.setVisibility(View.INVISIBLE);
        
        int mc = Environment.getColumns(this.getActivity());
 		
 		final int maxcol = mc;
 		
 		new Thread(new Runnable() {
        	
			@Override
			public void run() {
				
				if(Tmdb.get().api()==null) {
					return;
				}
				
				TvSeries series=null;
				List<TvSeason> seasons = null;
				try {
					series = Tmdb.get().loadSeries(_seriesId);
					seasons = new SeriesInfo(series).getSeasons();
					
				} catch (Exception ex) {
					return;
				}
				
				final TvSeries  ts = series;;
				final String    name           = ts.getName();
				final String    overview       = ts.getOverview();
				final String    poster         = ts.getPosterPath();

				
				// Load main image and HTML code
				//
				
				final Bitmap bm = Tmdb.get().loadPoster(1, ts.getPosterPath());
				final TableLayout  info_table = ((TableLayout) _rootView.findViewById(R.id.series_page_info_table));
				
				if(getActivity()!=null) {
					getActivity().runOnUiThread(new Runnable() {
				        @Override
				        public void run() {

				        	final List<TvSeason> tsa = ts.getSeasons();
					        
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




		        	final List<TvSeason> tsa = ts.getSeasons();

				    
					if(tsa!=null && tsa.size()!=0) {
						
						getActivity().runOnUiThread(new Runnable() {
					    	@Override
						    public void run() {		
							    
					    		final LayoutInflater vi = LayoutInflater.from(info_table.getContext());
							    
					    		Iterator<TvSeason> i = tsa.iterator();
					    		
						        LinearLayout header = (LinearLayout) vi.inflate(R.layout.series_table_header,null);
						        TextView txt = (TextView)header.findViewById(R.id.series_table_header_text);
						        
						        TableRow tr = new TableRow(info_table.getContext());
						    
						        tr.addView(header);
						        
						        TableRow.LayoutParams params = (TableRow.LayoutParams)header.getLayoutParams();
						        params.span = maxcol;
						        header.setLayoutParams(params);
						        
						        txt.setText("Seasons");
						        info_table.addView(tr);
								
							    int cc = 0;
							    
							    ArrayList<InfoNode> trl = new ArrayList();
							    
					    		while(i.hasNext()) {
					    			if(cc >= maxcol) {
					    				cc = 0;
					    			}
					    			 
					    			if(cc == 0) {					    				
					    				tr = new TableRow(info_table.getContext());
										tr.setOrientation(LinearLayout.HORIZONTAL);
										info_table.addView(tr);
										tr.setVisibility(View.GONE);
										InfoNode nd=new InfoNode();
										nd.row = tr;
										trl.add(nd);
					    			}
							        
					    			View v = (View)vi.inflate(R.layout.series_info_grid_entry,null);
							        
					    			TvSeason tv_season = i.next();
					    			
					    			tv_season = Tmdb.get().loadSeason(ts.getId(), tv_season.getSeasonNumber());
					    			
							        TextView       tt0 = (TextView)     v.findViewById(R.id.creator_name);
							        TextView       tt1 = (TextView)     v.findViewById(R.id.person_role);
								    ImageView       ii = (ImageView)    v.findViewById(R.id.creator_image);
								    ProgressBar     pb = (ProgressBar)  v.findViewById(R.id.creator_image_progress);
								    
								    tt0.setText("#"+Integer.toString(tv_season.getSeasonNumber())+(tv_season.getAirDate() == null ? "" : " "+tv_season.getAirDate()));
								    
								    if(tv_season.getEpisodes() != null && !tv_season.getEpisodes().isEmpty() )
								    	tt1.setText(Integer.toString(tv_season.getEpisodes().size())+" Episodes");
								    else
								    	tt1.setText("");
								    	
							    	ii.setTag(tv_season.getPosterPath());

							    	trl.get(trl.size()-1).img.add(ii);
							    	trl.get(trl.size()-1).pb.add(pb);
							    	
							    	tr.addView(v);
					    			
								    cc++;
					    		}
					    		
					    		final List<InfoNode> ftrl = trl;
 					    		header.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										LinearLayout ll = (LinearLayout) v;
										Iterator<InfoNode> i = ftrl.iterator();
										boolean f = false;
										while(i.hasNext()) {
											InfoNode n = i.next();
											if(n.row.getVisibility()!=View.VISIBLE) {
												n.row.setVisibility(View.VISIBLE);
												if(n.img!=null) {
													Iterator<ImageView> i2 = n.img.iterator();
													for(int j = 0;j<n.img.size();j++) {
														new BitmapDownloaderTask(n.img.get(j), getActivity(), n.pb.get(j), null,tv_progress).execute();
													}
													n.img=null;
													n.pb=null;
												}
												f = true;
											}
											else {
												n.row.setVisibility(View.GONE);
											}
										}
										ImageView iv = (ImageView)ll.findViewById(R.id.series_table_header_expand);
										iv.setImageDrawable(getActivity().getResources().getDrawable(f ? R.drawable.down : R.drawable.right));
									}
 					    		});
					    	}
						});
					}
			
					

					
					// Credits
					//
					
					Credits c;
					try {
						c = Tmdb.get().api().getTvSeries().getCredits(ts.getId(),null);
					} catch(Exception ex) {
						return;
					}
					new CastInfoThread(getActivity(),info_table,maxcol,
							c!=null ? c.getCast() : null,c!=null ? c.getCrew() : null,ts.getCreatedBy()).start();
	        	}		
			}
 		
 		}).start();

        return _rootView;
    }
}