package vn.tonnguyen.sathach;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class Startup extends Activity {
	public static final int WHAT_UPDATE_PERCENT = 0;
	public static final int WHAT_DOWNLOADING_RESOURCE = 1;
	public static final int WHAT_EXTRACTING_RESOURCE = 2;
	public static final int WHAT_DOWNLOAD_PROCESS_FAILED = 3;
	public static final int WHAT_DOWNLOAD_PROCESS_SUCCEED = 4;
	public static final int WHAT_EXTRACTING_RESOURCE_FAILED = 5;
	public static final int WHAT_EXTRACTING_RESOURCE_SUCCEED = 6;
	
	private ProgressDialog progressDialog;
	
	private Context context;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.startup);
		Log.v("Statup", "Displaying startup dialog");
		context = this;
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
									progressDialog = ProgressDialog.show(context, context.getString(R.string.download_Resource_Message_Downloading), "");
									String targetFilePathToSave = MyApplication.APPLICATION_DATA_PATH + "data.zip";
									Handler threadHandler = new Handler() {
										@Override
										public void handleMessage(Message msg) {
											// process incoming messages here
											switch(msg.what) {
											case WHAT_UPDATE_PERCENT:
												progressDialog.setMessage(String.valueOf(msg.obj) + "%");
												break;
											case WHAT_DOWNLOADING_RESOURCE:
											case WHAT_DOWNLOAD_PROCESS_SUCCEED:
											case WHAT_EXTRACTING_RESOURCE:
												progressDialog.setTitle((String)msg.obj);
												break;
											case WHAT_EXTRACTING_RESOURCE_SUCCEED:
												progressDialog.cancel(); // close the progress dialog
												// open Home activity
												loadResources();
												showHomeScreen();
												break;
											default: // error occurred
												// display confirm dialog to retry, or exit
												progressDialog.cancel();
												Toast toast = Toast.makeText(context, context.getString(R.string.download_Resource_Error_Occurred) + (String)msg.obj, Toast.LENGTH_LONG);
												toast.show();
												finish();
												break;
											}
											super.handleMessage(msg);
										}
									};
									ResourceDownloadThread thread = new ResourceDownloadThread(context, threadHandler,
																	MyApplication.ONLINE_DATA_FILE_URL, targetFilePathToSave, 
																	MyApplication.APPLICATION_DATA_PATH);
									thread.start();
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
			loadResources();
			showHomeScreen();
		}
	}
	
	private void loadResources() {
		
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
	
	private Context context;
	
	private String urlToDownload;
	
	private String targetFolderPath;
	
	private String targetFileToSave;
	
	private Handler threadHandler;
	public Handler GetThreadHandler() {
		return threadHandler;
	}
	
	public ResourceDownloadThread(Context context, Handler threadHandler, String urlToDownload, String targetFileToSave, String targetFolderPath) {
		this.context = context;
		this.threadHandler = threadHandler;
		this.urlToDownload = urlToDownload;
		this.targetFileToSave = targetFileToSave;
		this.targetFolderPath = targetFolderPath;
	}
	
	public void run() {
		try {
			downloadResources(threadHandler, urlToDownload, targetFileToSave);
			extractZipFile(threadHandler, targetFileToSave, targetFolderPath);
		} finally {
			// delete zip file
			try {
				new File(targetFileToSave).delete();
			} catch (Exception e) {}
		}
	}
	
	/***
	 * Get resources from Internet
	 * 
	 * @return true if the download process has been completed, false otherwise
	 */
	private boolean downloadResources(Handler threadHandler, String urlToDownload, String targetFileToSave) {
		InputStream inputStream = null;
		FileOutputStream fileOutput = null;
		// send message to notify that the download process is starting
		Message msg = new Message();
		msg.what = Startup.WHAT_DOWNLOADING_RESOURCE;
		msg.obj = context.getString(R.string.download_Resource_Message_Downloading);
		threadHandler.sendMessage(msg);
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
				msg = new Message();
				msg.what = Startup.WHAT_UPDATE_PERCENT;
				msg.obj = 100 * downloadedSize / totalSize;
				threadHandler.sendMessage(msg);
			}
			// send message to notify the download process has been completed
			msg = new Message();
			msg.what = Startup.WHAT_DOWNLOAD_PROCESS_SUCCEED;
			threadHandler.sendMessage(msg);
			return true;
		} catch (Exception e) {
			Log.e("Download resource", e.getMessage());
			// send message to notify the error
			msg = new Message();
			msg.what = Startup.WHAT_DOWNLOAD_PROCESS_FAILED;
			msg.obj = e.getMessage();
			threadHandler.sendMessage(msg);
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

	private boolean extractZipFile(Handler threadHandler, String zipFilePath, String targetFolderPath) {
		ZipInputStream zipinputstream = null;
		// send message to notify that the extract process is starting
		Message msg = new Message();
		msg.what = Startup.WHAT_EXTRACTING_RESOURCE;
		msg.obj = context.getString(R.string.download_Resource_Message_Extracting);
		threadHandler.sendMessage(msg);
		try {
			byte[] buf = new byte[1024];
			
			FileInputStream inputStream = new FileInputStream(zipFilePath);
			// total compressed size, to calculate extract process percent
			int totalSize = inputStream.available();
			zipinputstream = new ZipInputStream(inputStream);

			ZipEntry zipentry = zipinputstream.getNextEntry();
			long downloadedSize = 0;
			while (zipentry != null) { // for each entry to be extracted
				// get downloaded size, to calculate extract process's percent
				downloadedSize += zipentry.getCompressedSize();
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
				
				// send message to notify the download percent
				msg = new Message();
				msg.what = Startup.WHAT_UPDATE_PERCENT;
				msg.obj = 100 * downloadedSize / totalSize;
				threadHandler.sendMessage(msg);
				zipentry = zipinputstream.getNextEntry();
			}// while
			// send message to notify that the extract process has been completed
			msg = new Message();
			msg.what = Startup.WHAT_EXTRACTING_RESOURCE_SUCCEED;
			threadHandler.sendMessage(msg);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			// send message to notify the error
			msg = new Message();
			msg.what = Startup.WHAT_EXTRACTING_RESOURCE_FAILED;
			threadHandler.sendMessage(msg);
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