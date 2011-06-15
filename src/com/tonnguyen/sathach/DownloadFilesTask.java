package com.tonnguyen.sathach;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * A async task to download data file from Internet. This task can run in background, so the download process
 * will be executed even if user has leave the application
 * @author Ton Nguyen
 *
 */
public class DownloadFilesTask extends AsyncTask<String, Integer, String> {
	private boolean isSucceed = false;
	private String errorMessage = "";
	
	private BaseActivity activity;
	private Context context;
	private boolean completed;
	
	public DownloadFilesTask(BaseActivity activity, Context context) {
		this.activity = activity;
		this.context = context;
	}
	
	/**
	 * Android will invoke this method when this async task was started, or when user came back to our application.
	 */
    protected void onPreExecute() {
        super.onPreExecute();
        activity.updateProgress(0);
        activity.updateProgressStatus(context.getString(R.string.download_Resource_Message_Downloading));
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
			activity.onDownloadTaskCompleted(isSucceed, errorMessage);
		}
	}
}
