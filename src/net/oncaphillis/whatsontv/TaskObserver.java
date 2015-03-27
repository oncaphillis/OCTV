package net.oncaphillis.whatsontv;

import java.util.Iterator;
import java.util.WeakHashMap;

import android.os.AsyncTask;

abstract public class TaskObserver {
	
	private WeakHashMap<Thread,Void>    _tMap = new WeakHashMap<Thread,Void>();
	private WeakHashMap<AsyncTask,Void> _aMap = new WeakHashMap<AsyncTask,Void>();

	public void add(Thread t) {
		synchronized(this) {
			_tMap.put(t, null);
			if(_tMap.size()+_aMap.size() == 1);
				start();	
		}
	}

	public void add(AsyncTask a) {
		synchronized(this) {
			_aMap.put(a, null);
			if(_tMap.size()+_aMap.size() == 1);
				start();	
		}
	}

	private void start() {
		new Thread() {
			private TaskObserver _obs = TaskObserver.this;
			@Override
			public void run() {
				int n = 0;
				while(true) {
					synchronized(_obs) {
						boolean b = true;
						{
							Iterator<Thread> i = _tMap.keySet().iterator();
							while(i.hasNext()) {
								if ( i.next().isAlive()) {
									b = false;
									break;
								}
							}
						}
						
						if(b) {
							Iterator<AsyncTask> i = _aMap.keySet().iterator();
							while(i.hasNext()) {
								if ( i.next().getStatus() != AsyncTask.Status.FINISHED ) {
									b = false;
									break;
								}
							}
							
						}
						
						if(b) {
							if( n > 0)
								endProgress();
							break;
						}
					} 

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						break;
					}

					if(n == 0)
						beginProgress();
					n++;
				}
			}
		}.start();
	}

	abstract void beginProgress();
	abstract void endProgress();
}
