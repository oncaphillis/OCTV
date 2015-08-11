package net.oncaphillis.whatsontv;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class EntityInfoFragment extends Fragment {
	
	private Activity _activity = null;
	
	@Override
	public void onAttach(Activity act) {
        _activity = act;
        super.onAttach(act);
	}
	
	protected Activity getAttachedActivity() {
		return _activity;
	}
	
	protected String getBitmapHtml(Bitmap bm) {
		if(bm==null)
			return "";
		
	    // Convert bitmap to Base64 encoded image for web
	    
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
	    byte[] byteArray = byteArrayOutputStream.toByteArray();
	    String imgageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
	    
	    return "<img style='padding-right:0.5cm;padding-bottom:0.5cm;float:left;' src='"+
	    		"data:image/png;base64," + imgageBase64+"' />";
	}
}
