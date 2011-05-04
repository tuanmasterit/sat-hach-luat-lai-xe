package vn.tonnguyen.sathach;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Startup extends Activity {
	// link: http://stackoverflow.com/questions/5028421/android-unzip-a-folder
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.startup);

		// MyApplication application = (MyApplication)getApplicationContext();
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
								public void onClick(DialogInterface dialog,
										int which) {
									// Start the download process
									String targetFilePathToSave = MyApplication.APPLICATION_DATA_PATH
											+ "data.zip";
									ResourceDownloadThread thread = new ResourceDownloadThread(getApplicationContext(), 
																	MyApplication.ONLINE_DATA_FILE_URL, targetFilePathToSave, 
																	MyApplication.APPLICATION_DATA_PATH);
									thread.start();
									while(thread.IsRunning()) {
										// update process
										int percent = thread.GetProgressPercent();
										String status = thread.GetProgressStatusMessage();
									}
									if(thread.IsSuccess()) {
										// open Home activity
										startActivity(new Intent(getApplicationContext(), Home.class));
									}
								}
							})
					.setNegativeButton(R.string.download_Resource_Button_No,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// Close the activity
									finish();
								}
							}).show();
		} else {
			// open home activity
			startActivity(new Intent(this, Home.class));
		}
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
	
	private Context context;
	
	private String urlToDownload;
	
	private String targetFolderPath;
	
	private String targetFileToSave;
	
	private int progressPercent;
	
	public int GetProgressPercent() {
		return progressPercent;
	}
	
	private String progressStatusmessage;
	
	public String GetProgressStatusMessage() {
		return progressStatusmessage;
	}
	
	private boolean isRunning = false;
	
	public boolean IsRunning() {
		return isRunning;
	}
	
	private boolean isSuccess = false;
	
	public boolean IsSuccess() {
		return isSuccess;
	}
	
	public ResourceDownloadThread(Context context, String urlToDownload, String targetFileToSave, String targetFolderPath) {
		this.context = context;
		this.urlToDownload = urlToDownload;
		this.targetFileToSave = targetFileToSave;
		this.targetFolderPath = targetFolderPath;
	}
	
	public void run() {
		isRunning = true;
		isSuccess = false;
		try {
			downloadResources(urlToDownload, targetFileToSave);
			extractZipFile(targetFileToSave, targetFolderPath);
			isSuccess = true;
		} finally {
			// delete zip file
			try {
				new File(targetFileToSave).delete();
			} catch (Exception e) {}
			isRunning = false;
		}
	}
	
	/***
	 * Get resources from Internet
	 * 
	 * @return true if the download process has been completed, false otherwise
	 */
	private boolean downloadResources(String urlToDownload, String targetFileToSave) {
		InputStream inputStream = null;
		FileOutputStream fileOutput = null;
		progressStatusmessage = context.getString(R.string.download_Resource_Message_Downloading);
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
				// this is where you would do something to report the prgress,
				// like this maybe
				progressPercent = 100 * downloadedSize / totalSize;
			}
			return true;
		} catch (MalformedURLException e) {
			Log.e("Download resource", e.getMessage());
			return false;
		} catch (IOException e) {
			Log.e("Download resource", e.getMessage());
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

	private boolean extractZipFile(String zipFilePath, String targetFolderPath) {
		ZipInputStream zipinputstream = null;
		progressStatusmessage = context.getString(R.string.download_Resource_Message_Extracting);
		try {
			byte[] buf = new byte[1024];
			ZipEntry zipentry;
			zipinputstream = new ZipInputStream(new FileInputStream(zipFilePath));

			zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) {
				// for each entry to be extracted
				String entryName = zipentry.getName();
				Log.v("Extracting resources", "entryname " + entryName);
				int n;
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
				zipentry = zipinputstream.getNextEntry();

			}// while

			return true;
		} catch (Exception e) {
			e.printStackTrace();
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
}