package net.oncaphillis.whatsontv;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {

	private final Bitmap    _defBitmap;
	private String          _path;
	private ProgressBar     _pb;
	private WeakReference<ImageView> _imageView;
	private Activity         _act;
	private ProgressBar     _pb_group;
	
    public BitmapDownloaderTask(ImageView imageView, Activity act,ProgressBar pb, Bitmap defBitmap,ProgressBar pb_group) {
        _imageView = new WeakReference<ImageView>(imageView);
        _path      = imageView == null || imageView.getTag() == null ? null : imageView.getTag().toString();
        _defBitmap = defBitmap; 
        _pb        = pb;
        _act       = act;
        _pb_group  = pb_group;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
    	if(_imageView!=null && _imageView.get()!=null &&
    			_imageView.get().getTag()!=null &&
    			_imageView.get().getTag().toString().equals(_path)) 
    		return Tmdb.get().loadPoster(0,_path,_act,_pb);      		 
    	else {
    		_pb.setVisibility(View.INVISIBLE);
    		return null;
    	}
    }

    @Override
    // Once the image is downloaded, associates it to the imageView
    protected void onPostExecute(Bitmap bm) {
    	if(bm!=null && _imageView!=null && _imageView.get()!=null &&
    			_imageView.get().getTag()!=null &&
    			_imageView.get().getTag().toString().equals(_path)) 
       		_imageView.get().setImageBitmap(bm);
    
    	if(_pb_group!=null && _pb_group.getTag()!=null) {
    		Integer c=(Integer)_pb_group.getTag();
    		if(c>0) {
    			c--;
    			_pb_group.setTag(new Integer(c));
    		}
    		if(c==0) {
    			_pb_group.setVisibility(View.INVISIBLE);
    		}
    	}
    }
}