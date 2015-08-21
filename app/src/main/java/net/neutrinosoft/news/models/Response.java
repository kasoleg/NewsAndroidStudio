package net.neutrinosoft.news.models;

import java.util.List;

public class Response {
	private Info info;
	private List<News> ads;
	public Info getInfo() {
		return info;
	}
	public void setInfo(Info info) {
		this.info = info;
	}
	public List<News> getAds() {
		return ads;
	}
	public void setAds(List<News> ads) {
		this.ads = ads;
	}
}
