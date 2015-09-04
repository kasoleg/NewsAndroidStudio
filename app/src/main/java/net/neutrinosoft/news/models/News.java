package net.neutrinosoft.news.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;

public class News implements Parcelable {
	private String id;
	private String name;
	private String description;
	private String createdAt;
	//private Bitmap bitmap;

	public News() {
	}

	protected News(Parcel in) {
		id = in.readString();
		name = in.readString();
		description = in.readString();
		createdAt = in.readString();
		//bitmap = in.readParcelable(Bitmap.class.getClassLoader());
	}

	public static final Creator<News> CREATOR = new Creator<News>() {
		@Override
		public News createFromParcel(Parcel in) {
			return new News(in);
		}

		@Override
		public News[] newArray(int size) {
			return new News[size];
		}
	};

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeString(createdAt);

		//ByteArrayOutputStream stream = new ByteArrayOutputStream();
		//bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		//byte[] byteArray = stream.toByteArray();
		//dest.writeByteArray(byteArray);
	}
}
