package vn.tonnguyen.sathach;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class Startup extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.startup);
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
									downloadResources();
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
		return isFileExist("");
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
	 */
	private void downloadResources() {
		// open home activity
		startActivity(new Intent(getApplicationContext(), Home.class));
	}
}
