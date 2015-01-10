package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

public class ErrorActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String txt = this.getIntent().getExtras().getString("txt");
		setContentView(R.layout.activity_error);
		TextView tv = (TextView) findViewById(R.id.error_text);
		if(txt!=null)
			tv.setText(txt);
	}
}
