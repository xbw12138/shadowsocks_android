package com.xbw.mvp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPreference {

	private static final String SHAREDPREFERENCES_NAME = "keep_login";
	private static final String KEEP_LOGIN_ACTIVITY = "login_activity";
	private static final String KEEP_LOGIN_ACTIVITY_ID = "login_activity_id";
	private Context context;

	public SharedPreference(Context context) {
		this.context = context;
	}

	public void KeepLogin(String ID) {
		SharedPreferences settings = context.getSharedPreferences(
				SHAREDPREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEEP_LOGIN_ACTIVITY, "1");
		editor.putString(KEEP_LOGIN_ACTIVITY_ID, ID);
		Log.i("*****************", "true");
		editor.commit();
	}

	public void DisconnectLogin() {
		SharedPreferences settings = context.getSharedPreferences(
				SHAREDPREFERENCES_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEEP_LOGIN_ACTIVITY, "0");
		editor.commit();
	}

	public boolean isLogin(String className) {
		if (context == null || className == null
				|| "".equalsIgnoreCase(className))
			return false;
		String mResultStr = context.getSharedPreferences(
				SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE).getString(
				KEEP_LOGIN_ACTIVITY, "");
		if (mResultStr.equalsIgnoreCase("1"))
			return true;
		else
			return false;
	}

	public String getID() {
		String mResultStr = context.getSharedPreferences(
				SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE).getString(
				KEEP_LOGIN_ACTIVITY_ID, "");
		return mResultStr;
	}

}
