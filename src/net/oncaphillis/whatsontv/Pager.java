package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.List;

public interface Pager {
	List<TvSeries> getPage(int page);
	void start();
	void end();
}