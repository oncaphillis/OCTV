package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.tv.Network;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.commons.lang3.StringEscapeUtils;

import net.oncaphillis.whatsontv.R;
import net.oncaphillis.whatsontv.SeriesObjectFragment.InfoNode;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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
	
	private int _maxcol = 1;

 	class InfoNode {
		public TableRow row = null;
		public ArrayList<ImageView>   img = new ArrayList<ImageView>();
		public ArrayList<ProgressBar> pb  = new ArrayList<ProgressBar>();
		public ArrayList<TextView>    tx  = new ArrayList<TextView>();
		public ArrayList<TvSeason>    se  = new ArrayList<TvSeason>();
 	};
	
	
	@Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

    	final View rootView   = inflater.inflate(R.layout.series_fragment, container, false);
        Bundle args = getArguments();

        final int seriesId   = args.getIntArray(ARG_IDS)[args.getInt(ARG_IX)];
        final String seriesName = args.getStringArray(ARG_NAMES)[args.getInt(ARG_IX)];
        final String title;
        
        if( (title = args.getString(ARG_TITLE))!=null) {
        	this.getActivity().setTitle(title);
        }


        final WebView  overview_webview    = ((WebView) rootView.findViewById(R.id.series_fragment_overview));
        final TextView tv_header           = ((TextView) rootView.findViewById(R.id.series_header));
        final TextView tv_network     = ((TextView) rootView.findViewById(R.id.series_page_network));
        final TextView tv_genres     = ((TextView) rootView.findViewById(R.id.series_page_genres));
        final ProgressBar tv_progress      = ((ProgressBar) rootView.findViewById(R.id.series_fragment_progress));
        
        
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

        int next_last_tag = landscape ? R.id.series_page_nearest_tag : R.id.series_page_nearest_tag_portrait;
        
        final TextView tv_first_aired      = ((TextView) rootView.findViewById(first_aired));
        final TextView tv_nearest = ((TextView) rootView.findViewById(nearest_title));
        final ImageView tv_nearest_still = ((ImageView)rootView.findViewById(nearest_still));
        final TextView tv_next_last_tag = ((TextView)rootView.findViewById(next_last_tag));

        final TextView tv_nearest_summary = ((TextView)rootView.findViewById(nearest_summary));
        final TextView tv_last_aired      = ((TextView) rootView.findViewById(last_aired));

		final TableLayout  episode_info_table = ((TableLayout) rootView.findViewById(R.id.series_page_nearest_info_table));

        final String nextText = getResources().getString(R.string.next);
        final String lastText = getResources().getString(R.string.last);
		
        if(landscape) {
        	LinearLayout ll = ((LinearLayout)rootView.findViewById(R.id.series_page_episode_layout_portrait));
        	ll.setVisibility(View.GONE);
        } else {
        	LinearLayout ll = ((LinearLayout)rootView.findViewById(R.id.series_page_episode_layout_landscape));
        	ll.setVisibility(View.GONE);
        }
        tv_header.setText(seriesName);
        
        if(Environment.isDebug())
        	tv_network.setText("#"+Integer.toString(seriesId));
        
        overview_webview.loadData("","text/html; charset=utf-8;", "utf-8");
        tv_progress.setVisibility(View.INVISIBLE);
         		
 		
 		new Thread(new Runnable() {

 			private TvSeries series=null;

			@Override
			public void run() {
				
				if(Tmdb.get().api()==null) {
					return;
				}

				String networks = "";
				String gens = "";
				
				try {
					series = Tmdb.get().loadSeries(seriesId);
					
					if(Environment.isDebug())
		        		networks = "#"+Integer.toString(seriesId)+((series.getPosterPath()==null) ? " \u2205" : "\u753b")+" ";
					else
						networks = "";					
					
					
				} catch (Exception ex) {
					return;
				}
				
				if(series==null)
					return;

				{
					Iterator<Network> i = series.getNetworks().iterator();
					int j= 0;
					while(i.hasNext()) {
						networks += (j++==0 ? "" : "; ")+i.next().getName();
					}
				}

				{
					Iterator<Genre> i = series.getGenres().iterator();
					int j= 0;
					while(i.hasNext()) {
						gens += (j++==0 ? "" : "; ")+i.next().getName();
					}
				}

				// Credits
				//
				{
					final TvSeries _series = series;
					final TableLayout  series_info_table = ((TableLayout) rootView.findViewById(R.id.series_page_info_table));

					new Thread(new Runnable() {
						@Override
						public void run() {
							Credits c;
							try {
								c = Tmdb.get().api().getTvSeries().getCredits(series.getId(),null);
							} catch(Exception ex) {
								return;
							}

							if(c!=null) {
								List<? extends Person>[] cc = new ArrayList[3];
								String[] titles = getActivity().getResources().getStringArray(R.array.cast_titles);
								cc[0] = c != null ? c.getCast() : null;
								cc[1] = c != null ? c.getCrew() : null;
								cc[2] = series.getCreatedBy();
								new CastInfoThread(getActivity(),series_info_table,_maxcol,cc,titles).start();
							}
						}
					}).start();
				}

				// Set HTML code and everything already associated with the
				// series object. Load load main bitmap and rewrite the HTML
				// when finished.

				if(getActivity()!=null) {

					final String    _gens = gens;
					final String    _networks = networks;
			        final TextView  tv_voting           = ((TextView) rootView.findViewById(R.id.series_page_vote));
			        final TextView  tv_voting_count     = ((TextView) rootView.findViewById(R.id.series_page_vote_count));
			        final String    overview = series.getOverview();

					getActivity().runOnUiThread(new Runnable() {
			        	@Override
						public void run() {
							tv_network.setText(_networks);
							tv_genres.setText(_gens);
							tv_header.setText(series.getName() + 
									(series.getOriginalName()!=null && !series.getOriginalName().equals("") && !series.getOriginalName().equals(series.getName()) 
										? " ("+series.getOriginalName()+")" : ""));

							if(series.getVoteCount()!=0) {
						    	tv_voting.setText(String.format("%.1f",series.getVoteAverage())+"/"+Integer.toString(10) );
						        tv_voting_count.setText(Integer.toString(series.getVoteCount()));
				        	} else {
				        		tv_voting.setText("-/-");
				        		tv_voting_count.setText("0");
				        	}

							String fa = series.getFirstAirDate();
				        	
							if(fa != null)
				        		tv_first_aired.setText(fa);

							overview_webview.loadData(_prefix+
				    				StringEscapeUtils.escapeHtml4(overview) +  
				        			_postfix, "text/html; charset=utf-8;", "UTF-8");

							new AsyncTask<String,Void,Bitmap>() {
								final String _path = series.getPosterPath();
								final WebView _web = overview_webview;
								
								@Override
								protected Bitmap doInBackground(String...p) {
									Bitmap bm = Tmdb.get().loadPoster(1,_path);
									return bm;
								}

							    @Override
							    // Once the image is downloaded, associates it to the imageView
							    protected void onPostExecute(Bitmap bm) {
							    	_web.loadData(_prefix+" @@ "+getBitmapHtml(bm)+
						    				StringEscapeUtils.escapeHtml4(overview) +  
						        			_postfix, "text/html; charset=utf-8;", "UTF-8");
							    	_web.reload();
							    
							    }
							}.execute();
			        	}
					});
				}

				// Overview, Genre Network etc.
				if(getActivity()!=null) {

					new Thread(new Runnable() {

						@Override
						public void run() {
							final SeriesInfo series_info = new SeriesInfo(series);
							 
							final String nearest_title = series_info.getNearestEpisodeTitle();
							final int nearest_season = series_info.getNearestEpisodeSeason();
							final TvEpisode nearest_episode = series_info.getNearestEpisodeInfo().getTmdb(); 

					    	final Date today = TimeTool.getToday();
							final Date nearest = series_info.getNearestAiring();

							final String nearest_still_path = nearest_episode.getStillPath();
							
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									if(nearest_still_path != null) {
										tv_nearest_still.setTag(nearest_still_path);
										new BitmapDownloaderTask(tv_nearest_still,4, getActivity(), null,null,null).execute();
									}
									
								
									tv_nearest.setText(Integer.toString(nearest_season)+"x"+
					        				Integer.toString(nearest_episode.getEpisodeNumber())+" "+nearest_title);
	
									if(nearest_episode != null) {
										
										if(nearest_episode.getOverview()!=null) {
											tv_nearest_summary.setText(	nearest_episode.getOverview() );
										} else {
											tv_nearest_summary.setText(	"..." );
										}
									
						        		if(!today.before(nearest)) {
						        			tv_last_aired.setTextColor(getActivity().getResources().getColor(R.color.oncaphillis_white));
						        			tv_next_last_tag.setText(lastText);
						        		} else {
											tv_last_aired.setTextColor(getActivity().getResources().getColor(R.color.oncaphillis_orange));
						        			tv_next_last_tag.setText(nextText);
	
						        			if(!series_info.hasClock()) {
	
						        				Runnable r = new Runnable() {
						        					final Runnable _me = this;
						        					WeakReference<TextView> view = new WeakReference(tv_last_aired);
						        					TvSeries  tvs = series_info.getTmdb();
						        					@Override
													public void run() {
					        							SeriesInfo si = new SeriesInfo(tvs);
					        							DateFormat df = si.hasClock() ? Environment.TimeFormater : Environment.TimeFormater;
					        							final String s = df.format(si.getNearestAiring());
					        							if(getActivity()!=null && view.get() != null && view.get().getTag() == _me) {
					        								getActivity().runOnUiThread(new Runnable() {
					        									@Override
					        									public void run() {
					        										tv_last_aired.setText(s);
					        									}
					        								});
														}
													}
						        				};
							        			tv_last_aired.setTag(r);
						        				Tmdb.get().trakt_reader().register(r);
						        			}
						        		}
						        		DateFormat df = series_info.hasClock() ? Environment.TimeFormater : Environment.DateFormater;
						        		tv_last_aired.setText(df.format(nearest));
									
						        		// CAST OF NEAREST EPISODE
										Credits c = null;
										
										if(nearest_episode != null && (c = nearest_episode.getCredits())!=null) {

											List<? extends Person>[] cc = new ArrayList[1];
											
											String s[] = getActivity().getResources().getStringArray(R.array.cast_titles);
											
											String a[] = new String[] {s[Environment.GUEST]};

											cc[0] = c != null ? c.getGuestStars() : null;	
											
											new CastInfoThread(getActivity(),episode_info_table,_maxcol,cc,a).start();
										}
									}
								} 
							});
						}
					}).start();
				
					// SEASONS
					//
					final TableLayout  seasons_info_table = ((TableLayout) rootView.findViewById(R.id.series_page_seasons_table));

					new Thread(new Runnable() {
						@Override
						public void run() {

							final List<TvSeason> tsa = series.getSeasons();

							if(tsa!=null && tsa.size()!=0) {
								
								getActivity().runOnUiThread(new Runnable() {
							    	@Override
								    public void run() {		
									    
							    		final LayoutInflater vi = LayoutInflater.from(seasons_info_table.getContext());
									    
							    		Iterator<TvSeason> i = tsa.iterator();
							    		
								        LinearLayout header = (LinearLayout) vi.inflate(R.layout.series_table_header,null);
								        
								        
								        TextView txt = (TextView)header.findViewById(R.id.series_table_header_text);
								        TableRow tr = new TableRow(seasons_info_table.getContext());
								    
								        tr.addView(header);
								        
								        TableRow.LayoutParams params = (TableRow.LayoutParams)header.getLayoutParams();
								        params.span = _maxcol;
								        header.setLayoutParams(params);
								        
								        txt.setText(Integer.toString(tsa.size())+" Seasons");
								        seasons_info_table.addView(tr);
										
									    int cc = 0;
									    
									    ArrayList<InfoNode> trl = new ArrayList<InfoNode>();
									    
							    		while(i.hasNext()) {
							    			if(cc >= _maxcol) {
							    				cc = 0;
							    			}
							    			 
							    			if(cc == 0) {					    				
							    				tr = new TableRow(seasons_info_table.getContext());
												tr.setOrientation(LinearLayout.HORIZONTAL);
												seasons_info_table.addView(tr);
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
										    
										    if(tv_season.getEpisodes() != null && !tv_season.getEpisodes().isEmpty() )
										    	tt1.setText(Integer.toString(tv_season.getEpisodes().size())+" Episodes");
										    else
										    	tt1.setText("");
										    	
									    	ii.setTag(tv_season.getPosterPath());
		
									    	trl.get(trl.size()-1).img.add(ii);
									    	trl.get(trl.size()-1).pb.add(pb);
									    	trl.get(trl.size()-1).tx.add(tt1);
									    	trl.get(trl.size()-1).se.add(tv_season);
									    										    	
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
																final TextView _tv =  n.tx.get(j);
																final int _si = series.getId();
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
																		if(result!=null && result.getEpisodes() != null)
																			_tv.setText(Integer.toString(result.getEpisodes().size())+" Episodes"+(result.getName()!=null ? " "+result.getName() : "") );
																	}

																}.execute(n.se.get(j));
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
						}
					}).start();
	        	}		
			}
 		} ).start();
        return rootView;
    }
}