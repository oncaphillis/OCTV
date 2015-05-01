package net.oncaphillis.whatsontv;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class TtlCache<K,V> {
	private int  _max_size=0;
	private int  _ttl = 0;
	private long _hit = 0;
	
	private class CacheNode {
		public long timestamp;
		public K key;
		public CacheNode(K k) {
			timestamp = TimeTool.getNow().getTime() /1000;
			key = k;
		}
		@Override
		public  boolean equals(Object n) {
			return n instanceof TtlCache.CacheNode && this.key.equals(((CacheNode)n).key); 
		}
	};
	
	private HashMap<K,V> _cacheMap = new HashMap<K,V>();

	private PriorityQueue<CacheNode> _cacheQueue = new PriorityQueue<CacheNode>(100,new Comparator<CacheNode>() {
		@Override
		public int compare(CacheNode o1, CacheNode o2) {
			return o1.timestamp<o2.timestamp ? -1 : o1.timestamp>o2.timestamp ? 1 : 0;
		}
	});
	
	public TtlCache(int ttl,int size) {
		_ttl      = ttl;
		_max_size = size;
	}
	
	public V get(K k) {
		purge();
		V v = _cacheMap.get(k);
		
		if(v!=null)
			_hit++;
		
		return v;
	}

	public long getHits() {
		return _hit;
	}
	
	public void put(K k,V v) {
		purge();
		_cacheQueue.remove(new CacheNode(k));
		_cacheQueue.add(new CacheNode(k));
		_cacheMap.put(k,v);
	}
	
	private void purge() {
		long n = TimeTool.getNow().getTime()  / 1000;
		while(!_cacheQueue.isEmpty() && (_cacheQueue.size() >= _max_size || (_cacheQueue.peek().timestamp - n) > _ttl)) {
			CacheNode c = _cacheQueue.poll();
			_cacheMap.remove(c.key);
		}
	}

	public long getSize() {
		return _cacheMap.size();
	}
}
