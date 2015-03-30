package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

public class NetWatchdog extends Thread {
	@Override
	
	public void run()  {	
		while(true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			
			final Activity act =Environment.getCurrentActivity();
			
			if(act!=null && !isOnline(act)) {
				
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Intent myIntent = new Intent( act, ErrorActivity.class);
						Bundle b        = new Bundle();
						b.putString("txt1", "NO NET");
						myIntent.putExtras(b);
						act.startActivity(myIntent);
					}
				});
			}
		}
	}
	
	static public boolean isOnline(Activity act) {
	    ConnectivityManager cm =
	        (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    return netInfo != null && netInfo.isConnectedOrConnecting();
	}
}
