package net.neutrinosoft.news;

import net.neutrinosoft.news.models.User;
import android.app.Activity;
import android.content.Intent;
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

	private EditText etLogin;
	private EditText etPassword;
	private ProgressBar prBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		MySharedPreferences sPref = new MySharedPreferences(this, MySharedPreferences.USER_ID, MODE_PRIVATE);
		if (sPref.contains(MySharedPreferences.USER_ID)) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		etLogin = (EditText) findViewById(R.id.etLogin);
		etPassword = (EditText) findViewById(R.id.etPassword);
		Button btnLogin = (Button) findViewById(R.id.btnLogin);
		TextView tvRegister = (TextView) findViewById(R.id.tvRegister);
		prBar = (ProgressBar) findViewById(R.id.prBar);
		btnLogin.setOnClickListener(this);
		tvRegister.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLogin:
			requestData(getResources().getString(R.string.login_url));
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

	private class LoginTask extends AsyncTask<RequestPackage, String, String> {

		@Override
		protected void onPreExecute() {
			if (InternetUtils.isOnline(getApplicationContext())) {
				prBar.setVisibility(View.VISIBLE);
			} else {
				Toast.makeText(getApplicationContext(), "Network isn't available!",
						Toast.LENGTH_LONG).show();
			}
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
			if (user.getInfo().getSuccess()) {
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
			MySharedPreferences sPref = new MySharedPreferences(getApplicationContext(), MySharedPreferences.USER_ID, MODE_PRIVATE);
			sPref.put(MySharedPreferences.USER_ID, userId);
		}

	}

}
