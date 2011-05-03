package vn.tonnguyen.sathach;

import android.app.Application;
import android.os.Environment;

public class MyApplication extends Application {
	
	public static final String APPLICATION_DATA_PATH = Environment.getExternalStorageDirectory() + "/data/vn.tonnguyen.sathach/";
	
	public static final String APPLICATION_INDEX_FILE_NAME = "index.dat";
	
	public static final String APPLICATION_INDEX_FILE_PATH = APPLICATION_DATA_PATH + "index.dat";
	
	public static final String APPLICATION_QUESTIONS_DATA_FILE_NAME = "questions.dat";
	
	public static final String APPLICATION_QUESTIONS_DATA_FILE_PATH = APPLICATION_DATA_PATH + "questions.dat";
	
	public static final String ONLINE_DATA_FILE_URL = "http://sat-hach-luat-lai-xe.googlecode.com/files/Data.zip";
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

}
