package net.oncaphillis.whatsontv;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.config.TmdbTimezones;
import info.movito.themoviedbapi.model.config.TokenSession;
import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;


public class LoginActivity extends Activity {
	private ProgressBar _loginProgress= null;
	private EditText    _userName     = null;
	private EditText    _password     = null;
	private Button      _loginButton  = null;
	private TextView    _loginState   = null;
	
	class TzCollection {
		TreeMap<String,TreeSet<String> > _zoneMap = new TreeMap<String,TreeSet<String>>();
		TzCollection(TmdbTimezones tz) {

			for(String t : tz) {
				if(t.indexOf('/')>0) {
					String a = t.substring(0,t.indexOf('/'));
					String b = t.substring(t.indexOf('/')+1);
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
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		_userName = (EditText) findViewById(R.id.user_input);
		_password = (EditText) findViewById(R.id.password_input);
		_loginProgress = (ProgressBar) findViewById(R.id.login_progress);
		_loginState    = (TextView) findViewById(R.id.login_state);
		
		_loginProgress.setVisibility(View.INVISIBLE);
		
		_loginButton = (Button) findViewById(R.id.login_button);
		OnClickListener l;
		
		_loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String u = _userName.getText().toString();
				final String p = _password.getText().toString();

				_loginProgress.setVisibility(View.VISIBLE);

				AsyncTask<String, Void, String> at = new AsyncTask<String, Void, String>() {

					@Override
					protected String doInBackground(String... params) {
						try {
							return Tmdb.get().api().getAuthentication().getSessionLogin(u, p).getSessionId();
						} catch(Exception ex) {
							return ex.getMessage();
						}
					}
				    // Once the image is downloaded, associates it to the imageView
				    protected void onPostExecute(String  si) {				    	
				    	_loginProgress.setVisibility(View.INVISIBLE);
				    	_loginState.setText(si);
				    }
				};
				at.execute();
			}
		});
		

		final Spinner spinner_z = (Spinner) findViewById(R.id.time_zone_select);
		final Spinner spinner_c = (Spinner) findViewById(R.id.time_city_slect);
		
		// Create an ArrayAdapter using the string array and a default spinner layout

		final Activity  act = this; 
		final TzCollection tc  = new TzCollection(Tmdb.get().api().getTimezones());
		
		
		spinner_z.setAdapter(new BaseAdapter() {

			@Override
			public int getCount() {
				return tc.getZones().length;
			}
			@Override
			public Object getItem(int position) {
				Object[] a = tc.getZones();
				return a[position];
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if(convertView==null) {
			        LayoutInflater vi = LayoutInflater.from(act);
			        convertView  = vi.inflate(R.layout.login_spinner_entry, null);	
				}
		        TextView tv = (TextView)convertView.findViewById(R.id.login_spinner_textview);
				tv.setText((String)getItem(position));
				return convertView;
			}
		});

		class CityAdapter extends BaseAdapter {
			private int _zone = 0; 
			public void setZone(int z) {
				_zone = z;
				super.notifyDataSetChanged();
			}
			
			@Override
			public int getCount() {
				return tc.getCities((String)tc.getZones()[_zone]).size();
			}

			@Override
			public Object getItem(int position) {
				return tc.getCities((String)tc.getZones()[_zone]).toArray()[position];			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if(convertView==null) {
			        LayoutInflater vi = LayoutInflater.from(act);
			        convertView  = vi.inflate(R.layout.login_spinner_entry, null);	
				}
		        TextView tv = (TextView)convertView.findViewById(R.id.login_spinner_textview);
				tv.setText((String)getItem(position));
				return convertView;
			}
		};
		
		final CityAdapter ca= new CityAdapter();
		
		spinner_c.setAdapter(ca);
		OnItemSelectedListener a;
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
}
