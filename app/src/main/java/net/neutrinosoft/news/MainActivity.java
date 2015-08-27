package net.neutrinosoft.news;

import net.neutrinosoft.news.models.News;
import net.neutrinosoft.news.models.Response;

import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends ListActivity implements SearchView.OnQueryTextListener {

	private ProgressBar prBar = null;
	private SearchView search = null;
	private RetainedFragment dataFragment;
	private NewsAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// find the retained fragment on activity restarts
		FragmentManager fm = getFragmentManager();
		dataFragment = (RetainedFragment) fm.findFragmentByTag("data");

		// create the fragment and data the first time
		if (dataFragment == null) {
			// add the fragment
			dataFragment = new RetainedFragment();
			fm.beginTransaction().add(dataFragment, "data").commit();
			// load the data from the web
			//dataFragment.setData(loadMyData());
			prBar = (ProgressBar) findViewById(R.id.prBar2);
			requestData(getResources().getString(R.string.search_url), "");
		} else {
			// the data is available in dataFragment
			adapter = new NewsAdapter(getApplicationContext(),
					R.layout.item_news, dataFragment.getNewsList(), getUserId());
			setListAdapter(adapter);
			adapter.setMemoryCache(dataFragment.getMemoryCache());
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// store the data in the fragment
		List<News> l = adapter.getNewsList();
		MemoryCache m = adapter.getMemoryCache();
		dataFragment.setNewsList(adapter.getNewsList());
		dataFragment.setMemoryCache(adapter.getMemoryCache());
	}

	private void requestData(String uri, String query) {
        if (Utils.isOnline(this)) {
            RequestPackage p = new RequestPackage();
            p.setUri(uri);
            p.setHeader("UserId", getUserId());
            p.setParam("query", query);
            new SearchTask().execute(p);
        } else {
            Toast.makeText(this, "Network isn't available!",
                    Toast.LENGTH_LONG).show();
        }
	}

	private String getUserId() {
		MySharedPreferences sPref = new MySharedPreferences(this, LoginActivity.USER_ID, MODE_PRIVATE);
		return sPref.get(LoginActivity.USER_ID);
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
        return false;
	}



	@Override
	public boolean onQueryTextChange(String newText) {
        if (!newText.isEmpty()) {
            requestData(getResources().getString(R.string.search_url), newText);
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
					adapter = new NewsAdapter(getApplicationContext(),
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
			if (Utils.isOnline(this)) {
				Intent intent = new Intent(this, CreateActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(this, "Network isn't available!",
						Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.menu_logout:
			MySharedPreferences sPref = new MySharedPreferences(this, LoginActivity.USER_ID, MODE_PRIVATE);
			sPref.clearEditor();
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
