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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class Startup extends BaseActivity {
	public static final int WHAT_UPDATE_PERCENT = 0;
	public static final int WHAT_DOWNLOADING_RESOURCE = 1;
	public static final int WHAT_EXTRACTING_RESOURCE = 2;
	public static final int WHAT_DOWNLOAD_PROCESS_FAILED = 3;
	public static final int WHAT_DOWNLOAD_PROCESS_SUCCEED = 4;
	public static final int WHAT_EXTRACTING_RESOURCE_FAILED = 5;
	public static final int WHAT_EXTRACTING_RESOURCE_SUCCEED = 6;
	public static final int WHAT_LOADING_RESOURCE = 7;
	public static final int WHAT_LOADING_RESOURCE_FAILED = 8;
	public static final int WHAT_LOADING_RESOURCE_SUCCEED = 9;
	
	private ProgressDialog progressDialog;
	
	//private Context context;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.startup);
		Log.v("Statup", "Displaying startup dialog");
		final MyApplication application = (MyApplication)getApplicationContext();
		
		Handler threadHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				processMessage(msg, application);
				super.handleMessage(msg);
			}
		};
		final ResourceDownloadThread thread = new ResourceDownloadThread(application, threadHandler,
				MyApplication.ONLINE_DATA_FILE_URL, MyApplication.APPLICATION_DATA_PATH + "data.zip", 
				MyApplication.APPLICATION_DATA_PATH);
		if (!isResourcesAvailable()) {
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
									thread.start();
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
			thread.setResourceAvailable(true); // make sure that we wont download resource
			thread.start();
		}
	}
	
	private void processMessage(Message msg, MyApplication application) {
		// process incoming messages here
		switch(msg.what) {
		case WHAT_DOWNLOADING_RESOURCE:
		case WHAT_EXTRACTING_RESOURCE:
		case WHAT_LOADING_RESOURCE:
			Log.v("Processing", String.valueOf(msg.obj));
			if(progressDialog != null) {
				progressDialog.cancel();
				progressDialog = null;
			}
			progressDialog = new ProgressDialog(this);
			progressDialog.setCancelable(true);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage((String)msg.obj);
			progressDialog.setMax(100);
			progressDialog.setProgress(0);
			progressDialog.show();
			break;
		case WHAT_UPDATE_PERCENT:
			progressDialog.setProgress(toInt(String.valueOf(msg.obj)));
			break;
		case WHAT_DOWNLOAD_PROCESS_SUCCEED:
		case WHAT_EXTRACTING_RESOURCE_SUCCEED:
			Log.v("Startup", String.valueOf(msg.obj));
			progressDialog.cancel();
			progressDialog = null;
			break;
		case WHAT_LOADING_RESOURCE_SUCCEED:
			Log.v("Loading resource Suceed", String.valueOf(msg.obj));
			progressDialog.cancel();
			progressDialog = null;
			showHomeScreen();
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
	
	private int toInt(String value) {
		return Integer.parseInt(value);
	}

	private void showHomeScreen() {
		// open home activity
		Log.v("Statup", "Displaying home screen");
		startActivity(new Intent(this, Home.class));
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
}

class ResourceDownloadThread extends Thread {
	
	private MyApplication application;
	
	private String urlToDownload;
	
	private String targetFolderPath;
	
	private String targetFileToSave;
	
	private Handler threadHandler;
	
	private boolean resourceAvailable = false;
	
	public void setResourceAvailable(boolean value) {
		resourceAvailable = value;
	}
	
	public ResourceDownloadThread(MyApplication application, Handler threadHandler, String urlToDownload, String targetFileToSave, String targetFolderPath) {
		this(application, threadHandler, urlToDownload, targetFileToSave, targetFolderPath, false);
	}
	
	public ResourceDownloadThread(MyApplication application, Handler threadHandler, String urlToDownload, String targetFileToSave, String targetFolderPath, boolean resourceAvailable) {
		this.application = application;
		this.threadHandler = threadHandler;
		this.urlToDownload = urlToDownload;
		this.targetFileToSave = targetFileToSave;
		this.targetFolderPath = targetFolderPath;
		this.resourceAvailable = resourceAvailable;
	}
	
	public void run() {
		try {
			if(!resourceAvailable) {
				downloadResources(threadHandler, urlToDownload, targetFileToSave);
				extractZipFile(threadHandler, targetFileToSave, targetFolderPath);
			}
			loadResources();
		} finally {
			// delete zip file
			try {
				new File(targetFileToSave).delete();
			} catch (Exception e) {}
		}
	}
	
	/***
	 * Download zipped resource file from Internet, and save it to targetFileToSave
	 * @param threadHandler A thread handler, which will process messages, to update UI 
	 * @param urlToDownload URL of zip file to download
	 * @param targetFileToSave Path of the file to save
	 * @return Flag to indicate whether the process has been completed or not
	 */
	private boolean downloadResources(Handler threadHandler, String urlToDownload, String targetFileToSave) {
		InputStream inputStream = null;
		FileOutputStream fileOutput = null;
		// send message to notify that the download process is starting
		sendMessageToHandler(threadHandler, Startup.WHAT_DOWNLOADING_RESOURCE, application.getString(R.string.download_Resource_Message_Downloading));
		try {
			// set the download URL, a url that points to a file on the internet
			// this is the file to be downloaded
			URL url = new URL(urlToDownload);

			// create the new connection
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

			// set up some things on the connection
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);

			// and connect!
			urlConnection.connect();

			File dataFolder = new File(targetFileToSave).getParentFile();
			// make sure the folder was empty first
			if (dataFolder.exists()) {
				for (File file : dataFolder.listFiles()) {
					file.delete();
				}
				dataFolder.delete();
			}
			// have the object build the directory structure, if needed.
			dataFolder.mkdirs();

			Log.v("Saving location", dataFolder.getAbsolutePath());
			// create a File object for the output file
			File outputFile = new File(targetFileToSave);
			if (outputFile.exists()) {
				outputFile.delete();
			}
			// this will be used to write the downloaded data into the file we
			// created
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
				
				// send message to notify the download percent
				sendMessageToHandler(threadHandler, Startup.WHAT_UPDATE_PERCENT, 100 * downloadedSize / totalSize);
			}
			// send message to notify the download process has been completed
			sendMessageToHandler(threadHandler, Startup.WHAT_DOWNLOAD_PROCESS_SUCCEED, application.getString(R.string.download_Resource_Message_Downloaded));
			return true;
		} catch (Exception e) {
			Log.e("Download resource", e.getMessage());
			// send message to notify the error
			sendMessageToHandler(threadHandler, Startup.WHAT_DOWNLOAD_PROCESS_FAILED, e.getMessage());
			return false;
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

	/***
	 * Extract downloaded zip file to data folder
	 * @param threadHandler A thread handler, which will process messages, to update UI 
	 * @param zipFilePath Path to zip file to extract
	 * @param targetFolderPath Path to data folder to extract files to
	 * @return Flag to indicate whether the process has been completed or not
	 */
	private boolean extractZipFile(Handler threadHandler, String zipFilePath, String targetFolderPath) {
		ZipInputStream zipinputstream = null;
		// send message to notify that the extract process is starting
		sendMessageToHandler(threadHandler, Startup.WHAT_EXTRACTING_RESOURCE, application.getString(R.string.download_Resource_Message_Extracting));
		try {
			byte[] buf = new byte[1024];
			
			FileInputStream inputStream = new FileInputStream(zipFilePath);
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

				fileoutputstream = new FileOutputStream(targetFolderPath + entryName);

				while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
					fileoutputstream.write(buf, 0, n);
				}

				fileoutputstream.close();
				zipinputstream.closeEntry();
				
				// send message to notify the download percent			
				sendMessageToHandler(threadHandler, Startup.WHAT_UPDATE_PERCENT, 100 * downloadedSize / totalSize);
				
				zipentry = zipinputstream.getNextEntry();
			}// while
			// send message to notify that the extract process has been completed
			sendMessageToHandler(threadHandler, Startup.WHAT_EXTRACTING_RESOURCE_SUCCEED, application.getString(R.string.download_Resource_Message_Extracted));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			// send message to notify the error
			sendMessageToHandler(threadHandler, Startup.WHAT_EXTRACTING_RESOURCE_FAILED, e.getMessage());
			return false;
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
	 * Load resources from data files to memory
	 */
	private void loadResources() {
		// send message to notify that the resource loading process is starting
		sendMessageToHandler(threadHandler, Startup.WHAT_LOADING_RESOURCE, application.getString(R.string.loading_Data));
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
				sendMessageToHandler(threadHandler, Startup.WHAT_UPDATE_PERCENT, 80 * i / MyApplication.NUMBER_OF_QUESTIONS);
			}
			application.setQuestions(questions);

			// then read index.dat, to get level and question format data
			ArrayList<Level> levels = new ArrayList<Level>();
			String[] indexData = readFileAsStringArray(MyApplication.APPLICATION_INDEX_FILE_PATH);
			for (String line : indexData) {
				String[] data = line.split(";");
				levels.add(new Level(toInt(data[0]), data[1], MyApplication.APPLICATION_DATA_PATH + data[2],
										getExamFormats(MyApplication.APPLICATION_DATA_PATH + data[2])));
			}
			application.setLevels(levels);
			sendMessageToHandler(threadHandler, Startup.WHAT_LOADING_RESOURCE_SUCCEED, application.getString(R.string.download_Resource_Message_Loaded));
		} catch (IOException e) {
			Log.e("Loading resource", e.getMessage());
			// send message to notify the error
			sendMessageToHandler(threadHandler, Startup.WHAT_LOADING_RESOURCE_FAILED, e.getMessage());
		}
	}
	
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
	
	private int toInt(String value) {
		return Integer.parseInt(value);
	}
	
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
	
	private long lastUpdatePercentInMilliseconds = 0;
	
	private void sendMessageToHandler(Handler threadHandler, int messageID, Object what) {
		// with message type of WHAT_UPDATE_PERCENT, let's throttle it for 500 ms, to make sure within that 500ms, only 1 message was sent(performance purpose)
		if(messageID == Startup.WHAT_UPDATE_PERCENT && (System.currentTimeMillis() - lastUpdatePercentInMilliseconds) < 500) {
			return;
		}
		lastUpdatePercentInMilliseconds = System.currentTimeMillis();
		Message msg = new Message();
		msg.what = messageID;
		msg.obj = what;
		threadHandler.sendMessage(msg);
	}
}