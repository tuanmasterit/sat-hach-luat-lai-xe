package vn.tonnguyen.sathach;

import java.util.ArrayList;
import java.util.Hashtable;

import vn.tonnguyen.sathach.bean.Level;
import vn.tonnguyen.sathach.bean.Question;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;

public class MyApplication extends Application {
	
	public static final String APPLICATION_DATA_PATH = Environment.getExternalStorageDirectory() + "/data/vn.tonnguyen.sathach/";
	
	//public static final String APPLICATION_INDEX_FILE_NAME = "index.dat";
	
	//public static final String APPLICATION_INDEX_FILE_PATH = APPLICATION_DATA_PATH + APPLICATION_INDEX_FILE_NAME;
	
	//public static final String APPLICATION_QUESTIONS_DATA_FILE_NAME = "questions.dat";
	
	//public static final String APPLICATION_QUESTIONS_DATA_FILE_PATH = APPLICATION_DATA_PATH + APPLICATION_QUESTIONS_DATA_FILE_NAME;
	
	//public static final String ONLINE_DATA_FILE_URL = "http://sat-hach-luat-lai-xe.googlecode.com/files/Data.zip";
	
	public static final String ONLINE_DATA_ROOT_URL = "http://sat-hach-luat-lai-xe.googlecode.com/files/";
	
	public static final String APPLICATION_SAVING_ZIP_FILE_PATH = APPLICATION_DATA_PATH + "data.zip";
	
	public static final int NUMBER_OF_QUESTIONS = 405;
	
	public static final String USER_PREFERENCE_KEY = "UserSetting";
	
	public static final String USER_PREFERENCE_LEVEL_KEY = "Level";
	
	public static final String USER_PREFERENCE_ZOOM_KEY = "ZOOM";
	
	public static final String USER_PREFERENCE_ENABLE_VIBRATE_ON_TOUCH = "VibrateOnTouch";
	
	private Hashtable<Integer, Question> questions;
	
	private ArrayList<Level> levels;
	
	// Get instance of Vibrator from current Context
	private Vibrator vibrator;
	
	// a in-memory-copy of setting for vibrate on touch, to optimize the performance
	private boolean isVibrateOnTouchEnabled;
	
	public Hashtable<Integer, Question> getQuestions() {
		return questions;
	}

	public void setQuestions(Hashtable<Integer, Question> questions) {
		this.questions = questions;
	}

	public ArrayList<Level> getLevels() {
		return levels;
	}

	public void setLevels(ArrayList<Level> levels) {
		this.levels = levels;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		isVibrateOnTouchEnabled = getEnableVibrateOnTouch();
	}
	
	/**
	 * Get the User setting from SharedPreferences
	 * @return
	 */
	public SharedPreferences getUserPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(this);
			//getSharedPreferences(MyApplication.USER_PREFERENCE_KEY, MODE_PRIVATE);
	}
	
	/**
	 * Get the level that user has chosen recently
	 * @return The level that user has chosen recently, or 0 by default
	 */
	public int getRecentlyLevel() {
		return getUserPreferences().getInt(USER_PREFERENCE_LEVEL_KEY, 0);
	}
	
	/**
	 * Update the level that user has chosen recently
	 * @param levelIndex index of the level that user has chosen
	 */
	public void setRecentlyLevel(int levelIndex) {
		getUserPreferences().edit().putInt(USER_PREFERENCE_LEVEL_KEY, levelIndex).commit();
	}
	
	/**
	 * Get the zoom value that user has chosen recently
	 * @return The zoom value that user has chosen recently, or 75 by default
	 */
	public int getRecentlyZoom() {
		return getUserPreferences().getInt(USER_PREFERENCE_ZOOM_KEY, 75);
	}
	
	/**
	 * Update the zoom value that user has chosen recently
	 * @param scale zoom value
	 */
	public void setRecentlyZoom(int scale) {
		getUserPreferences().edit().putInt(USER_PREFERENCE_ZOOM_KEY, scale).commit();
	}
	
	/**
	 * Get the flag which indicates whether the phone will vibrate on touch
	 * @return flag which indicates whether the phone will vibrate on touch
	 */
	public boolean getEnableVibrateOnTouch() {
		return getUserPreferences().getBoolean(USER_PREFERENCE_ENABLE_VIBRATE_ON_TOUCH, false);
	}
	
//	/**
//	 * Update configuration "Vibrate On Touch" feature
//	 * @param enable Should the phone vibrate on touch
//	 */
//	public void setEnableVibrateOnTouch(boolean enable) {
//		isVibrateOnTouchEnabled = enable;
//		getUserPreferences().edit().putBoolean(USER_PREFERENCE_ENABLE_VIBRATE_ON_TOUCH, enable).commit();
//	}
	
	public void vibrateIfEnabled() {
		if(isVibrateOnTouchEnabled) {
			if(vibrator == null) {
				vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
			}
			vibrator.vibrate(50);
		}
	}

	public boolean isVibrateOnTouchEnabled() {
		return isVibrateOnTouchEnabled;
	}

	public void setVibrateOnTouchEnabled(boolean isVibrateOnTouchEnabled) {
		this.isVibrateOnTouchEnabled = isVibrateOnTouchEnabled;
	}
}
