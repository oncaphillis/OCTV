package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.oncaphillis.whatsontv.SeriesObjectFragment.InfoNode;
import net.oncaphillis.whatsontv.Tmdb.EpisodeInfo;

import org.apache.commons.lang3.StringEscapeUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class EpisodeObjectFragment extends EntityInfoFragment {

	private View _rootView = null;
	private int _series;
	private int _season;
	private int _episode;
	
    private static final String _prefix = "<html>"+
			   " <body style='background-color: #000000; color: #ffffff'>";

    private static final String _postfix = "</body></html>";

	
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
		
        final TextView tv_diag             = ((TextView) _rootView.findViewById(R.id.episode_fragment_id));
        final TextView tv_header           = ((TextView) _rootView.findViewById(R.id.episode_header));
        
        final TextView tv_rating           = ((TextView) _rootView.findViewById(R.id.episode_page_voting));
        final TextView tv_first_aired      = ((TextView) _rootView.findViewById(R.id.episode_page_first_aired));
        final TextView tv_last_aired       = ((TextView) _rootView.findViewById(R.id.episode_page_last_aired));
        final TextView tv_genres           = ((TextView) _rootView.findViewById(R.id.episode_page_genres));
		

        
        new Thread(new Runnable() {
			@Override
			public void run() {
				final TvSeries s = Tmdb.get().loadSeries(_series);
				final EpisodeInfo e = Tmdb.get().loadEpisode(_series, _season, _episode);

				if(s!= null && e!=null) {
			        episode_still.setTag(e.getTmdb().getStillPath());
			        new BitmapDownloaderTask(episode_still, getActivity(), null, null, null).execute();

					final Bitmap bm = Tmdb.get().loadPoster(1, e.getTmdb().getStillPath());
					final String overview = e.getTmdb().getOverview();
					if(getActivity()!=null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								
								tv_header.setText(s.getName()+":"+e.getTmdb().getName());
								overview_webview.loadData(_prefix+
							    				StringEscapeUtils.escapeHtml4(overview) +  
							        			_postfix, "text/html; charset=utf-8;", "UTF-8");
		
							}
						});
					}
				}
			}
        }).start();
        

        return _rootView;
    }
}
