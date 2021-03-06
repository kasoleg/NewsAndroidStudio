package net.neutrinosoft.news;

import net.neutrinosoft.news.models.Response;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity implements SearchView.OnQueryTextListener {

	private ProgressBar prBar;
	private SearchView search;
	private NewsAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (savedInstanceState == null) {
			prBar = (ProgressBar) findViewById(R.id.prBar2);
			requestData(getResources().getString(R.string.search_url), "");
		} else {
			adapter = new NewsAdapter(getApplicationContext(),
					R.layout.item_news, (List) savedInstanceState.getParcelableArrayList("news"), getUserId());
			setListAdapter(adapter);
			adapter.setMemoryCache((MemoryCache) savedInstanceState.getParcelable("bitmaps"));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("news", (ArrayList) adapter.getNewsList());
		outState.putParcelable("bitmaps", adapter.getMemoryCache());
	}

	private void requestData(String uri, String query) {
		RequestPackage p = new RequestPackage();
		p.setUri(uri);
		p.setHeader("UserId", getUserId());
		p.setParam("query", query);
		new SearchTask().execute(p);
	}

	private String getUserId() {
		MySharedPreferences sPref = new MySharedPreferences(this, MySharedPreferences.USER_ID, MODE_PRIVATE);
		return sPref.get(MySharedPreferences.USER_ID);
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
		// Catch event on [x] button inside search view
		int searchCloseButtonId = search.getContext().getResources()
				.getIdentifier("android:id/search_close_btn", null, null);
		ImageView closeButton = (ImageView) search.findViewById(searchCloseButtonId);
		// Set on click listener
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int searchTextViewId = search.getContext().getResources()
						.getIdentifier("android:id/search_src_text", null, null);
				TextView txSearch = (TextView) search.findViewById(searchTextViewId);
				txSearch.setText("");
				prBar = (ProgressBar) findViewById(R.id.prBar2);
				requestData(getResources().getString(R.string.search_url), "");
			}
		});

		search.setOnQueryTextListener(this);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_create:
			if (InternetUtils.isOnline(this)) {
				Intent intent = new Intent(this, CreateActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(this, "Network isn't available!",
						Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.menu_logout:
			MySharedPreferences sPref = new MySharedPreferences(this, MySharedPreferences.USER_ID, MODE_PRIVATE);
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
