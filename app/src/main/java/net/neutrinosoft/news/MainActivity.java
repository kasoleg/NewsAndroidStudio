package net.neutrinosoft.news;

import net.neutrinosoft.news.models.Response;
import android.app.ListActivity;
import android.app.SearchManager;
import android.app.SearchableInfo;
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
import android.widget.SearchView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends ListActivity implements SearchView.OnQueryTextListener {

	private ProgressBar prBar = null;
	private SearchView search = null;
	private SharedPreferences sPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		prBar = (ProgressBar) findViewById(R.id.prBar2);
		if (isOnline()) {
			requestData(getResources().getString(R.string.search_url), "");
		} else {
			Toast.makeText(this, "Network isn't available!",
					Toast.LENGTH_LONG).show();
		}
	}

	private void requestData(String uri, String query) {
		RequestPackage p = new RequestPackage();
		p.setUri(uri);
		p.setHeader("UserId", getUserId());
		p.setParam("query", query);
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

	@Override
	public boolean onQueryTextSubmit(String query) {
        if (isOnline()) {
            requestData(getResources().getString(R.string.search_url), query);
            return true;
        } else {
            Toast.makeText(this, "Network isn't available!",
                    Toast.LENGTH_LONG).show();
            return false;
        }
	}

	@Override
	public boolean onQueryTextChange(String newText) {
        if (newText.isEmpty()) {
            if (isOnline()) {
                requestData(getResources().getString(R.string.search_url), "");
                return true;
            } else {
                Toast.makeText(this, "Network isn't available!",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return false;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		search = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        search.setOnQueryTextListener(this);
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
