package net.neutrinosoft.news;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MySharedPreferences {
    private SharedPreferences sPref;
    private Editor editor;
    private Context context;

    public MySharedPreferences(Context context, String name, int mode) {
        this.context = context;
        sPref = context.getSharedPreferences(name, mode);
    }

    public void put(String key, String value) {
        editor = sPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String get(String key) {
        return sPref.getString(key, "");
    }

    public boolean contains(String key) {
        if (sPref.contains(key))
            return true;
        else
            return false;
    }

    public void clearEditor() {
        editor = sPref.edit();
        editor.clear();
        editor.apply();
    }
}
