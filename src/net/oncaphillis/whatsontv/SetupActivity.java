package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.config.Timezone;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class SetupActivity extends Activity {

	
	private class TzCollection {
		TreeMap<String,TreeSet<String> > _zoneMap = new TreeMap<String,TreeSet<String>>();
		TzCollection(List<Timezone> tzl) {

			for(Timezone tz : tzl) {
				if(tz.getName().indexOf('/')>0) {
					String a = tz.getName().substring(0,tz.getName().indexOf('/'));
					String b = tz.getName().substring(tz.getName().indexOf('/')+1);
					if(_zoneMap.get(a)==null)
						_zoneMap.put(a,new TreeSet<String>());
					_zoneMap.get(a).add(b);
				}
			}
		}
		Object[] getZones() {
			return _zoneMap.keySet().toArray();
		}
		Set<String> getCities(String zone) {
			return _zoneMap.get(zone);
		}
	};

	private abstract class TzAdapter extends BaseAdapter {
		protected TzCollection _tzcollection = null;
		protected Activity _act = null;
		public TzAdapter(Activity a,TzCollection tc) {
			_tzcollection = tc;
			_act = a;
		}
		@Override
		public long getItemId(int position) {
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null) {
		        LayoutInflater vi = LayoutInflater.from(_act);
		        convertView  = vi.inflate(R.layout.login_spinner_entry, null);	
			}
	        TextView tv = (TextView)convertView.findViewById(R.id.login_spinner_textview);
			tv.setText((String)getItem(position));
			return convertView;
		}
	}
	
	private class ContinentAdapter  extends  TzAdapter {
		ContinentAdapter(Activity a,TzCollection tc) {
			super(a,tc);
		}
		@Override
		public int getCount() {
			return _tzcollection.getZones().length;
		}
		@Override
		public Object getItem(int position) {
			Object[] a = _tzcollection.getZones();
			return a[position];
		}
	};

	private class CityAdapter extends TzAdapter {
		private int _zone = 0; 
		public CityAdapter(Activity a,TzCollection tc) {
			super(a,tc);
		}
		public void setZone(int z) {
			_zone = z;
			super.notifyDataSetChanged();
		}
		@Override
		public int getCount() {
			return _tzcollection.getCities((String)_tzcollection.getZones()[_zone]).size();
		}

		@Override
		public Object getItem(int position) {
			return _tzcollection.getCities((String)_tzcollection.getZones()[_zone]).toArray()[position];			}
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		
		final Spinner spinner_z = (Spinner) findViewById(R.id.time_zone_select);
		final Spinner spinner_c = (Spinner) findViewById(R.id.time_city_slect);
				
		final Activity  act = this; 
		final TzCollection tc  = new TzCollection(Tmdb.get().getTimezones());
		Locale[] loclist = Locale.getAvailableLocales();

		final CityAdapter ca =new CityAdapter(this,tc);
		spinner_z.setAdapter( new ContinentAdapter(this,tc) );
		spinner_c.setAdapter( ca );

		spinner_z.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				ca.setZone(position);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}
