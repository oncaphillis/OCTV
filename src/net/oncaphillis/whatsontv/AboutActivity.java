package net.oncaphillis.whatsontv;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;

public class AboutActivity extends Activity {

	private final static String _html = 			
			"<!DOCTYPE HTML PUBLIC -//W3C//DTD HTML 4.01 Transitional//EN>"+
					" <html> "+
					"  <body> "+
					"    <div style='font-height:1.5em; line-height:2em; font-family:helvetica'> "+
					"      <div style='margin:8px'> "+
					"       <div style='float:left'> "+
					"         %s"+
					"       </div> "+
					"       <h1>%s %s</h1>  "+
					"         <p style='font-size:80%%'>(%s)</p>"+
					"       <p> "+
					"         %s"+
					"         Licensed under the <a href='http://www.gnu.org/licenses/gpl-2.0.html'>GPLv2</a>. "+
					"         Source available at <a href='https://github.com/oncaphillis/%s'>github.com/oncaphillis/%s</a>. "+
					"         <i>'themoviedbapi'</i>&copy; 2014-15 by Holger Brandl. "+
					"         <i>'trakt-java'</i>&copy; 2013-2015 by Uwe Trottmann."+
					"         Icons made by  "+
					"         <a href='http://www.icomoon.io' title='Icomoon'>Icomoon</a>, "+
					"         <a href='http://catalinfertu.com' title='Catalin Fertu'>Catalin Fertu</a>, "+
					"         <a href='http://linhpham.me/miu' title='Linh Pham'>Linh Pham</a> "+
					"         from <a href='http://www.flaticon.com' title='Flaticon'>www.flaticon.com</a>  "+
					"         is licensed by <a href='http://creativecommons.org/licenses/by/3.0/' title='Creative Commons BY 3.0'>CC BY 3.0</a> "+
					"       </p>  "+
					"       <hr>"+
					"         <div>"+
					"           <div style'display:inline-block'>"+
					"             <b>Powered by: <a href='http://themoviedb.org'>TMDB</a> and <a href='http://trakt.tv'>Trakt.tv</a></b>"+
					"           </div>"+
					"           <div style='float:left;display:inline-block'>"+
					"               %s"+
					"           </div>"+
					"           <div style='display:inline-block'>"+
					"               %s"+
					"           </div>"+
					"               <div style='font-size:80%%'>This product uses the TMDB API but is not endorsed or certified by TMDB.</div>"+
					"         </div>"+
					"       </div> "+
					"         %s"+
					"       </div>"+
					"       <div>"+
					"       </div>"+
					"  </body> "+
					" </html> ";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_about);

		WebView  webview    = ((WebView) findViewById(R.id.about_webview));
		String debug = new String("");
		
		if(Tmdb.isDebug()) {

			File f = webview.getContext().getDatabasePath(Environment.NAME);
			long dbSize = f.length();
			debug = "<div>"+
					"<p>Series[pre:"+Long.toString(Tmdb.getSeriesPreCache().getSize())+"/"+Long.toString(Tmdb.getSeriesPreCache().getHits())+" "
								+"back:"+Long.toString(Tmdb.getSeriesCacheSize())+"/"+Integer.toString(Tmdb.getSeriesCacheHits())+"]<br>"+
					    "Seasons:[pre:"+Long.toString(Tmdb.getSeasonPreCache().getSize())+"/"+Long.toString(Tmdb.getSeasonPreCache().getHits())+" "
								+"back:"+Long.toString(Tmdb.getSeasonsCacheSize())+"/"+Integer.toString(Tmdb.getSeasonsCacheHits())+"]<br>"+
						"Episodes:"+Integer.toString(Tmdb.getEpisodeCache().size())+"/"+Integer.toString(Tmdb.getEpisodeCache().hit())+"<br>"+
					    "Bitmaps:[pre:"+Long.toString(Tmdb.getBitmapCache().getPreCache().getSize())+"/"+Long.toString(Tmdb.getBitmapCache().getPreCache().getHits())+" "+
					    		"back:"+String.format("%.2f MB" ,(float)Tmdb.getBitmapCache().getSize() / (float)(1024*1024))+" "+
					    		Integer.toString(Tmdb.getBitmapCache().getCount())+"/"+
					    		Integer.toString(Tmdb.getBitmapCache().getHits())+"<br>"+
						   " DbMaxSize:"+String.format("%.2f MB",(float)Environment.CacheHelper.getWritableDatabase().getMaximumSize()/(float)(1024*1024))+
						   " DbSize:"+String.format("%.2f MB", (float)dbSize/(1024*1024))+"<br>"+
						   " Trakt: lookup:"+Long.toString(Tmdb.get().trakt_reader().getLookupCount())+
						   		" Load:"+Long.toString(Tmdb.get().trakt_reader().getLoadCount())+
						   		" Hit:"+Long.toString(Tmdb.get().trakt_reader().getHitCount())+"<br>"+
						   	" SeriesInfo: size: "+Long.toString(SeriesInfo.getCacheSize())+" hits:"+Long.toString(SeriesInfo.getCacheHits())+"<p/>"+
						   	" SQL: SELECT:"+ Tmdb.getSqlSelectCount()+" INSERT:"+Long.toString(Tmdb.getSqlInsertCount())+" DELETE:"+Tmdb.getSqlDelete()+"<p/>"+
						   	" Lists: #"+Integer.toString(Environment.ListAdapters[0].getCount())+
						   		" #"+Integer.toString(Environment.ListAdapters[1].getCount())+
						   		" #"+Integer.toString(Environment.ListAdapters[2].getCount())+
						   		" #"+Integer.toString(Environment.ListAdapters[3].getCount())+
						   	"</p>"+
						   "</div>";
		}
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(Environment.BUILD_DATE);
		webview.loadData(String.format(_html, 
				getBitmapHtml(R.drawable.ic_launcher,new Integer(128),null),
				Environment.NAME,
				Environment.VERSION,
				Environment.TimeFormater.format(c.getTime()),
				Environment.COPYRIGHT,
				Environment.NAME,
				Environment.NAME,
				getBitmapHtml(R.drawable.tmdb_logo,null,null),
				getBitmapHtml(R.drawable.trakt_logo,null,null),
				debug),
				"text/html; charset=utf-8;", "utf-8");
		
        ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	
	}
	
	private String getBitmapHtml(Bitmap bm,Integer w,Integer h) {
		if(bm==null)
			return "";
		
	    // Convert bitmap to Base64 encoded image for web
	    
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
	    byte[] byteArray = byteArrayOutputStream.toByteArray();
	    String imgageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
	    
	    String ws="";
	    String hs="";
	    
	    if(w != null) {
	    	ws = "width='"+w.toString()+"'";
	    	hs = "height='auto'";
	    }
	    
	    if(h != null) {
	    	hs = "height='"+h.toString()+"'";
	    	if(ws.equals("")) {
	    		ws="width='auto'";
	    	}
	    }
	    
	    return "<img "+ws+" "+hs+" style='max-width:100%;padding-right:0.5cm;padding-bottom:0.5cm;float:left;' src='"+
	    		"data:image/png;base64," + imgageBase64+"' />";
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	        finish();
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	private String getBitmapHtml(int id,Integer w,Integer h)  {
		return getBitmapHtml(((BitmapDrawable)getResources().getDrawable(id)).getBitmap(),w,h);
	}
}
