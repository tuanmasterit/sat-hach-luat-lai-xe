package com.tonnguyen.sathach;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import android.os.AsyncTask;
import android.util.Log;

import com.tonnguyen.sathach.bean.ExamFormat;
import com.tonnguyen.sathach.bean.Level;
import com.tonnguyen.sathach.bean.Question;

/**
 * A thread to init resources, load questions and level data into memory
 * @author Ton Nguyen
 *
 */
public class ResourceLoaderThread extends AsyncTask<String, Integer, String> {
	private boolean isSucceed = false;
	private String errorMessage = "";
	
	private BaseActivity activity;
	private MyApplication context;
	private boolean completed;
	
	public ResourceLoaderThread(BaseActivity activity, MyApplication context) {
		this.activity = activity;
		this.context = context;
	}
	
	/**
	 * Android will invoke this method when this async task was started, or when user came back to our application.
	 */
    protected void onPreExecute() {
        super.onPreExecute();
        activity.updateProgress(0);
        activity.updateProgressStatus(context.getString(R.string.loading_Data));
    }
	
    /**
     * Will be invoked when calling execute(). Everything the task need to do, will be implement here
     */
	protected String doInBackground(String... params) {
		try {
			// read question.dat to get question data
			String[] questionsAndAnswers = activity.readFileAsStringArray(context.getResources().openRawResource(R.raw.questions));
			Hashtable<Integer, Question> questions = new Hashtable<Integer, Question>();
			Question question = null;
			int questionIndex, questionList, answer, numberOfAnswer = 0;
			String questionRawData = "";
			for (int i = 1; i <= MyApplication.NUMBER_OF_QUESTIONS; i++) {
				String questionFileName = String.format("%03d.html", i);
				questionIndex = (i - 1) % 25;
				questionList = (i - 1) / 25;
				questionRawData = questionsAndAnswers[questionList];
				answer = Integer.parseInt(questionRawData.substring(
						2 * questionIndex, 2 * questionIndex + 1));
				numberOfAnswer = Integer.parseInt(questionRawData.substring(
						2 * questionIndex + 1, 2 * questionIndex + 2));

				question = new Question(questionFileName, numberOfAnswer, answer);
				questions.put(i, question);
				
				//activity.progressDialog.setProgress(80 * i / MyApplication.NUMBER_OF_QUESTIONS);
				publishProgress(80 * i / MyApplication.NUMBER_OF_QUESTIONS);
			}
			context.setQuestions(questions);

			// then read index.dat, to get level and question format data
			ArrayList<Level> levels = new ArrayList<Level>();
			String[] indexData = activity.readFileAsStringArray(context.getResources().openRawResource(R.raw.index));
			for (String line : indexData) {
				String[] data = line.split(";");
				levels.add(new Level(toInt(data[0]), data[1], MyApplication.APPLICATION_DATA_PATH + data[2],
										getExamFormats(activity.getAssets().open(data[2])),
										toInt(data[3]), toLong(data[4])));
			}
			context.setLevels(levels);
			Log.d("Statup loading thread", "loading thread");
			Log.d("context.getQuestions()", String.valueOf(context.getQuestions() == null));
			Log.d("context.getLevels()", String.valueOf(context.getLevels() == null));
			isSucceed = true;
			return null;
		} catch (IOException e) {
			isSucceed = false;
			errorMessage = e.getMessage();
			Log.e("Loading resource", errorMessage);
			return null;
		}
	}
	
	/**
	 * This method will be invoke be UI thread. The purpose is to update UI
	 */
	protected void onProgressUpdate(Integer... args) {
		activity.updateProgress(args[0]);
		activity.updateProgressStatus(context.getString(R.string.loading_Data) + " - " + args[0]);
	}
	
	public void setActivity(StartupActivity activity) {
		this.activity = activity;
		if (completed) {
			notifyActivityTaskCompleted();
		}
	}
	
	/**
	 * After doInBackground has been completed, this method will be called, by UI thread
	 */
	protected void onPostExecute(String unused) {
		completed = true;
		notifyActivityTaskCompleted();
	}
	
	/**
	 * Helper method to notify the activity that this task was completed.
	 */
	private void notifyActivityTaskCompleted() {
		if (null != activity) {
			activity.onLoadingTaskCompleted(isSucceed, errorMessage);
		}
	}
	
	/**
	 * Gets the exam format from data file. The exam format will describe the algorithm to generate questions
	 * @param dataFilePath path to the data file
	 * @return An ArrayList of ExamFormat
	 * @throws IOException if data file cannot be found, or read
	 */
	private ArrayList<ExamFormat> getExamFormats(InputStream inputStream) throws IOException {
		ArrayList<ExamFormat> examsFormatList = new ArrayList<ExamFormat>();
		String[] examsFormat = activity.readFileAsStringArray(inputStream);
		String[] examFormatData = null;
		for(String examFormatLine : examsFormat) {
			examFormatData = examFormatLine.split(";");
			examsFormatList.add(new ExamFormat(toInt(examFormatData[0]), toInt(examFormatData[1]), toInt(examFormatData[2])));
		}
		return examsFormatList;
	}
	
	/**
	 * Quick shortcut to parse a string to int
	 * @param value input string to parse to int
	 * @return the primitive integer value represented by value
	 */
	private int toInt(String value) {
		return Integer.parseInt(value);
	}
	
	/**
	 * Quick shortcut to parse a string to long
	 * @param value input string to parse to long
	 * @return the primitive long value represented by value
	 */
	private long toLong(String value) {
		return Long.parseLong(value);
	}
}
