package net.oncaphillis.whatsontv;

import java.io.ByteArrayOutputStream;

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

	private final static String _prefix = 			
    " <html>" +
	"   <body>" +
	"     <div style='font-height:1.5em; line-height:2em'>" +
	"       <div style='margin:8px'> "+
	"         <div style='float:left'> ";
	
	private final static String _middle= 
	"         </div> "+
	"         <h1>What's On TV "+Environment.VERSION+"</h1> "+ 
	"         <p> "+
	"          "+Environment.COPYRIGHT+"."+
	"          Licensed under the <a href='http://www.gnu.org/licenses/gpl-2.0.html'>GPLv2</a>. "+
	"          Source available at <a href='https://github.com/oncaphillis/WhatsOnTV'>github.com/oncaphillis/WhatsOnTV</a>. "+
	"          <i>'themoviedbapi'</i>&copy; 2014-15 by Holger Brandl. Icons made by  "+
	"           <a href='http://www.icomoon.io' title='Icomoon'>Icomoon</a>, "+
	"           <a href='http://catalinfertu.com' title='Catalin Fertu'>Catalin Fertu</a>, "+
	"           <a href='http://linhpham.me/miu' title='Linh Pham'>Linh Pham</a> "+
	"          from <a href='http://www.flaticon.com' title='Flaticon'>www.flaticon.com</a> "+ 
	"          is licensed by <a href='http://creativecommons.org/licenses/by/3.0/' title='Creative Commons BY 3.0'>CC BY 3.0</a> "+
	"         </p> "+ 
	"       </div> "+
	"       <hr> "+
	"       <div style='float:left'>";

	private final static String _postfix = 
 	"       </div>"+
	"       <p style='vertical-align:top'> "+ 
	"        This product uses the <a href='http://themoviedb.org'>TMDb</a> API but is not endorsed or certified by TMDb. "+ 
	"       </p> "+
	"     </div> "+
	"   </body> "+
	" </html> ";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_about);

		WebView  webview    = ((WebView) findViewById(R.id.about_webview));
		String debug = new String("");
		
		if(Tmdb.isDebug()) {
			debug = "<p>Series:"+Integer.toString(Tmdb.getSeriesCache().size())+";"+
						   "Seasons:"+Integer.toString(Tmdb.getSeasonsCache().size())+";"+
						   "Episodes:"+Integer.toString(Tmdb.getEpisodeCache().size())+";"+
						   "Bitmaps:"+Integer.toString(Tmdb.getBitmapCache().size())+"</p>";
		}
		
		webview.loadData(_prefix+getBitmapHtml(R.drawable.ic_launcher,new Integer(128),null)+
				_middle+getBitmapHtml(R.drawable.tmdb_logo,null,null)+debug+
				_postfix,"text/html; charset=utf-8;", "utf-8");
		
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
