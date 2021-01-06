package com.saywut.flutter_foreground_service_plugin;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Handles the service saved preferences data
 */
public class SharedPreferencesHandler
{
    private static final String SHARED_PREFERENCES_NAME = "com.saywut.flutter_foreground_service_plugin";
    private static final int SHARED_PREFERENCES_MOD = Context.MODE_PRIVATE;

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;

    public SharedPreferencesHandler(Context context)
    {
        preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, SHARED_PREFERENCES_MOD);
        preferencesEditor = preferences.edit();
    }

    /**
     * Add new value to the preferences with special key
     *
     * @param key the value key to
     * @param value the value to add to the preferences, the value type has to be String, boolean, float, int, long
     */
    public void put(String key, Object value)
    {
        if (value instanceof String)
            preferencesEditor.putString(key, (String) value);
        if (value instanceof Boolean)
            preferencesEditor.putBoolean(key, (boolean) value);
        if (value instanceof Float)
            preferencesEditor.putFloat(key, (float) value);
        if (value instanceof Integer)
            preferencesEditor.putInt(key, (int) value);
        if (value instanceof Long)
            preferencesEditor.putLong(key, (long) value);
    }

    /**
     * Saves the new values to the preferences
     *
     * need to be called after putting new values to the preferences
     */
    public void apply()
    {
        preferencesEditor.apply();
    }

    /**
     * gets the key value
     *
     * @param key the value key
     * @param <T> the value return type
     * @return the retrieve value
     */
    public <T> T get(String key)
    {
        return (T) preferences.getAll().get(key);
    }

    /**
     * gets the key value, if not found or the value is null return the default value
     *
     * @param key the value key
     * @param defaultValue the default value to return
     * @param <T> the default value and key value types
     * @return return the key value, if not found or the value is null return the defaultValue
     */
    public <T> T getDefault(String key, T defaultValue)
    {
        T storedValue = get(key);

        return storedValue == null ? defaultValue : storedValue;
    }
}
