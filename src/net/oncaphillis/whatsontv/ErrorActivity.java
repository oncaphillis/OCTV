package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ErrorActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String txt1 = this.getIntent().getExtras().getString("txt1");
		String txt2 = this.getIntent().getExtras().getString("txt2");
		setContentView(R.layout.activity_error);
		TextView tv1 = (TextView) findViewById(R.id.error_text1);
		TextView tv2 = (TextView) findViewById(R.id.error_text2);
		if(txt1!=null)
			tv1.setText(txt1);
		if(txt2!=null)
			tv2.setText(txt2);
		
		Button but = (Button) findViewById(R.id.error_done_button);
		but.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});;
	}
}
