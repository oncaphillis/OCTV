package net.oncaphillis.whatsontv;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import info.movito.themoviedbapi.model.config.Account;
import info.movito.themoviedbapi.model.config.TokenSession;
import info.movito.themoviedbapi.model.core.SessionToken;


public class LoginActivity extends Activity {
	private ProgressBar _loginProgress= null;
	private EditText    _userName     = null;
	private EditText    _password     = null;
	private Button      _loginButton  = null;
	private TextView    _loginState   = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
		_userName = (EditText) findViewById(R.id.user_input);
		_password = (EditText) findViewById(R.id.password_input);
		_loginProgress = (ProgressBar) findViewById(R.id.login_progress);
		_loginState    = (TextView) findViewById(R.id.login_state);
		
		_loginProgress.setVisibility(View.INVISIBLE);
		
		_loginButton = (Button) findViewById(R.id.login_button);
		
		_loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				class LoginPair {
					public String sessionid;
					public String username;
					LoginPair(String u,String i) {
						username = u;
						sessionid = i;
					}
				};
				
				final String u = _userName.getText().toString();
				final String p = _password.getText().toString();

				_loginProgress.setVisibility(View.VISIBLE);
				
				AsyncTask<String, Void, LoginPair> at = new AsyncTask<String, Void, LoginPair>() {

					@Override
					protected LoginPair doInBackground(String... params) {
						try {
							TokenSession s = Tmdb.get().api().getAuthentication().getSessionLogin(u, p);
							Account ac = Tmdb.get().api().getAccount().getAccount(new SessionToken(s.getSessionId()));
							
							return new LoginPair(ac.getUserName(),s.getSessionId());
						} catch(Exception ex) {
							return null;
						}
					}

				    protected void onPostExecute(LoginPair  p) {				    	
				    	_loginProgress.setVisibility(View.INVISIBLE);
				    	_password.setText("");
				    	if(p == null) {
				    		_loginState.setText("Login failed");
				    		_userName.setText("");
				    		_loginState.setTextColor(getResources().getColor(R.color.oncaphillis_orange));
				    	} else {
				    		_loginButton.setText("Logout");
				    		_loginState.setText("Logged in");
				    		_userName.setText(p.username);
				    		_loginState.setTextColor(getResources().getColor(R.color.oncaphillis_blue));				    		
				    	}
				    }
				};
				at.execute();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	        finish();
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}

}
