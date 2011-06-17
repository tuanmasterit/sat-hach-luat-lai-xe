/*
 * Copyright (C) 2011  Nguyen Hoang Ton, a.k.a Ton Nguyen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.tonnguyen.sathach;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * A async task to extract downloaded data file from Internet. This task can run in background, so the extracting process
 * will be executed even if user has leave the application
 * @author Ton Nguyen
 *
 */
public class ExtractFilesTask extends AsyncTask<String, Integer, String> {
	private boolean isSucceed = false;
	private String errorMessage = "";
	
	private BaseActivity activity;
	private Context context;
	private boolean completed;
	
	public ExtractFilesTask(BaseActivity activity, Context context) {
		this.activity = activity;
		this.context = context;
	}
	
	/**
	 * Android will invoke this method when this async task was started, or when user came back to our application.
	 */
    protected void onPreExecute() {
        super.onPreExecute();
        activity.updateProgress(0);
        activity.updateProgressStatus(context.getString(R.string.download_Resource_Message_Extracting));
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
			activity.onExtractTaskCompleted(isSucceed, errorMessage);
		}
	}
}