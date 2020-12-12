package com.saywut.flutter_foreground_service_plugin;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHandler
{
    private static final String SHARED_PREFERENCES_NAME = "com.saywut.flutter_foreground_service_plugin";
    private static final int  SHARED_PREFERENCES_MOD = Context.MODE_PRIVATE;

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;

    public SharedPreferencesHandler(Context context)
    {
        preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, SHARED_PREFERENCES_MOD);
        preferencesEditor = preferences.edit();
    }

    public void put(String key, Object data)
    {
        if(data instanceof String)
            preferencesEditor.putString(key, (String) data);
        if(data instanceof Boolean)
            preferencesEditor.putBoolean(key, (boolean) data);
        if(data instanceof Float)
            preferencesEditor.putFloat(key, (float) data);
        if(data instanceof Integer)
            preferencesEditor.putInt(key, (int) data);
        if(data instanceof Long)
            preferencesEditor.putLong(key, (long) data);
    }

    public void apply()
    {
        preferencesEditor.apply();
    }

    public Object get(String key)
    {
        return preferences.getAll().get(key);
    }
}
