package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
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
	private String _networks;
	private Date _nearest = null;
	private Date _today = null;
	private TvSeries _series=null;
	private List<TvSeason> _seasons = null;

	private SeriesInfo _series_info;
	private int _maxcol = 1;
	

 	class InfoNode {
		public TableRow row = null;
		public ArrayList<ImageView>   img = new ArrayList<ImageView>();
		public ArrayList<ProgressBar> pb  = new ArrayList<ProgressBar>();
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
        final TextView tv_header           = ((TextView) _rootView.findViewById(R.id.series_header));
        final TextView tv_network     = ((TextView) _rootView.findViewById(R.id.series_page_network));
        final TextView tv_genres     = ((TextView) _rootView.findViewById(R.id.series_page_genres));
        final ProgressBar tv_progress      = ((ProgressBar) _rootView.findViewById(R.id.series_fragment_progress));
        
        final TextView tv_voting           = ((TextView) _rootView.findViewById(R.id.series_page_vote));
        final TextView tv_voting_count     = ((TextView) _rootView.findViewById(R.id.series_page_vote_count));
        
        boolean landscape = true;

        _maxcol  = Environment.getColumns(this.getActivity());
        
        if(_maxcol==1)
        	landscape = false;
        else
        	_maxcol/=2;
        
        int first_aired = landscape ? R.id.series_page_first_aired : R.id.series_page_first_aired_portrait;
        int last_aired = landscape ? R.id.series_page_last_aired : R.id.series_page_last_aired_portrait;
        int nearest_title = landscape ? R.id.series_page_nearest_title : R.id.series_page_nearest_title_portrait;
        int nearest_still = landscape ? R.id.series_page_nearest_still : R.id.series_page_nearest_still_portrait;
        int nearest_summary = landscape ? R.id.series_page_nearest_summary : R.id.series_page_nearest_summary_portrait;
        int episode_vote = landscape ? R.id.series_page_episode_vote : R.id.series_page_episode_vote_count_portrait;
        int episode_vote_count = landscape ? R.id.series_page_episode_vote_count : R.id.series_page_episode_vote_count_portrait;
        int next_last_tag = landscape ? R.id.series_page_nearest_tag : R.id.series_page_nearest_tag_portrait;
        
        final TextView tv_first_aired      = ((TextView) _rootView.findViewById(first_aired));
        final TextView tv_last_aired       = ((TextView) _rootView.findViewById(last_aired));
        final TextView tv_nearest = ((TextView) _rootView.findViewById(nearest_title));
        final ImageView tv_nearest_still = ((ImageView)_rootView.findViewById(nearest_still));
        final TextView tv_nearest_summary = ((TextView)_rootView.findViewById(nearest_summary));
        final TextView tv_next_last_tag = ((TextView)_rootView.findViewById(next_last_tag));
        final TextView tv_episode_vote = ((TextView)_rootView.findViewById(episode_vote));
        final TextView tv_episode_vote_count = ((TextView)_rootView.findViewById(episode_vote_count));

        final String nextText = getResources().getString(R.string.next);
        final String lastText = getResources().getString(R.string.last);
        
        if(landscape) {
        	LinearLayout ll = ((LinearLayout)_rootView.findViewById(R.id.series_page_episode_layout_portrait));
        	ll.setVisibility(View.GONE);
        } else {
        	LinearLayout ll = ((LinearLayout)_rootView.findViewById(R.id.series_page_episode_layout_landscape));
        	ll.setVisibility(View.GONE);
        }
        tv_header.setText(_seriesName);
        
        if(Environment.isDebug())
        	tv_network.setText("#"+Integer.toString(_seriesId));
        
        overview_webview.loadData("","text/html; charset=utf-8;", "utf-8");
        tv_progress.setVisibility(View.INVISIBLE);
         		
 		
 		new Thread(new Runnable() {
        	
			private String _nearest_title;
			private int _nearest_season;
			private TvEpisode _nearest_episode; 
			private String  _nearest_still_path;
			private String _nearest_summary;

			@Override
			public void run() {
				
				if(Tmdb.get().api()==null) {
					return;
				}
				
				try {
					_series = Tmdb.get().loadSeries(_seriesId);
					_series_info  = new SeriesInfo(_series); 
					_nearest_still_path = _series_info.getNearestEpisode().getStillPath();
					_nearest_episode = _series_info.getNearestEpisode();
					_nearest_summary = _series_info.getNearestEpisode().getOverview();
					_seasons = _series_info.getSeasons();
					if(Environment.isDebug())
		        		_networks = "#"+Integer.toString(_seriesId)+((_series.getPosterPath()==null) ? " \u2205" : "\u753b")+" ";
					_networks += new SeriesInfo(_series).getNetworks();
				} catch (Exception ex) {
					return;
				}

				final String    name           = _series.getName();
				final String    overview       = _series.getOverview();
				final String    poster         = _series.getPosterPath();

				_today = TimeTool.getToday();
				_nearest = _series_info.getNearestAiring();
				_nearest_title   = _series_info.getNearestEpisodeTitle();
				_nearest_season  = _series_info.getNearestEpisodeSeason();
				
				
				
				// Load main image and HTML code
				//
				
				final Bitmap bm = Tmdb.get().loadPoster(1, _series.getPosterPath());
				final TableLayout  series_info_table = ((TableLayout) _rootView.findViewById(R.id.series_page_info_table));
				final TableLayout  episode_info_table = ((TableLayout) _rootView.findViewById(R.id.series_page_nearest_info_table));
				
				if(_nearest_still_path != null) {
					tv_nearest_still.setTag(_nearest_still_path);
					new BitmapDownloaderTask(tv_nearest_still,4, getActivity(), null,null,null).execute();
				}
				
				if(getActivity()!=null) {
					getActivity().runOnUiThread(new Runnable() {
				        @Override
				        public void run() {

				        	final List<TvSeason> tsa = _series.getSeasons();
					        
				        	String gen = new String();
					        


				        	if(_series!=null) {
					        	List<Genre> gl = _series.getGenres();
					        	if(gl!=null) {
					        		Iterator<Genre> it = gl.iterator();
					        		int i=0;
					        		while(it.hasNext()) {
					        			gen += (i++==0 ? "" : ",")+it.next().getName();
					        		}
					        		
					        		tv_network.setText(_networks);
					        		tv_genres.setText(gen);
					    	    	tv_header.setText( _series.getName() +
					    	    			(_series.getOriginalName()!=null 
					    	    			&& !_series.getOriginalName().equals("") 
					    	    			&& !_series.getOriginalName().equals(_series.getName()) ? " ("+_series.getOriginalName()+")" : ""));
					        	}
					        	
					        	String fa = _series.getFirstAirDate();
					        	
					        	if(fa != null)
					        		tv_first_aired.setText(fa);
					        	
					        	if(_nearest != null && _today!=null) {
					        		
					        		if(_nearest.before(_today)) {
					        			tv_last_aired.setTextColor(getActivity().getResources().getColor(R.color.oncaphillis_white));
					        			tv_next_last_tag.setText(lastText);
					        		} else {
					        			tv_last_aired.setTextColor(getActivity().getResources().getColor(R.color.oncaphillis_orange));
					        			tv_next_last_tag.setText(nextText);
					        		}
					        		
					        		DateFormat df = _series_info.hasClock() ? Environment.TimeFormater : Environment.DateFormater;
					        		
					        		tv_last_aired.setText(df.format(_nearest));
					        		tv_nearest.setText(Integer.toString(_nearest_season)+"x"+
					        				Integer.toString(_nearest_episode.getEpisodeNumber())+" "+_nearest_title);
					        		
									if(_nearest_episode != null && _nearest_episode.getVoteCount()!=0) {
										tv_episode_vote.setText(String.format("%.1f", _nearest_episode.getVoteAverage())+"/"+"10");
										tv_episode_vote_count.setText(Integer.toString(_nearest_episode.getVoteCount()));
									} else {
										tv_episode_vote.setText("-/-");
										tv_episode_vote_count.setText("0");										
									}
									
					        		if(_nearest_summary != null)
					        			tv_nearest_summary.setText(_nearest_summary);

					        	
					        	}
					        	
					        	if(_series.getVoteCount()!=0) {
					        		tv_voting.setText(String.format("%.1f",_series.getVoteAverage())+"/"+Integer.toString(10) );
					        		tv_voting_count.setText(Integer.toString(_series.getVoteCount()));
					        	} else {
					        		tv_voting.setText("-/-");
					        		tv_voting_count.setText("0");
					        	}


					        	// tv_header.setText(name);

					        	overview_webview.loadData(_prefix+getBitmapHtml(bm)+
					    				StringEscapeUtils.escapeHtml4(overview) +  
					        			_postfix, "text/html; charset=utf-8;", "UTF-8");

				        	} 
				        }
				    });




		        	final List<TvSeason> tsa = _series.getSeasons();

				    
					if(tsa!=null && tsa.size()!=0) {
						
						getActivity().runOnUiThread(new Runnable() {
					    	@Override
						    public void run() {		
							    
					    		final LayoutInflater vi = LayoutInflater.from(series_info_table.getContext());
							    
					    		Iterator<TvSeason> i = tsa.iterator();
					    		
						        LinearLayout header = (LinearLayout) vi.inflate(R.layout.series_table_header,null);
						        TextView txt = (TextView)header.findViewById(R.id.series_table_header_text);
						        
						        TableRow tr = new TableRow(series_info_table.getContext());
						    
						        tr.addView(header);
						        
						        TableRow.LayoutParams params = (TableRow.LayoutParams)header.getLayoutParams();
						        params.span = _maxcol;
						        header.setLayoutParams(params);
						        
						        txt.setText(Integer.toString(tsa.size())+" Seasons");
						        series_info_table.addView(tr);
								
							    int cc = 0;
							    
							    ArrayList<InfoNode> trl = new ArrayList();
							    
					    		while(i.hasNext()) {
					    			if(cc >= _maxcol) {
					    				cc = 0;
					    			}
					    			 
					    			if(cc == 0) {					    				
					    				tr = new TableRow(series_info_table.getContext());
										tr.setOrientation(LinearLayout.HORIZONTAL);
										series_info_table.addView(tr);
										tr.setVisibility(View.GONE);
										InfoNode nd=new InfoNode();
										nd.row = tr;
										trl.add(nd);
					    			}
							        
					    			View v = (View)vi.inflate(R.layout.series_info_grid_entry,null);
							        
					    			TvSeason tv_season = i.next();
					    			
					    			tv_season = Tmdb.get().loadSeason(_series.getId(), tv_season.getSeasonNumber());
					    			
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
						c = Tmdb.get().api().getTvSeries().getCredits(_series.getId(),null);
					} catch(Exception ex) {
						return;
					}

					if(c!=null) {
						List<? extends Person>[] cc = new ArrayList[3];
						
						String[] titles = getActivity().getResources().getStringArray(R.array.cast_titles);
						
						cc[0] = c != null ? c.getCast() : null;
						cc[1] = c != null ? c.getCrew() : null;
						cc[2] = _series.getCreatedBy();
	
						new CastInfoThread(getActivity(),series_info_table,_maxcol,cc,titles).start();
					}
					
					if(_nearest_episode != null)
						c = _nearest_episode.getCredits();

					if(c!=null) {
						List<? extends Person>[] cc = new ArrayList[1];
						
						String s[] = getActivity().getResources().getStringArray(R.array.cast_titles);
						
						String a[] = new String[] {s[Environment.GUEST]};

						cc[0] = c != null ? c.getGuestStars() : null;	
						
						new CastInfoThread(getActivity(),episode_info_table,_maxcol,cc,a).start();
					}
	        	}		
			}
 		} ).start();

        return _rootView;
    }
}