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

	class ViewHolder {
		TextView tvName;
		TextView tvDescription;
		ImageView ivNews;
		TextView tvCreatedAt;
		ViewHolder(View view) {
			tvName = (TextView) view.findViewById(R.id.tvName);
			tvDescription = (TextView) view
					.findViewById(R.id.tvDescription);
			tvCreatedAt = (TextView) view.findViewById(R.id.tvCreatedAt);
			ivNews = (ImageView) view.findViewById(R.id.ivNews);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder viewHolder;

		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.item_news, parent, false);
			viewHolder = new ViewHolder(row);
			row.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) row.getTag();
		}

		News news = newsList.get(position);
		viewHolder.tvName.setText(news.getName());
		viewHolder.tvDescription.setText(news.getDescription().trim());
		viewHolder.tvCreatedAt.setText(news.getCreatedAt());
		Bitmap bitmap = memoryCache.get(news.getId());
		if (bitmap != null) {
			viewHolder.ivNews.setImageBitmap(bitmap);
		} else {
			NewsAndView container = new NewsAndView();
			container.news = news;
			container.viewHolder = viewHolder;
			new ImageLoader().execute(container);
		}
		return row;

	}

	class NewsAndView {
		public News news;
		public ViewHolder viewHolder;
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
			//ImageView ivNews = (ImageView) result.view
			//		.findViewById(R.id.ivNews);
			result.viewHolder.ivNews.setImageBitmap(scaledBitmap);
			memoryCache.put(result.news.getId(), scaledBitmap);
		}

	}

	public void setNewsList(List<News> newsList) {
		this.newsList = newsList;
	}

	public List<News> getNewsList() {
		return newsList;
	}

	public void setMemoryCache(MemoryCache memoryCache) {
		this.memoryCache = memoryCache;
	}

	public MemoryCache getMemoryCache() {
		return memoryCache;
	}

	public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
								   boolean filter) {
		float ratio = Math.min(
				(float) maxImageSize / realImage.getWidth(),
				(float) maxImageSize / realImage.getHeight());
		int width = Math.round((float) ratio * realImage.getWidth());
		int height = Math.round((float) ratio * realImage.getHeight());

		return Bitmap.createScaledBitmap(realImage, width,
				height, filter);
	}
}