package net.neutrinosoft.news;

import android.app.Fragment;
import android.os.Bundle;

import net.neutrinosoft.news.models.News;

import java.util.List;

public class RetainedFragment extends Fragment {

    // data object we want to retain
    private List<News> newsList;
    private MemoryCache memoryCache;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
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
}
