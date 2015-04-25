package net.oncaphillis.whatsontv;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.PriorityQueue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * A cash for Bitmap objects we retrieve from the Web. It holds 
 * references until a threshold value is reached. After that
 * Bitmaps get deleted. 
 * 
 * @author kloska
 *
 */

class BitmapCache {
	private int _hit = 0; 
	private File _cacheDir = null;

	private PriorityQueue<File> _queue = new PriorityQueue<File>(100, new Comparator<File>() {
		@Override
		public int compare(File o1, File o2) {
			return o1.lastModified()<o2.lastModified() ? -1 : o1.lastModified() > o2.lastModified() ? 1 : 0;
		}
	});

	BitmapCache(File cashDir) {
		_cacheDir = cashDir;
		if(_cacheDir.exists()) {
			// walk through  
			File[] fl = _cacheDir.listFiles();
			for(File f : fl) {
				//Age in seconds
				_size += f.length();
				_queue.add(f);
			}
		}
		purge();
	}
	
	private int _size = 0;
	
	static final int MAX_SIZE=20000000; 
	
	/** Stores a new Bitmap under a given Key. If the max size of 
	 * the cache is exceeded we delete images until we are below
	 * the treshold.
	 * 
	 * @param bm
	 * @param size
	 * @param path
	 */
	
	void put(Bitmap bm,int size,String path) {

		File f = new File(_cacheDir,Integer.toString(size)+"_"+new File(path).getName());
		
		try {
			FileOutputStream os = new FileOutputStream(f.getAbsolutePath());
			bm.compress(Bitmap.CompressFormat.JPEG, 85, os);
			os.close();
			_size += f.length();
			_queue.add(f);
			purge();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	Bitmap get(int size,String path) {
		File f = new File(_cacheDir,Integer.toString(size)+"_"+new File(path).getName());
		long n = TimeTool.getNow().getTime();
		if(f.exists()) {
			if( ((n-f.lastModified()) / 1000.0f) > Environment.TTL) {
				f.delete();
				_queue.remove(f);
				return null;
			}
			_hit++;
			Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
			return bm;
		}
		return null;
	}

	public int getCount() {
		return _queue.size();
	}
	
	public int getSize() {
		return _size;
	}

	public int getHits() {
		return _hit;
	}
	
	private void purge() {
		long n = TimeTool.getNow().getTime();
		while(!_queue.isEmpty() && (_size>MAX_SIZE || ((n-_queue.peek().lastModified())/1000.0) > Environment.TTL)) {
			File f = _queue.remove();
			long m=f.lastModified(); 
			if(f.exists()) {
				_size-=f.length();
				f.delete();
			}
		}
	}
}