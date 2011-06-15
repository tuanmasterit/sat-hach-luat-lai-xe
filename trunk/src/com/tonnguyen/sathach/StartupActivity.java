package com.tonnguyen.sathach;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tonnguyen.sathach.bean.ExamFormat;
import com.tonnguyen.sathach.bean.Level;
import com.tonnguyen.sathach.bean.Question;

public class StartupActivity extends BaseActivity {
	public static final int WHAT_ERROR = 1;
	private Handler threadHandler;
	
	private static DownloadFilesTask downloadTask;
	private static ExtractFilesTask extractTask;
	private static ResourceLoaderThread loaderTask;
	
	private static boolean isDownloading;
	private static boolean isExtracting;
	private static boolean isLoadingResource;
	
	private ProgressBar progressBar;
	private TextView progressStatus;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d("Statup onCreate", "Displaying startup dialog");
		
		setContentView(R.layout.activity_startup);
		loadResource();
		Object retained = getLastNonConfigurationInstance();
		if(retained != null) {
			Log.i("Statup onCreate", "Reclaiming previous background task.");
			initAdMob();
			setWorkingState();
			if (retained instanceof DownloadFilesTask) {
		        isDownloading = true;
				downloadTask = (DownloadFilesTask) retained;
			} else if (retained instanceof ExtractFilesTask) {
		        isExtracting = true;
				extractTask = (ExtractFilesTask) retained;
			} else {
		        isLoadingResource = true;
				loaderTask = (ResourceLoaderThread) retained;
			}
		}
		if(savedInstanceState != null) {
			Log.d("onCreate", "savedInstanceState is not NULL");
			isDownloading = savedInstanceState.getBoolean("isDownloading");
			isExtracting = savedInstanceState.getBoolean("isExtracting");
			isLoadingResource = savedInstanceState.getBoolean("isLoadingResource");
			Log.d("onCreate - isDownloading || isExtracting || isLoadingResource", String.valueOf(isDownloading || isExtracting || isLoadingResource));
			if(isDownloading || isExtracting || isLoadingResource) {
				setWorkingState();
			}
		}
		if(downloadTask != null) {
			downloadTask.setActivity(this);
			updateProgressStatus(context.getString(R.string.download_Resource_Message_Downloading));
		}
		if(extractTask != null) {
			extractTask.setActivity(this);
			updateProgressStatus(context.getString(R.string.download_Resource_Message_Extracting));
		}
		if(loaderTask != null) {
			loaderTask.setActivity(this);
			updateProgressStatus(context.getString(R.string.loading_Data));
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle state) {
		state.putSerializable("isDownloading", isDownloading);
		state.putSerializable("isExtracting", isExtracting);
		state.putSerializable("isLoadingResource", isLoadingResource);
		Log.d("StartupActivity onSaveInstanceState", state.toString());
		super.onSaveInstanceState(state);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		Log.d("StartupActivity", "onRestoreInstanceState " + state.toString());
		isDownloading = state.getBoolean("isDownloading");
		isExtracting = state.getBoolean("isExtracting");
		isLoadingResource = state.getBoolean("isLoadingResource");
		super.onRestoreInstanceState(state);
	}
	
	@Override
	public void onStart() {
		Log.d("StartupActivity onStart", "onStart");
		super.onStart();
	}
	
	@Override
	public void onResume() {
		Log.d("StartupActivity onResume - isDownloading || isExtracting || isLoadingResource", String.valueOf(isDownloading || isExtracting || isLoadingResource));
		if(isDownloading || isExtracting || isLoadingResource) {
			setWorkingState();
		}
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Log.d("StartupActivity onPause", "onPause");
		super.onPause();
	}
	
	@Override
	public void onStop() {
		Log.d("StartupActivity onStop", "onStop");
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		Log.d("StartupActivity onDestroy", "onDestroy");
		super.onDestroy();
	}
	
	@Override
	public void onRestart() {
		Log.d("StartupActivity onRestart", "onRestart");
		super.onRestart();
	}
	
	private void updateProgressStatus(String status) {
		progressStatus.setText(status);
	}
	
	private void updateProgress(int progress) {
		progressBar.setProgress(progress);
	}
	
	private void loadResource() {
		Log.d("Statup loadResource", "loadResource");
		Log.d("context.getQuestions()", String.valueOf(context.getQuestions() == null));
		Log.d("context.getLevels()", String.valueOf(context.getLevels() == null));
		progressBar = (ProgressBar)findViewById(R.id.startup_progressBar);
		progressStatus = (TextView)findViewById(R.id.startup_progressStatus);
		if(context.getQuestions() == null || context.getLevels() == null) { // check if resource has been loaded into memory
			threadHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					processMessage(msg, context);
					super.handleMessage(msg);
				}
			};
			if (!isResourcesAvailable()) { // check if resource has been downloaded into data folder
				initAdMob();
				final StartupActivity thisActivity = this;
				((Button)findViewById(R.id.startup_downloadButton)).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						context.vibrateIfEnabled();
						setWorkingState();
						
						// Start the download process and showing the process dialog
						Log.d("Statup", "Displaying download dialog");
						downloadTask = new DownloadFilesTask(thisActivity);
						downloadTask.execute(getOnlineDataFileUrl(), MyApplication.APPLICATION_SAVING_ZIP_FILE_PATH);
					}
				});
				
			} else {
				loaderTask = new ResourceLoaderThread(this);
				loaderTask.execute(new String[0]);
			}
		} else {
			showHomeActivity();
		}
	}
	
	private void setWorkingState() {
		findViewById(R.id.startup_confirmMessage).setVisibility(View.GONE);
		findViewById(R.id.startup_downloadButton).setVisibility(View.GONE);
		
		progressBar.setVisibility(View.VISIBLE);
		progressStatus.setVisibility(View.VISIBLE);
	}
	
	/**
	 * After a screen orientation change, this method is invoked. As we're going
	 * to state save the task, we can no longer associate it with the Activity
	 * that is going to be destroyed here.
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.d("StartupActivity onRetainNonConfigurationInstance - isDownloading || isExtracting || isLoadingResource", String.valueOf(isDownloading || isExtracting || isLoadingResource));
		if(isDownloading) {
			downloadTask.setActivity(null);
			return downloadTask;
		} else if(isExtracting) {
			extractTask.setActivity(null);
			return extractTask;
		} else if(isLoadingResource) {
			loaderTask.setActivity(null);
			return loaderTask;
		}
		return null;
	}
	
	/**
	 * When the aSyncTask has notified the activity that it has completed, we
	 * can refresh the list control, and attempt to dismiss the dialog.
	 */
	private void onDownloadTaskCompleted(boolean completed, String errorMessage) {
		Log.i("Startup Activity", "Activity " + this + " has been notified the task is complete.");
		if(completed) {
			// start extracting downloaded file
			extractTask = new ExtractFilesTask(this);
			extractTask.execute(MyApplication.APPLICATION_SAVING_ZIP_FILE_PATH, MyApplication.APPLICATION_DATA_PATH);
		} else {
			// send message to notify the error
			sendMessageToHandler(StartupActivity.WHAT_ERROR, errorMessage);
		}
	}
	
	/**
	 * When the aSyncTask has notified the activity that it has completed, we
	 * can refresh the list control, and attempt to dismiss the dialog.
	 */
	private void onExtractTaskCompleted(boolean completed, String errorMessage) {
		Log.i("Startup Activity", "Activity " + this + " has been notified the task is complete.");
		if(completed) {
			// start loading resource
			loaderTask = new ResourceLoaderThread(this);
			loaderTask.execute(new String[0]);
		} else {
			// send message to notify the error
			sendMessageToHandler(StartupActivity.WHAT_ERROR, errorMessage);
		}
	}
	
	/**
	 * When the aSyncTask has notified the activity that it has completed, we
	 * can refresh the list control, and attempt to dismiss the dialog.
	 */
	private void onLoadingTaskCompleted() {
		Log.i("Startup Activity", "Activity " + this + " has been notified the task is complete.");
		showHomeActivity();
	}
	
	private String getOnlineDataFileUrl() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int dpi = metrics.densityDpi;
		Log.d("Startup screen", "dpi: " + String.valueOf(dpi));
		Log.d("Startup screen", "isSmallScreen: " + String.valueOf(isSmallScreen()));
		Log.d("Startup screen", "isNormalScreen: " + String.valueOf(isNormalScreen()));
		Log.d("Startup screen", "isLargeScreen: " + String.valueOf(isLargeScreen()));
		
		String fileName = null;
		if(isSmallScreen() || (isNormalScreen() && dpi <= 160)) {
			fileName = "mpdi.zip";
		} else if(isNormalScreen() || (isLargeScreen() && dpi <= 160)) {
			fileName = "hpdi.zip";
		} else {
			fileName = "xhpdi.zip";
		}
		Log.d("Startup screen", "fileName: " + fileName);
		return MyApplication.ONLINE_DATA_ROOT_URL + fileName;
	}
	
	private void processMessage(Message msg, MyApplication application) {
		// process incoming messages here
		switch(msg.what) {
		default: // error occurred
			// display error message
			Log.d("Error occurred", (String)msg.obj);
			Toast toast = Toast.makeText(application, application.getString(R.string.download_Resource_Error_Occurred) + (String)msg.obj, Toast.LENGTH_LONG);
			toast.show();
			Log.d("Startup screen", "Closing startup activity");
			finish();
		}
	}
	
	/**
	 * Init data, bind event for buttons
	 */
	private void showHomeActivity() {
		if(downloadTask != null) {
			downloadTask.cancel(true);
			downloadTask = null;
		}
		if(extractTask != null) {
			extractTask.cancel(true);
			extractTask = null;
		}
		if(loaderTask != null) {
			loaderTask.cancel(true);
			loaderTask = null;
		}
		Intent intent = new Intent(context, HomeActivity.class);
		startActivity(intent);
	}

	/**
	 * Check if resources are available
	 * 
	 * @return true if resources exist, false otherwise.
	 */
	private boolean isResourcesAvailable() {
		return isFileExist(MyApplication.APPLICATION_DATA_PATH + "161.png")
				&& isFileExist(MyApplication.APPLICATION_DATA_PATH + "405.png");
	}

	/**
	 * Check if a input file is existed
	 * 
	 * @param filePath
	 * @return true if this file exists, false otherwise.
	 */
	private boolean isFileExist(String filePath) {
		return new File(filePath).exists();
	}
	
	/**
	 * Pushes a message onto the end of the message queue after all pending messages before the current time. 
	 * It will be received in handleMessage(Message), in the thread attached to this handler.
	 * @param messageID Integer ID of the message, to identify a message
	 * @param what An object to send belong with the message
	 */
	private void sendMessageToHandler(int messageID, Object what) {
		Message msg = new Message();
		msg.what = messageID;
		msg.obj = what;
		if(threadHandler != null) {
			threadHandler.sendMessage(msg);
		}
	}
	
	/**
	 * A async task to download data file from Internet. This task can run in background, so the download process
	 * will be executed even if user has leave the application
	 * @author Ton Nguyen
	 *
	 */
	private class DownloadFilesTask extends AsyncTask<String, Integer, String> {
		private boolean isSucceed = false;
		private String errorMessage = "";
		
		private StartupActivity activity;
		private boolean completed;
		
		private DownloadFilesTask(StartupActivity activity) {
			this.activity = activity;
		}
		
		/**
		 * Android will invoke this method when this async task was started, or when user came back to our application.
		 */
	    protected void onPreExecute() {
	        super.onPreExecute();
	        activity.updateProgress(0);
	        activity.updateProgressStatus(context.getString(R.string.download_Resource_Message_Downloading));
	        isDownloading = true;
	    }
		
	    /**
	     * Will be invoked when calling execute(). Everything the task need to do, will be implement here
	     */
		protected String doInBackground(String... params) {
			Log.d("Download resource", "Start downloading " + params[0]);
			InputStream inputStream = null;
			FileOutputStream fileOutput = null;
			try {
				URL url = new URL(params[0]);

				// create the new connection
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				// set up some things on the connection
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				// and connect!
				urlConnection.connect();

				File dataFolder = new File(params[1]).getParentFile();
				// make sure the folder was empty first
				if (dataFolder.exists()) {
					for (File file : dataFolder.listFiles()) {
						file.delete();
					}
					dataFolder.delete();
				}
				// have the object build the directory structure, if needed.
				dataFolder.mkdirs();

				Log.d("Saving location", dataFolder.getAbsolutePath());
				// create a File object for the output file
				File outputFile = new File(params[1]);
				if (outputFile.exists()) {
					outputFile.delete();
				}
				// this will be used to write the downloaded data into the file we created
				fileOutput = new FileOutputStream(outputFile);
				// this will be used in reading the data from the internet
				inputStream = urlConnection.getInputStream();

				// this is the total size of the file
				int totalSize = urlConnection.getContentLength();
				// variable to store total downloaded bytes
				int downloadedSize = 0;

				// create a buffer...
				byte[] buffer = new byte[1024];
				int bufferLength = 0; // used to store a temporary size of the buffer

				// now, read through the input buffer and write the contents to the file
				while ((bufferLength = inputStream.read(buffer)) > 0) {
					// add the data in the buffer to the file in the file output
					// stream (the file on the sd card
					fileOutput.write(buffer, 0, bufferLength);
					// add up the size so we know how much is downloaded
					downloadedSize += bufferLength;
					
					// publishing the progress....
					publishProgress(100 * downloadedSize / totalSize);
				}
				isSucceed = true;
				return null;
			} catch (Exception e) {
				Log.e("Download resource", e.getMessage());
				isSucceed = false;
				errorMessage = e.getMessage();
				return null;
			} finally {
				try {
					if (inputStream != null) {
						inputStream.close();
						inputStream = null;
					}
				} catch (Exception ex) {
				}

				try {
					if (fileOutput != null) {
						fileOutput.close();
						fileOutput = null;
					}
				} catch (Exception ex) {
				}
			}
		}

		/**
		 * This method will be invoke be UI thread. The purpose is to update UI
		 */
		protected void onProgressUpdate(Integer... args) {
			activity.updateProgress(args[0]);
			activity.updateProgressStatus(context.getString(R.string.download_Resource_Message_Downloading) + " - " + args[0]);
		}
		
		private void setActivity(StartupActivity activity) {
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
			isDownloading = false;
			notifyActivityTaskCompleted();
		}
		
		/**
		 * Helper method to notify the activity that this task was completed.
		 */
		private void notifyActivityTaskCompleted() {
			if (null != activity) {
				activity.onDownloadTaskCompleted(isSucceed, errorMessage);
			}
		}
	}
	
	/**
	 * A async task to extract downloaded data file from Internet. This task can run in background, so the extracting process
	 * will be executed even if user has leave the application
	 * @author Ton Nguyen
	 *
	 */
	private class ExtractFilesTask extends AsyncTask<String, Integer, String> {
		private boolean isSucceed = false;
		private String errorMessage = "";
		
		private StartupActivity activity;
		private boolean completed;
		
		private ExtractFilesTask(StartupActivity activity) {
			this.activity = activity;
		}
		
		/**
		 * Android will invoke this method when this async task was started, or when user came back to our application.
		 */
	    protected void onPreExecute() {
	        super.onPreExecute();
	        activity.updateProgress(0);
	        activity.updateProgressStatus(context.getString(R.string.download_Resource_Message_Extracting));
	        isExtracting = true;
	    }
		
	    /**
	     * Will be invoked when calling execute(). Everything the task need to do, will be implement here
	     */
		protected String doInBackground(String... params) {
			Log.d("Extract resource", "Start extracting");
			ZipInputStream zipinputstream = null;
			try {
				byte[] buf = new byte[1024];
				
				// open input zip file to extract
				FileInputStream inputStream = new FileInputStream(params[0]);
				// total compressed size, to calculate extract process percent
				int totalSize = inputStream.available();
				
				zipinputstream = new ZipInputStream(inputStream);

				ZipEntry zipentry = zipinputstream.getNextEntry();
				long downloadedSize = 0;
				long bufferLength = 0;
				int n;
				while (zipentry != null) { // for each entry to be extracted
					// get downloaded size, to calculate extract process's percent
					bufferLength = zipentry.getCompressedSize();
					downloadedSize += bufferLength;
					String entryName = zipentry.getName();
					//Log.d("Extracting resources", "entryname " + entryName);
					FileOutputStream fileoutputstream;
					File newFile = new File(entryName);
					String directory = newFile.getParent();

					if (directory == null) {
						if (newFile.isDirectory()) {
							break;
						}
					}

					// save file to output folder, which has been defined in params[1]
					fileoutputstream = new FileOutputStream(params[1] + entryName);

					while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
						fileoutputstream.write(buf, 0, n);
					}

					fileoutputstream.close();
					zipinputstream.closeEntry();
					
					publishProgress((int)(100 * downloadedSize / totalSize));
					
					zipentry = zipinputstream.getNextEntry();
				}// while
				isSucceed = true;
				return null;
			} catch (Exception e) {
				Log.e("Extract resource", e.getMessage());
				isSucceed = false;
				errorMessage = e.getMessage();
				return null;
			} finally {
				try {
					// delete zip file
					try {
						new File(params[0]).delete();
					} catch (Exception e) {}
				} catch (Exception w) {}
				try {
					if (zipinputstream != null) {
						zipinputstream.close();
						zipinputstream = null;
					}
				} catch (Exception ex) {
				}
			}
		}

		/**
		 * This method will be invoke be UI thread. The purpose is to update UI
		 */
		protected void onProgressUpdate(Integer... args) {
			activity.updateProgress(args[0]);
			activity.updateProgressStatus(context.getString(R.string.download_Resource_Message_Extracting) + " - " + args[0]);
		}
		
		private void setActivity(StartupActivity activity) {
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
			isExtracting = false;
			notifyActivityTaskCompleted();
		}
		
		/**
		 * Helper method to notify the activity that this task was completed.
		 */
		private void notifyActivityTaskCompleted() {
			if (null != activity) {
				activity.onExtractTaskCompleted(isSucceed, errorMessage);
			}
		}
	}
	
	/**
	 * A thread to init resources, load questions and level data into memory
	 * @author Ton Nguyen
	 *
	 */
	private class ResourceLoaderThread extends AsyncTask<String, Integer, String> {
		private String errorMessage = "";
		
		private StartupActivity activity;
		private boolean completed;
		
		private ResourceLoaderThread(StartupActivity activity) {
			this.activity = activity;
		}
		
		/**
		 * Android will invoke this method when this async task was started, or when user came back to our application.
		 */
	    protected void onPreExecute() {
	        super.onPreExecute();
	        activity.updateProgress(0);
	        activity.updateProgressStatus(context.getString(R.string.loading_Data));
	        isLoadingResource = true;
	    }
		
	    /**
	     * Will be invoked when calling execute(). Everything the task need to do, will be implement here
	     */
		protected String doInBackground(String... params) {
			try {
				// read question.dat to get question data
				String[] questionsAndAnswers = readFileAsStringArray(context.getResources().openRawResource(R.raw.questions));
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
				String[] indexData = readFileAsStringArray(context.getResources().openRawResource(R.raw.index));
				for (String line : indexData) {
					String[] data = line.split(";");
					levels.add(new Level(toInt(data[0]), data[1], MyApplication.APPLICATION_DATA_PATH + data[2],
											getExamFormats(getAssets().open(data[2])),
											toInt(data[3]), toLong(data[4])));
				}
				context.setLevels(levels);
				Log.d("Statup loading thread", "loading thread");
				Log.d("context.getQuestions()", String.valueOf(context.getQuestions() == null));
				Log.d("context.getLevels()", String.valueOf(context.getLevels() == null));
				return null;
			} catch (IOException e) {
				errorMessage = e.getMessage();
				Log.e("Loading resource", errorMessage);
				// send message to notify the error
				sendMessageToHandler(StartupActivity.WHAT_ERROR, errorMessage);
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
		
		private void setActivity(StartupActivity activity) {
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
			isLoadingResource = false;
			notifyActivityTaskCompleted();
		}
		
		/**
		 * Helper method to notify the activity that this task was completed.
		 */
		private void notifyActivityTaskCompleted() {
			if (null != activity) {
				activity.onLoadingTaskCompleted();
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
			String[] examsFormat = readFileAsStringArray(inputStream);
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
}