package com.tonnguyen.sathach;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	private MyApplication context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		// Get the vibrate on touch preference, and update to our in-memory-copy of vibrate on touch setting
		// so the variable isVibrateOnTouchEnabled will be synced everytime config changed
		context = (MyApplication)getApplicationContext();
		CheckBoxPreference vibrateOnTouch = (CheckBoxPreference) findPreference(MyApplication.USER_PREFERENCE_ENABLE_VIBRATE_ON_TOUCH);
		vibrateOnTouch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				context.setVibrateOnTouchEnabled(Boolean.parseBoolean(newValue.toString()));
				return true;
			}
		});
	}
}
