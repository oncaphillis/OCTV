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

import net.oncaphillis.whatsontv.SeriesObjectFragment.InfoNode;

import org.apache.commons.lang3.StringEscapeUtils;

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

public class EpisodeObjectFragment extends Fragment {

	
	private View _rootView = null;

	@Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

		_rootView    = inflater.inflate(R.layout.episode_fragment, container, false);
        return _rootView;
    }
	
}
