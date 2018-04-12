package com.ubtrobot.commons;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ObjectStorage {

    private final SharedPreferences mSharedPreferences;
    private final Gson mGson;

    public ObjectStorage(SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
        mGson = new Gson();
    }

    public void save(String key, Object object) {
        mSharedPreferences.edit().putString(key, mGson.toJson(object)).commit();
    }

    public <T> T get(String key, Class<T> clazz) {
        String json = mSharedPreferences.getString(key, null);
        if (json == null) {
            return null;
        }

        try {
            return mGson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public <T> T get(String key, TypeToken<T> typeToken) {
        String json = mSharedPreferences.getString(key, null);
        if (json == null) {
            return null;
        }

        return mGson.fromJson(json, typeToken.getType());
    }

    public void remove(String key) {
        mSharedPreferences.edit().remove(key).commit();
    }
}