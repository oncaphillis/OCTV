package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.Date;

import net.oncaphillis.whatsontv.Tmdb.EpisodeInfo;

import org.apache.commons.lang3.StringEscapeUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class EpisodeObjectFragment extends EntityInfoFragment {

	private TaskObserver _threadObserver = null; 

    private static final String _prefix = "<html>"+
			   " <body style='background-color: #000000; color: #ffffff'>";

    private static final String _postfix = "</body></html>";
    
    static private String _no_overview = null;
    static private String _episodes = null;
    static private String _no_name = null;
    
	public EpisodeObjectFragment() {
	}

	@Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
		
		if(_no_overview == null)
			_no_overview  = container.getResources().getString(R.string.no_overview_available);

		if(_episodes == null)
			_episodes  = container.getResources().getString(R.string.episodes);

		if(_no_name == null)
			_no_name  = container.getResources().getString(R.string.no_name);

		Bundle args = getArguments();
		
		int series  = args.getInt("series");
		int season  = args.getInt("season");
		int episode = args.getInt("episode");
		
		return episode != 0 ? createEpisodeView(inflater,container,series,season,episode) : 
			createSeasonView(inflater,container,series,season);
	}
	
	private View createSeasonView(LayoutInflater inflater, ViewGroup container, final int series,final int season) {
		View theView    = inflater.inflate(R.layout.season_fragment, container, false);

		final Fragment fragment = this;
		
		final WebView  overview_webview    = ((WebView)   theView.findViewById(R.id.season_fragment_overview));
		final TextView first_txt = (TextView)theView.findViewById(R.id.season_fragment_first_aired);
		final TextView episodes_txt = (TextView)theView.findViewById(R.id.season_fragment_episodes);
		final TextView name_txt = (TextView)theView.findViewById(R.id.season_fragment_name);
		
		Thread t = new Thread(new Runnable() {
			Activity act = fragment.getActivity(); 
			@Override			
			public void run() {
				
				final TvSeason tvs = Tmdb.get().loadSeason(series, season);
				final Date d = Tmdb.getAirDate(tvs);

				act.runOnUiThread(new Runnable() {
					String overview = tvs.getOverview();
					String name = tvs.getName();
					public void run() {
						first_txt.setText(Environment.TmdbDateFormater.format(d));

						if(tvs.getEpisodes() != null && tvs.getEpisodes().size()!=0)
							episodes_txt.setText(String.format("%d %s", tvs.getEpisodes().size(),_episodes));

						if(overview == null || overview.equals(""))
							overview = _no_overview;
						
						if(name==null || name.equals(""))
							name = _no_name;
						
						name_txt.setText(name);
						
						overview_webview.loadData(_prefix+
			    				StringEscapeUtils.escapeHtml4(overview) +  
			        			_postfix, "text/html; charset=utf-8;", "UTF-8");
						
						
			        	// Then initialize lazy load of Bitmap + text
						{
							AsyncTask<String,Void,Bitmap> at = new AsyncTask<String,Void,Bitmap>() {
								final String path = tvs.getPosterPath();
								final WebView web = overview_webview;
								
								@Override
								protected Bitmap doInBackground(String...p) {
									Bitmap bm = Tmdb.get().loadPoster(1,path);
									return bm;
								}

							    @Override
							    // Once the image is downloaded, associates it to the imageView
							    protected void onPostExecute(Bitmap bm) {
							    	web.loadData(_prefix+getBitmapHtml(bm)+
						    				StringEscapeUtils.escapeHtml4(overview) +  
						        			_postfix, "text/html; charset=utf-8;", "UTF-8");
							    	web.reload();
							    	web.invalidate();
							    }
							};
							at.execute();
							
							if(_threadObserver!=null)
								_threadObserver.add(at);
				        }
					}
				});
			}
			
		});
		
		t.start();
		
		if(_threadObserver!=null)
			_threadObserver.add(t);
		
		return theView;
	}
	
	private View createEpisodeView(LayoutInflater inflater, ViewGroup container, final int series,final int season,final int episode) {
		
		View theView    = inflater.inflate(R.layout.episode_fragment, container, false);
		
		final WebView  overview_webview    = ((WebView)   theView.findViewById(R.id.episode_fragment_overview));
		final ImageView episode_still      = ((ImageView) theView.findViewById(R.id.episode_stillpath));
		
        final TextView tv_header           = ((TextView) theView.findViewById(R.id.episode_fragment_title));
        final TextView tv_rating           = ((TextView) theView.findViewById(R.id.episode_page_voting));
        final TextView tv_rating_count     = ((TextView) theView.findViewById(R.id.episode_page_voting_count));
        final TextView tv_date_tag         = ((TextView) theView.findViewById(R.id.episode_fragment_nearest_tag));
        final TextView tv_date             = ((TextView) theView.findViewById(R.id.episode_fragment_last_aired));

        final String aires = getActivity().getResources().getString(R.string.aires);
        final String aired = getActivity().getResources().getString(R.string.aired);
		final Fragment fragment = this;
        
		Thread t = new Thread(new Runnable() {
			final int o_white  = fragment.getActivity().getResources().getColor(R.color.oncaphillis_white);
			final int o_orange = fragment.getActivity().getResources().getColor(R.color.oncaphillis_orange);
			
			@Override
			public void run() {
				final TvSeries tvs = Tmdb.get().loadSeries( series);
				final EpisodeInfo tve = Tmdb.get().loadEpisode( series,  season,  episode);
				final String ds;
				
				if(tvs!= null && tve!=null) {

					// If we currently do not have a clock value
			        // for this episode we might want to get informed 
			        // when this value becomes available.
					
					Runnable r0 = new Runnable() {
						Date today = TimeTool.getToday();
						
						@Override
						public void run() {
							final Date date = tve.getAirTime();
							if(fragment.getActivity()!=null) {
								fragment.getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if(date!=null) {
											tv_date.setText(Environment.TimeFormater.format(date));
											if(! today.before(date)) {
												tv_date_tag.setText(aired);
												tv_date.setTextColor(o_white);
											} else {
												tv_date_tag.setText(aires);
												tv_date.setTextColor(o_orange);
											}
										}
									}
								});
							}
						}
					};
					
					tv_date.setTag(r0);
					Tmdb.get().trakt_reader().register(r0);
					
					Date date  = tve.getAirTime() == null ? tve.getAirDate() : tve.getAirTime();
					Date today = TimeTool.getToday();

					episode_still.setTag(tve.getTmdb().getStillPath());
			        new BitmapDownloaderTask(episode_still, 3, getActivity(), null, null, null).execute();
			        
			        if(getActivity()!=null) {
			        	final Date _date = date;
						final Activity act = getActivity();
			        	getActivity().runOnUiThread(new Runnable() {

							float  _voteAverage = tve.getTmdb().getVoteAverage();
							int    _voteCount = tve.getTmdb().getVoteCount();
							String overview = tve.getTmdb().getOverview();
							
							boolean withTime = tve.getAirTime()==null ? false : true;
							
							@Override
							public void run() {
								String episodes_for = act.getResources().getString(R.string.episodes_for);
								act.setTitle(String.format(episodes_for,tvs.getName()));
								
								Date today = TimeTool.getToday();
								
								if(overview == null || overview == "")
									overview = _no_overview;
								
								if(tve.getTmdb().getName() == null || tve.getTmdb().getName().equals(""))  {
									tv_header.setTextColor(act.getResources().getColor(R.color.oncaphillis_light_grey));
									tv_header.setText(act.getResources().getString(R.string.no_title_available));
								} else {
									tv_header.setTextColor(act.getResources().getColor(R.color.oncaphillis_white));
									tv_header.setText(tve.getTmdb().getName());
								}
								
								overview_webview.loadData(_prefix+
							    				StringEscapeUtils.escapeHtml4(overview) +  
							        			_postfix, "text/html; charset=utf-8;", "UTF-8");
	
								tv_rating.setText(_voteCount==0 ? "-/-" : String.format("%.1f/10", _voteAverage));
								tv_rating_count.setText(String.format("%d", _voteCount));
								if(fragment.getActivity()!=null)  {
									if(_date!=null) {
										tv_date.setText(withTime ? Environment.TimeFormater.format(_date) :  Environment.TmdbDateFormater.format(_date));
										if(!today.before(_date)) {
											tv_date_tag.setText(aired);
											tv_date.setTextColor(fragment.getActivity().getResources().getColor(R.color.oncaphillis_white));
										} else {
											tv_date_tag.setText(aires);
											tv_date.setTextColor(fragment.getActivity().getResources().getColor(R.color.oncaphillis_orange));
										}
									} else {
										tv_date.setText("...");
										tv_date_tag.setText(aired);
										tv_date.setTextColor(fragment.getActivity().getResources().getColor(R.color.oncaphillis_white));
									}
								}
							}
						});
					}
				}
			}
        });
		
		t.start();
		
		if(_threadObserver!=null)
			_threadObserver.add(t);

        return theView;
    }
}
