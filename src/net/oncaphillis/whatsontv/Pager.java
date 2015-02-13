package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.List;

public abstract class Pager {
	
	private String _language=null;
	
	protected TmdbApi api() {
		return Tmdb.get().api();
	}
	abstract List<TvSeries> getPage(int page);
	abstract void start();
	abstract void end();
	abstract int getTotal();  
}