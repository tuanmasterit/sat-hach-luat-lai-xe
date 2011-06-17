/*
 * Copyright (C) 2011  Nguyen Hoang Ton, a.k.a Ton Nguyen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.tonnguyen.sathach;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * @author Ton Nguyen
 *
 */
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
