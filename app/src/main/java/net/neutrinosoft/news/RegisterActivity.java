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
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {

	private EditText etLogin;
	private EditText etPassword;
	private Button btnRegister;
	private ProgressBar prBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		etLogin = (EditText) findViewById(R.id.etLogin);
		etPassword = (EditText) findViewById(R.id.etPassword);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		prBar = (ProgressBar) findViewById(R.id.prBar);
		btnRegister.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnRegister) {
			String login = etLogin.getText().toString();
			String password = etPassword.getText().toString();
			if (login.isEmpty()) {
				Toast.makeText(this, "Login is empty!",
						Toast.LENGTH_LONG).show();
				return;
			}
			if (password.isEmpty()) {
				Toast.makeText(this, "Password is empty!",
						Toast.LENGTH_LONG).show();
				return;
			}
			requestData(getResources().getString(R.string.register_url));
		}
	}
	
	private void requestData(String uri) {
		if (Utils.isOnline(this)) {
			RequestPackage p = new RequestPackage();
			p.setUri(uri);
			p.setParam("login", etLogin.getText().toString());
			p.setParam("password", etPassword.getText().toString());
			new RegisterTask().execute(p);
		} else {
			Toast.makeText(this, "Network isn't available!",
					Toast.LENGTH_LONG).show();
		}
	}
	
	private class RegisterTask extends AsyncTask<RequestPackage, String, String> {

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
			if (user.getInfo().getSuccess()) {
				Toast.makeText(getApplicationContext(),
						"You are register successfully!",
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
			MySharedPreferences sPref = new MySharedPreferences(getApplicationContext(), LoginActivity.USER_ID, MODE_PRIVATE);
			sPref.put(LoginActivity.USER_ID, userId);
		}

	}

}