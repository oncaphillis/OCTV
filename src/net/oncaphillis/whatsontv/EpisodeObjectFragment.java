package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.Date;

import net.oncaphillis.whatsontv.Tmdb.EpisodeInfo;

import org.apache.commons.lang3.StringEscapeUtils;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class EpisodeObjectFragment extends EntityInfoFragment {

	private View _rootView = null;
	private int _series;
	private int _season;
	private int _episode;
	private TaskObserver _threadObserver = null; 

    private static final String _prefix = "<html>"+
			   " <body style='background-color: #000000; color: #ffffff'>";

    private static final String _postfix = "</body></html>";

	
	public EpisodeObjectFragment() {
	}


	@Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

		Bundle args = getArguments();
		
		_series = args.getInt("series");
		_season = args.getInt("season");
		_episode = args.getInt("episode");
		
		_rootView    = inflater.inflate(R.layout.episode_fragment, container, false);
		
		final WebView  overview_webview    = ((WebView) _rootView.findViewById(R.id.episode_fragment_overview));
		final ImageView episode_still      = ((ImageView) _rootView.findViewById(R.id.episode_stillpath));
		
        final TextView tv_header           = ((TextView) _rootView.findViewById(R.id.episode_fragment_title));
        final TextView tv_rating           = ((TextView) _rootView.findViewById(R.id.episode_page_voting));
        final TextView tv_rating_count     = ((TextView) _rootView.findViewById(R.id.episode_page_voting_count));
        final TextView tv_date_tag = ((TextView) _rootView.findViewById(R.id.episode_fragment_nearest_tag));
        final TextView tv_date = ((TextView) _rootView.findViewById(R.id.episode_fragment_last_aired));

        final String no_overview = getActivity().getResources().getString(R.string.no_overview_available);
        final String aires = getActivity().getResources().getString(R.string.aires);
        final String aired = getActivity().getResources().getString(R.string.aired);
		final Fragment fragment = this;
        
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				final TvSeries series = Tmdb.get().loadSeries(_series);
				final EpisodeInfo episode = Tmdb.get().loadEpisode(_series, _season, _episode);
				final String ds;
				
				if(series!= null && episode!=null) {

					// If we currently do not have a clock value
			        // for this episode we might want to get informed 
			        // when this value becomes available.
					
					Runnable r0 = new Runnable() {
						Date today = TimeTool.getToday();
						
						@Override
						public void run() {
							final Date date = episode.getAirTime();
							if(fragment.getActivity()!=null) {
								fragment.getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if(date!=null) {
											tv_date.setText(Environment.TimeFormater.format(date));
											if(! today.before(date)) {
												tv_date_tag.setText(aired);
												tv_date.setTextColor(getActivity().getResources().getColor(R.color.oncaphillis_white));
											} else {
												tv_date_tag.setText(aires);
												tv_date.setTextColor(getActivity().getResources().getColor(R.color.oncaphillis_orange));
											}
										}
									}
								});
							}
						}
					};
					
					tv_date.setTag(r0);
					Tmdb.get().trakt_reader().register(r0);
					
					Date date  = episode.getAirTime() == null ? episode.getAirDate() : episode.getAirTime();
					Date today = TimeTool.getToday();

					episode_still.setTag(episode.getTmdb().getStillPath());
			        new BitmapDownloaderTask(episode_still, 3, getActivity(), null, null, null).execute();
			        
			        if(getActivity()!=null) {
			        	final Date _date = date;
						final Activity act = getActivity();
			        	getActivity().runOnUiThread(new Runnable() {
							
								
							float  _voteAverage = episode.getTmdb().getVoteAverage();
							int    _voteCount = episode.getTmdb().getVoteCount();
							String _overview = episode.getTmdb().getOverview();
							
							boolean withTime = episode.getAirTime()==null ? false : true;
							
							@Override
							public void run() {
								String episodes_for = act.getResources().getString(R.string.episodes_for);
								act.setTitle(String.format(episodes_for,series.getName()));
								
								Date today = TimeTool.getToday();
								
								if(_overview == null || _overview == "")
									_overview = no_overview;
								
								if(episode.getTmdb().getName() == null || episode.getTmdb().getName().equals(""))  {
									tv_header.setTextColor(act.getResources().getColor(R.color.oncaphillis_light_grey));
									tv_header.setText(act.getResources().getString(R.string.no_title_available));
								} else {
									tv_header.setTextColor(act.getResources().getColor(R.color.oncaphillis_white));
									tv_header.setText(episode.getTmdb().getName());
								}
								
								overview_webview.loadData(_prefix+
							    				StringEscapeUtils.escapeHtml4(_overview) +  
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
		
		
        return _rootView;
    }
}
