package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NonetActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nonet);
		Button but = (Button) findViewById(R.id.nonet_button);
		but.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		final Activity act = this;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!NetWatchdog.isOnline()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				act.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						act.finish();
					}
				});
			}
		});
		t.start();
	}
		
	@Override
	protected void	onResume() {
		super.onResume();
	    Environment.setCurrentActivity(this);
	}
	
	@Override
	protected void	onPause() {
		clearReferences();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		clearReferences();
		super.onDestroy();
	}
	
	private void clearReferences(){
      Activity currActivity = Environment.getCurrentActivity();
      if (currActivity!=null && currActivity.equals(this))
            Environment.setCurrentActivity(null);
	}
}
