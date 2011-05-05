package vn.tonnguyen.sathach;

import java.util.ArrayList;
import java.util.Hashtable;

import vn.tonnguyen.sathach.bean.Level;
import vn.tonnguyen.sathach.bean.Question;
import android.app.Application;
import android.os.Environment;

public class MyApplication extends Application {
	
	public static final String APPLICATION_DATA_PATH = Environment.getExternalStorageDirectory() + "/data/vn.tonnguyen.sathach/";
	
	public static final String APPLICATION_INDEX_FILE_NAME = "index.dat";
	
	public static final String APPLICATION_INDEX_FILE_PATH = APPLICATION_DATA_PATH + APPLICATION_INDEX_FILE_NAME;
	
	public static final String APPLICATION_QUESTIONS_DATA_FILE_NAME = "questions.dat";
	
	public static final String APPLICATION_QUESTIONS_DATA_FILE_PATH = APPLICATION_DATA_PATH + APPLICATION_QUESTIONS_DATA_FILE_NAME;
	
	public static final String ONLINE_DATA_FILE_URL = "http://sat-hach-luat-lai-xe.googlecode.com/files/Data.zip";
	
	private Hashtable<Integer, Question> questions;
	
	private ArrayList<Level> levels;
	
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
	}
}
