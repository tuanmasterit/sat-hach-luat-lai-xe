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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Startup extends Activity {

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
									if (downloadResources(
											MyApplication.ONLINE_DATA_FILE_URL,
											targetFilePathToSave)
											&& extractZipFile(
													targetFilePathToSave,
													MyApplication.APPLICATION_DATA_PATH)) {
										// delete zip file
										new File(targetFilePathToSave).delete();
										// open Home activity
										startActivity(new Intent(
												getApplicationContext(),
												Home.class));
									} else {
										// Display confirm message to retry
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

	/***
	 * Get resources from Internet
	 * 
	 * @return true if the download process has been completed, false otherwise
	 */
	private boolean downloadResources(String urlToDownload,
			String targetFileToSave) {
		InputStream inputStream = null;
		FileOutputStream fileOutput = null;
		try {
			// set the download URL, a url that points to a file on the internet
			// this is the file to be downloaded
			URL url = new URL(urlToDownload);

			// create the new connection
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();

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
			int bufferLength = 0; // used to store a temporary size of the
									// buffer

			// now, read through the input buffer and write the contents to the
			// file
			while ((bufferLength = inputStream.read(buffer)) > 0) {
				// add the data in the buffer to the file in the file output
				// stream (the file on the sd card
				fileOutput.write(buffer, 0, bufferLength);
				// add up the size so we know how much is downloaded
				downloadedSize += bufferLength;
				// this is where you would do something to report the prgress,
				// like this maybe
				updateProgress(downloadedSize, totalSize);
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
		try {
			byte[] buf = new byte[1024];
			ZipEntry zipentry;
			zipinputstream = new ZipInputStream(
					new FileInputStream(zipFilePath));

			zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) {
				// for each entry to be extracted
				String entryName = zipentry.getName();
				System.out.println("entryname " + entryName);
				int n;
				FileOutputStream fileoutputstream;
				File newFile = new File(entryName);
				String directory = newFile.getParent();

				if (directory == null) {
					if (newFile.isDirectory())
						break;
				}

				fileoutputstream = new FileOutputStream(targetFolderPath
						+ entryName);

				while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
					fileoutputstream.write(buf, 0, n);

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

	private void updateProgress(int downloadedSize, int totalSize) {
		int percent = 100 * downloadedSize / totalSize;
		Log.v("Download percent", String.valueOf(percent) + "%");
	}
}
