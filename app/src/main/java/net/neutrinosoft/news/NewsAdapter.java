package net.neutrinosoft.news;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import net.neutrinosoft.news.models.News;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsAdapter extends ArrayAdapter<News> {

	private Context context;
	private List<News> newsList;
	private String imageUrl;
	private String userId;

	private MemoryCache memoryCache;

	public NewsAdapter(Context context, int resource, List<News> objects,
			String userId) {
		super(context, resource, objects);
		this.context = context;
		this.newsList = objects;
		this.imageUrl = context.getResources().getString(R.string.image_url);
		this.userId = userId;

		memoryCache = new MemoryCache();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.item_news, parent, false);

		News news = newsList.get(position);
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		tvName.setText(news.getName());

		TextView tvDesctription = (TextView) view
				.findViewById(R.id.tvDescription);
		tvDesctription.setText(news.getDescription().trim());

		TextView tvCreatedAt = (TextView) view.findViewById(R.id.tvCreatedAt);
		tvCreatedAt.setText(news.getCreatedAt());
		Bitmap bitmap = memoryCache.get(news.getId());
		if (bitmap != null) {
			ImageView ivNews = (ImageView) view.findViewById(R.id.ivNews);
			ivNews.setImageBitmap(bitmap);
		} else {
			NewsAndView container = new NewsAndView();
			container.news = news;
			container.view = view;
			new ImageLoader().execute(container);
		}
		return view;
		
	}

	class NewsAndView {
		public News news;
		public View view;
		public Bitmap bitmap;
	}

	
	private class ImageLoader extends AsyncTask<NewsAndView, Void, NewsAndView> {

		@Override
		protected NewsAndView doInBackground(NewsAndView... params) {
			NewsAndView container = params[0];
			News news = container.news;

			HttpURLConnection connection = null;
			try {
				URL url = new URL(imageUrl + news.getId());
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.addRequestProperty("UserId", userId);
				connection.connect();
				int responseCode = connection.getResponseCode();
				if (responseCode == 200) {
					container.bitmap = BitmapFactory.decodeStream(connection
							.getInputStream());
					return container;
				} else
					return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}

		@Override
		protected void onPostExecute(NewsAndView result) {
			Bitmap scaledBitmap = scaleDown(result.bitmap, 300, true);
			ImageView ivNews = (ImageView) result.view
					.findViewById(R.id.ivNews);
			ivNews.setImageBitmap(scaledBitmap);
			memoryCache.put(result.news.getId(), scaledBitmap);
		}

	}
		
	public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
	        boolean filter) {
	    float ratio = Math.min(
	            (float) maxImageSize / realImage.getWidth(),
	            (float) maxImageSize / realImage.getHeight());
	    int width = Math.round((float) ratio * realImage.getWidth());
	    int height = Math.round((float) ratio * realImage.getHeight());

	    Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
	            height, filter);
	    return newBitmap;
	}
}
