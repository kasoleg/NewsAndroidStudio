package net.neutrinosoft.news;

import java.util.ArrayList;
import java.util.List;

import net.neutrinosoft.news.models.Info;
import net.neutrinosoft.news.models.News;
import net.neutrinosoft.news.models.Response;
import net.neutrinosoft.news.models.User;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {

	public static User parseUser(String json) {

		try {
			User user = new User();
			Info info = new Info();

			JSONObject object = new JSONObject(json);
			
			user.setUserId(object.getString("id"));
			info.setSuccess(object.getBoolean("success"));
			info.setMessage(object.getString("message"));
			user.setInfo(info);

			return user;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Response parseNews(String json) {
		
		try {
			JSONObject responseObject = new JSONObject(json);

			Response response = new Response();
			Info info = new Info();
			info.setSuccess(responseObject.getBoolean("success"));
			info.setMessage(responseObject.getString("message"));
			response.setInfo(info);
			if (responseObject.optJSONArray("ads") != null) {
				JSONArray array = responseObject.getJSONArray("ads");
				List<News> newsList = new ArrayList<News>();

				for (int i = 0; i < array.length(); i++) {

					JSONObject object = array.getJSONObject(i);
					News news = new News();

					news.setId(object.getString("id"));
					news.setName(object.getString("name"));
					news.setDescription(object.getString("description"));
					news.setCreatedAt(object.getString("createdAt"));
					newsList.add(news);
				}
				response.setAds(newsList);
			}
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Info parseMessage(String json) {
		try {
			Info info = new Info();

			JSONObject object = new JSONObject(json);
			info.setSuccess(object.getBoolean("success"));
			info.setMessage(object.getString("message"));

			return info;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
