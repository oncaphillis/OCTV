package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import net.oncaphillis.whatsontv.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ExpandableListView;
import android.widget.GridLayout;
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
	
    private static final int CAST    = 0;
    private static final int CREW    = 1;
    private static final int CREATOR = 2;
       
    private static final String _prefix = "<html>"+
										   " <body style='background-color: #000000; color: #ffffff'>";
	private static final String _postfix = "</body></html>";
	
	private Activity _activity;
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

				
				// Load main image and HTML code
				//
				
				final Bitmap bm = Tmdb.get().loadPoster(1, ts.getPosterPath());
				final TableLayout  info_table = ((TableLayout) _rootView.findViewById(R.id.series_page_info_table));
				
				if(_activity!=null) {
					_activity.runOnUiThread(new Runnable() {
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


					// Credits
					//
					
					final String[] info_type={"Cast","Crew","Creator"};
					Credits c;
					try {
						c = Tmdb.get().api().getTvSeries().getCredits(ts.getId(),null);
					} catch(Exception ex) {
						return;
					}

		        	final List<TvSeason> tsa = ts.getSeasons();
					final int img_count =  c.getCast().size()+c.getCrew().size()+ts.getCreatedBy().size()+tsa.size();
				    tv_progress.setTag(new Integer(img_count));
				    
					if(tsa!=null && tsa.size()!=0) {
						
						_activity.runOnUiThread(new Runnable() {
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

							        TextView       tt0 = (TextView)     v.findViewById(R.id.creator_name);
							        TextView       tt1 = (TextView)     v.findViewById(R.id.person_role);
								    ImageView       ii = (ImageView)    v.findViewById(R.id.creator_image);
								    ProgressBar     pb = (ProgressBar)  v.findViewById(R.id.creator_image_progress);
								    
								    tt0.setText("#"+Integer.toString(tv_season.getSeasonNumber())+(tv_season.getAirDate() == null ? "" : " "+tv_season.getAirDate()));
								    tt1.setText(tv_season.getOverview()==null ? "" : tv_season.getOverview());
								    
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
														new BitmapDownloaderTask(n.img.get(j), _activity, n.pb.get(j), null,tv_progress).execute();
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
										iv.setImageDrawable(_activity.getResources().getDrawable(f ? R.drawable.down : R.drawable.right));
									}
 					    		});
					    	}
						});
					}
			
					
					final Credits   cr = c;
					
					if(cr!=null ){
						_activity.runOnUiThread(new Runnable() {
					    	@Override
						    public void run() {		

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
			
										List<InfoNode> trl = new ArrayList();
										
										while(it.hasNext()) {
											if(cc>=maxcol)
												cc=0;
											
											if(cc==0) {
												InfoNode n = new InfoNode();
												tr = new TableRow(info_table.getContext());
												tr.setOrientation(LinearLayout.HORIZONTAL);
												info_table.addView(tr);
												n.row = tr;
												trl.add(n);
												tr.setVisibility(View.GONE);
											}
		
											String nn;
											String rr;
											String pp;
											
											if(person_group==CAST) {
												PersonCast p = (PersonCast)it.next();
												nn = p.getName();
												rr = p.getCharacter();
												pp = p.getProfilePath();
											} else if(person_group==CREW) {
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
										    										    
										    ii.setTag(pp);
					
										    tt0.setText(nn == null ? "" : nn);
										    tt1.setText(rr == null ? "" : rr);
										    
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
														f = true;
														if(n.img!=null) {
															for(int j=0;j<n.img.size();j++) {
																new BitmapDownloaderTask(n.img.get(j), _activity, n.pb.get(j), null,tv_progress).execute();
															}
															n.img = null;
															n.pb = null;
														}
													}
													else {
														n.row.setVisibility(View.GONE);
													}
												}
												ImageView iv = (ImageView)ll.findViewById(R.id.series_table_header_expand);
												iv.setImageDrawable(_activity.getResources().getDrawable(f ? R.drawable.down : R.drawable.right));
											}
		 					    		});
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