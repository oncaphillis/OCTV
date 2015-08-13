package net.oncaphillis.whatsontv;

import java.util.List;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.tv.TvSeries;

public abstract class Pager {
	
	private String _language=null;
	
/*	protected TmdbApi api()  {
		return Tmdb.api();
	}
*/
	abstract List<TvSeries> getPage(int page);
	abstract void start();
	abstract void end();
	abstract int getTotal();  
	abstract void reset();
}