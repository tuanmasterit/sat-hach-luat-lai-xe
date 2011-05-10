package vn.tonnguyen.sathach;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import vn.tonnguyen.sathach.bean.ExamFormat;
import vn.tonnguyen.sathach.bean.Level;
import vn.tonnguyen.sathach.bean.Question;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class Startup extends BaseActivity {
	public static final int WHAT_ERROR = 1;
	public static final int WHAT_LOADING_RESOURCE = 2;
	public static final int WHAT_LOADING_RESOURCE_SUCCEED = 4;
	
	public static final int DIALOG_DOWNLOAD_PROGRESS = 5;
	public static final int DIALOG_EXTRACT_PROGRESS = 6;
	public static final int DIALOG_LOADING_PROGRESS = 7;
	
	private ProgressDialog progressDialog;
	private Handler threadHandler;
	private MyApplication context;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.v("Statup", "Displaying startup dialog");
		setContentView(R.layout.startup);
		// Look up the AdView as a resource and load a request.
	    AdView adView = (AdView)this.findViewById(R.id.adView);
	    AdRequest re = new AdRequest();
	    re.setTesting(true);
	    re.setGender(AdRequest.Gender.MALE);
	    adView.loadAd(re);

		context = (MyApplication)getApplicationContext();
		if(context.getQuestions() == null || context.getLevels() == null) { // check if resource has been loaded into memory
			threadHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					processMessage(msg, context);
					super.handleMessage(msg);
				}
			};
			if (!isResourcesAvailable()) { // check if resource has been downloaded into data folder
				// Get resources from Internet
				// confirm is user is willing to download first
				new AlertDialog.Builder(this)
						.setIcon(android.R.drawable.ic_dialog_info)
						.setTitle(R.string.download_Resource_Title)
						.setMessage(R.string.download_Resource_Message)
						.setPositiveButton(R.string.download_Resource_Button_Yes,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// Start the download process and showing the process dialog
										Log.v("Statup", "Displaying download dialog");
										new DownloadFilesTask().execute(MyApplication.ONLINE_DATA_FILE_URL, MyApplication.APPLICATION_SAVING_ZIP_FILE_PATH);
									}
								})
						.setNegativeButton(R.string.download_Resource_Button_No,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// Close the activity
										Log.v("Startup screen", "Closing startup activity");
										finish();
									}
								}).show();
			} else {
				startLoadingResource();
			}
		} else {
			initComponents();
		}
	}
	
	/**
	 * Override the onCreateDialog, show when async task was working, user leaved application, and came back, Android will know what dialog should be displayed
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	        case DIALOG_DOWNLOAD_PROGRESS:
	        	progressDialog = new ProgressDialog(this);
	        	progressDialog.setMessage(context.getString(R.string.download_Resource_Message_Downloading));
	        	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        	progressDialog.setCancelable(false);
	        	progressDialog.show();
	            return progressDialog;
	        case DIALOG_EXTRACT_PROGRESS:
	        	progressDialog = new ProgressDialog(this);
	        	progressDialog.setMessage(context.getString(R.string.download_Resource_Message_Extracting));
	        	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        	progressDialog.setCancelable(false);
	        	progressDialog.show();
	            return progressDialog;
	        case DIALOG_LOADING_PROGRESS:
	        	progressDialog = new ProgressDialog(this);
	        	progressDialog.setMessage(context.getString(R.string.loading_Data));
	        	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        	progressDialog.setCancelable(false);
	        	progressDialog.show();
	            return progressDialog;
	        default:
	            return null;
	    }
	}
	
	private void processMessage(Message msg, MyApplication application) {
		// process incoming messages here
		switch(msg.what) {
		case WHAT_LOADING_RESOURCE_SUCCEED:
			Log.v("Loading resource Suceed", String.valueOf(msg.obj));
			progressDialog.cancel();
			progressDialog = null;
			initComponents();
			break;
		default: // error occurred
			// display error message
			Log.v("Error occurred", (String)msg.obj);
			progressDialog.cancel();
			Toast toast = Toast.makeText(application, application.getString(R.string.download_Resource_Error_Occurred) + (String)msg.obj, Toast.LENGTH_LONG);
			toast.show();
			Log.v("Startup screen", "Closing startup activity");
			finish();
		}
	}
	
	/**
	 * Start the thread to load resource into memory
	 */
	private void startLoadingResource() {
		showDialog(DIALOG_LOADING_PROGRESS);
		ResourceLoaderThread thread = new ResourceLoaderThread();
		thread.start();
	}

	/**
	 * Init data, bind event for buttons
	 */
	private void initComponents() {
		ArrayList<Level> levels = context.getLevels();
		if(levels == null || levels.size() < 1) {
			Toast.makeText(context, context.getString(R.string.error_data_corrupted), Toast.LENGTH_LONG)
				.show();
			return;
		}
		final String[] menuItems = new String[levels.size()];
		for(int i = 0; i < levels.size(); i++) {
			menuItems[i] = levels.get(i).getName();
		}
	    // Register the onClick listener with the implementation above
		((Button)findViewById(R.id.button_new_game)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO check if we have a pending exam, to asking for resume or create new
				AlertDialog.Builder selectLevelDialog = new AlertDialog.Builder(Startup.this);
				selectLevelDialog.setTitle(R.string.home_SelectlLevel_Title);
				selectLevelDialog.setSingleChoiceItems(menuItems, context.getRecentlyLevel(), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// onClick Action
						// the level list has been ordered by index, so whichButton will be the selected index
						context.setRecentlyLevel(whichButton);
					}
				}).setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// on Ok button action
						Log.v("Selected level index to create new exam", String.valueOf(context.getRecentlyLevel()));
						if(context.getRecentlyLevel() >= 0) {
							startActivity(new Intent((MyApplication)getApplicationContext(), ExamScreen.class));
						} else {
							Toast.makeText(context, context.getString(R.string.error_pleaseSelect_Level), Toast.LENGTH_LONG)
								.show();
						}
					}
				}).setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// on cancel button action
						dialog.cancel();
					}
				});
				selectLevelDialog.show();
			}
		});

	    // Register the onClick listener with the implementation above
		((Button)findViewById(R.id.button_exit)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// exit
				finish();
			}
		});
	}

	/**
	 * Check if resources are available
	 * 
	 * @return true if resources exist, false otherwise.
	 */
	private boolean isResourcesAvailable() {
		return isFileExist(MyApplication.APPLICATION_INDEX_FILE_PATH)
				&& isFileExist(MyApplication.APPLICATION_QUESTIONS_DATA_FILE_PATH);
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
		threadHandler.sendMessage(msg);
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
		
		/**
		 * Android will invoke this method when this async task was started, or when user came back to our application.
		 */
	    protected void onPreExecute() {
	        super.onPreExecute();
	        showDialog(DIALOG_DOWNLOAD_PROGRESS);
	    }
		
	    /**
	     * Will be invoked when calling execute(). Everything the task need to do, will be implement here
	     */
		protected String doInBackground(String... params) {
			Log.d("Download resource", "Start downloading");
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
			progressDialog.setProgress(args[0]);
		}
		
		/**
		 * After doInBackground has been completed, this method will be called, by UI thread
		 */
		protected void onPostExecute(String unused) {
			if(isSucceed) {
				// start extracting downloaded file
				new ExtractFilesTask().execute(MyApplication.APPLICATION_SAVING_ZIP_FILE_PATH, MyApplication.APPLICATION_DATA_PATH);
			} else {
				// send message to notify the error
				sendMessageToHandler(Startup.WHAT_ERROR, errorMessage);
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
		
		/**
		 * Android will invoke this method when this async task was started, or when user came back to our application.
		 */
	    protected void onPreExecute() {
	        super.onPreExecute();
	        showDialog(DIALOG_EXTRACT_PROGRESS);
	    }
		
	    /**
	     * Will be invoked when calling execute(). Everything the task need to do, will be implement here
	     */
		protected String doInBackground(String... params) {
			Log.d("Extract resource", "Start extracting");
			ZipInputStream zipinputstream = null;
			try {
				byte[] buf = new byte[1024];
				
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
					//Log.v("Extracting resources", "entryname " + entryName);
					FileOutputStream fileoutputstream;
					File newFile = new File(entryName);
					String directory = newFile.getParent();

					if (directory == null) {
						if (newFile.isDirectory()) {
							break;
						}
					}

					fileoutputstream = new FileOutputStream(params[1] + entryName);

					while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
						fileoutputstream.write(buf, 0, n);
					}

					fileoutputstream.close();
					zipinputstream.closeEntry();
					
					publishProgress((int)(100 * downloadedSize / totalSize));
					
					zipentry = zipinputstream.getNextEntry();
				}// while
				// delete zip file
				try {
					new File(params[0]).delete();
				} catch (Exception e) {}
				isSucceed = true;
				return null;
			} catch (Exception e) {
				Log.e("Extract resource", e.getMessage());
				isSucceed = false;
				errorMessage = e.getMessage();
				return null;
			} finally {
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
			progressDialog.setProgress(args[0]);
		}
		
		/**
		 * After doInBackground has been completed, this method will be called, by UI thread
		 */
		protected void onPostExecute(String unused) {
			if(isSucceed) {
				// start loading resource
				startLoadingResource();
			} else {
				// send message to notify the error
				sendMessageToHandler(Startup.WHAT_ERROR, errorMessage);
			}
		}
	}
	
	/**
	 * A thread to init resources, load questions and level data into memory
	 * @author Ton Nguyen
	 *
	 */
	private class ResourceLoaderThread extends Thread {
		
		public ResourceLoaderThread() {
		}
		
		/**
		 * Thread's start point
		 */
		public void run() {
			loadResources();
		}

		/**
		 * Load resources from data files to memory
		 */
		private void loadResources() {
			try {
				// read question.dat to get question data
				String[] questionsAndAnswers = readFileAsStringArray(MyApplication.APPLICATION_QUESTIONS_DATA_FILE_PATH);
				Hashtable<Integer, Question> questions = new Hashtable<Integer, Question>();
				Question question = null;
				int questionIndex, questionList, answer, numberOfAnswer = 0;
				String questionRawData = "";
				for (int i = 1; i <= MyApplication.NUMBER_OF_QUESTIONS; i++) {
					String pictureName = String.format("%03d.JPG", i);
					questionIndex = (i - 1) % 25;
					questionList = (i - 1) / 25;
					questionRawData = questionsAndAnswers[questionList];
					answer = Integer.parseInt(questionRawData.substring(
							2 * questionIndex, 2 * questionIndex + 1));
					numberOfAnswer = Integer.parseInt(questionRawData.substring(
							2 * questionIndex + 1, 2 * questionIndex + 2));

					question = new Question(pictureName, numberOfAnswer, answer);
					questions.put(i, question);
					
					progressDialog.setProgress(80 * i / MyApplication.NUMBER_OF_QUESTIONS);
				}
				context.setQuestions(questions);

				// then read index.dat, to get level and question format data
				ArrayList<Level> levels = new ArrayList<Level>();
				String[] indexData = readFileAsStringArray(MyApplication.APPLICATION_INDEX_FILE_PATH);
				for (String line : indexData) {
					String[] data = line.split(";");
					levels.add(new Level(toInt(data[0]), data[1], MyApplication.APPLICATION_DATA_PATH + data[2],
											getExamFormats(MyApplication.APPLICATION_DATA_PATH + data[2])));
				}
				context.setLevels(levels);
				sendMessageToHandler(Startup.WHAT_LOADING_RESOURCE_SUCCEED, context.getString(R.string.download_Resource_Message_Loaded));
			} catch (IOException e) {
				Log.e("Loading resource", e.getMessage());
				// send message to notify the error
				sendMessageToHandler(Startup.WHAT_ERROR, e.getMessage());
			}
		}
		
		/**
		 * Gets the exam format from data file. The exam format will describe the algorithm to generate questions
		 * @param dataFilePath path to the data file
		 * @return An ArrayList of ExamFormat
		 * @throws IOException if data file cannot be found, or read
		 */
		private ArrayList<ExamFormat> getExamFormats(String dataFilePath) throws IOException {
			ArrayList<ExamFormat> examsFormatList = new ArrayList<ExamFormat>();
			String[] examsFormat = readFileAsStringArray(dataFilePath);
			String[] examFormatData = null;
			for(String examFormatLine : examsFormat) {
				examFormatData = examFormatLine.split(";");
				examsFormatList.add(new ExamFormat(toInt(examFormatData[0]), toInt(examFormatData[1]), toInt(examFormatData[2])));
			}
			return examsFormatList;
		}
		
		/**
		 * Quick shorcut to parse a string to int
		 * @param value input string to parse to int
		 * @return the primitive integer value represented by value
		 */
		private int toInt(String value) {
			return Integer.parseInt(value);
		}
		
		/**
		 * Read an input text file, and return as text lines
		 * @param filePath path to file to read
		 * @return A String array, which represents every lines of input file
		 * @throws IOException If file not found, or cannot execute BufferedReader.readLine()
		 */
		private String[] readFileAsStringArray(String filePath) throws IOException {
			//Get the text file
			File file = new File(filePath);
			if(!file.exists()) {
				throw new FileNotFoundException("File not found: " + filePath);
			}

			ArrayList<String> stringArray = new ArrayList<String>();
			//Read text from file
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;
		    while ((line = br.readLine()) != null) {
		    	stringArray.add(line);
		    }
		    return stringArray.toArray(new String[stringArray.size()]);
		}
	}
}