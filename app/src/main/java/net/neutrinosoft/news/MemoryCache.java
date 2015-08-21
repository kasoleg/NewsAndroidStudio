package net.neutrinosoft.news;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;
import android.util.Log;

public class MemoryCache {

	private static final String TAG = "MemoryCache";

	private Map<String, Bitmap> cache = Collections
			.synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f, true));

	private Long size = 0L;

	private Long limit = 100000L;

	public MemoryCache() {
		//set 25% for cache of maximum available memory on device
		setLimit(Runtime.getRuntime().maxMemory() / 4);
	}

	private void setLimit(Long newLimit) {
		limit = newLimit;
		Log.i(TAG, "MemoryCache will use up to " + limit / 1024. / 1024. + "MB");
	}

	/**
     * Get Bitmap by id from cache and return Bitmap if exists and null otherwise
     *
     * @param id - id of Bitmap in cache
     */
	public Bitmap get(String id) {
		try {
			if (cache.containsKey(id)) {
				return cache.get(id);
			} else {
				return null;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
     * Set Bitmap and id
     *
     * @param id - id of Bitmap in cache
     * @param bitmap - Bitmap in cache
     */
	public void put(String id, Bitmap bitmap) {
		try {
			if (cache.containsKey(id)) {
				size -= getSizeInBytes(bitmap);
			}

			cache.put(id, bitmap);
			size += getSizeInBytes(bitmap);
			checkSize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
     * If cache is full clear cache while memory is enough for adding Bitmap in cache
     */
	private void checkSize() {
		Log.i(TAG, "cache size = " + size + " length = " + cache.size());

		if (size > limit) {
			Iterator<Entry<String, Bitmap>> iterator = cache.entrySet()
					.iterator();

			while (iterator.hasNext()) {
				Entry<String, Bitmap> entry = iterator.next();
				size -= getSizeInBytes(entry.getValue());
				iterator.remove();
				if (size <= limit)
					break;
			}
			Log.i(TAG, "Clean cache. New size " + cache.size());
		}
	}

	public void clear() {
		try {
			cache.clear();
			size = 0L;
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private Long getSizeInBytes(Bitmap bitmap) {
		if (bitmap != null) {
			return (long) (bitmap.getRowBytes() * bitmap.getHeight());
		}
		return 0L;
	}
}
