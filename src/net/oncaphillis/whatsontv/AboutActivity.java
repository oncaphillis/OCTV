package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.webkit.WebView;

public class AboutActivity extends Activity {

	private final String _cc = 			
		"<html>"
			 + "<body>"
			 + "<div>"
			 + "<div style='float:center'>"
			 + "Icons made by <a href='http://www.icomoon.io' title='Icomoon'>Icomoon</a>,"
			 + "<a href='http://catalinfertu.com' title='Catalin Fertu'>Catalin Fertu</a>,"
			 + "<a href='http://linhpham.me/miu' title='Linh Pham'>Linh Pham</a>"
			 + " from <a href='http://www.flaticon.com' title='Flaticon'>www.flaticon.com</a>"
			 + " is licensed by <a href='http://creativecommons.org/licenses/by/3.0/' title='Creative Commons BY 3.0'>CC BY 3.0</a>"
			 + "</div>"
			 + "</div>"
			 + "</body>"
			 + "</html>";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		setContentView(R.layout.activity_about);


		WebView  webview    = ((WebView) findViewById(R.id.about_webview));
	
		webview.loadData(_cc,"text/html; charset=utf-8;", "utf-8");
		
	}
}
