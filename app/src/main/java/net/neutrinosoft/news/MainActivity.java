package net.neutrinosoft.news;

import net.neutrinosoft.news.models.Response;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends ListActivity implements OnClickListener {

	private EditText etSearch = null;
	private Button btnSearch = null;
	private ProgressBar prBar = null;
	private SharedPreferences sPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		etSearch = (EditText) findViewById(R.id.etSearch);
		btnSearch = (Button) findViewById(R.id.btnSearch);
		prBar = (ProgressBar) findViewById(R.id.prBar2);
		btnSearch.setOnClickListener(this);
		if (isOnline()) {
			requestData(getResources().getString(R.string.search_url));
		} else {
			Toast.makeText(this, "Network isn't available!",
					Toast.LENGTH_LONG).show();
		}
	}

	private void requestData(String uri) {
		RequestPackage p = new RequestPackage();
		//p.setMethod("GET");
		p.setUri(uri);
		p.setHeader("UserId", getUserId());
		p.setParam("query", etSearch.getText().toString());
		new SearchTask().execute(p);
	}

	private String getUserId() {
		sPref = getSharedPreferences(LoginActivity.USER_ID, MODE_PRIVATE);
		return sPref.getString(LoginActivity.USER_ID, "");
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

	private class SearchTask extends AsyncTask<RequestPackage, String, String> {

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
				client.addHeader("UserId", getUserId());
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
			if (json != null) {
				prBar.setVisibility(View.INVISIBLE);
				Response response = JSONParser.parseNews(json);
				if (response.getInfo().getSuccess()) {
					NewsAdapter adapter = new NewsAdapter(getApplicationContext(),
							R.layout.item_news, response.getAds(), getUserId());
					setListAdapter(adapter);
				} else {
					Toast.makeText(getApplicationContext(), response.getInfo().getMessage(),
							Toast.LENGTH_LONG).show();
				}
			}
		}

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnSearch) {
			if (isOnline()) {
				requestData(getResources().getString(R.string.search_url));
			} else {
				Toast.makeText(this, "Network isn't available!",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_create:
			if (isOnline()) {
				Intent intent = new Intent(this, CreateActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(this, "Network isn't available!",
						Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.menu_logout:
			sPref = getSharedPreferences(LoginActivity.USER_ID, MODE_PRIVATE);
			Editor editor = sPref.edit();
			editor.clear();
			editor.commit();
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
