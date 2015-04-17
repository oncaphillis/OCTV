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
			
			if(act!=null && !(act instanceof NonetActivity) && !isOnline(act)) {
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Intent myIntent = new Intent( act, NonetActivity.class);
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

	public static boolean isOnline() {
		Activity act = Environment.getCurrentActivity();
		return act == null ? false : isOnline(act);
	}
}
