package net.neutrinosoft.news;

import net.neutrinosoft.news.models.User;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	static final String USER_ID = "userID";

	private EditText etLogin = null;
	private EditText etPassword = null;
	private Button btnLogin = null;
	private TextView tvRegister = null;
	private ProgressBar prBar = null;
	private SharedPreferences sPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sPref = getSharedPreferences(USER_ID, MODE_PRIVATE);
		if (sPref.contains(USER_ID)) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		etLogin = (EditText) findViewById(R.id.etLogin);
		etPassword = (EditText) findViewById(R.id.etPassword);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		tvRegister = (TextView) findViewById(R.id.tvRegister);
		prBar = (ProgressBar) findViewById(R.id.prBar);
		btnLogin.setOnClickListener(this);
		tvRegister.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLogin:
			if (isOnline()) {
				requestData(getResources().getString(R.string.login_url));
			} else {
				Toast.makeText(this, "Network isn't available!",
						Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.tvRegister:
			startActivity(new Intent(this, RegisterActivity.class));
			break;
		default:
			break;
		}

	}

	private void requestData(String uri) {
		RequestPackage p = new RequestPackage();
		p.setUri(uri);
		p.setParam("login", etLogin.getText().toString());
		p.setParam("password", etPassword.getText().toString());
		new LoginTask().execute(p);
	}
	
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		} else {
			return false;
		}
	}

	private class LoginTask extends AsyncTask<RequestPackage, String, String> {

		@Override
		protected void onPreExecute() {
			prBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(RequestPackage... params) {
			String json = null;
			RequestPackage p = params[0];
			HttpClient client = new HttpClient(p.getUri());
			try {
				client.connectForMultipart();
				for (String key : p.getParams().keySet()) {
					client.addFormPart(key, p.getParams().get(key));
				}
				client.finishMultipart();
				json = client.getResponse();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return json;
		}

		@Override
		protected void onPostExecute(String json) {
			prBar.setVisibility(View.INVISIBLE);
			User user = JSONParser.parseUser(json);
			if (user.getInfo().getSuccess() == true) {
				Toast.makeText(getApplicationContext(),
						"You are login successfully!",
						Toast.LENGTH_LONG).show();
				saveUserId(user.getUserId());
				Intent intent = new Intent(getApplicationContext(),
						MainActivity.class);
				startActivity(intent);
				finish();
			} else {
				Toast.makeText(getApplicationContext(), user.getInfo().getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}

		private void saveUserId(String userId) {
			sPref = getSharedPreferences(USER_ID, MODE_PRIVATE);
			Editor editor = sPref.edit();
			editor.putString(USER_ID, userId);
			editor.commit();
		}

	}

}
