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
import java.util.ArrayList;

import com.tonnguyen.sathach.bean.Level;
import com.tonnguyen.sathach.bean.QuestionReviewSession;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Ton Nguyen
 *
 */
public class StartupActivity extends BaseActivity {
	public static final int REQUEST_CODE_START_EXAM = 1;
	public static final int REQUEST_CODE_VIEW_RESULT = 2;
	
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
	
	private LinearLayout progressContainer;
	private LinearLayout buttonContainer;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d("Statup onCreate", "Displaying startup dialog");
		
		setContentView(R.layout.activity_home);
		loadResource();
		Object retained = getLastNonConfigurationInstance();
		if(retained != null) {
			Log.i("Statup onCreate", "Reclaiming previous background task.");
			setProcessingState();
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
				setProcessingState();
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
			setProcessingState();
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
	
	@Override
	protected void updateProgressStatus(String status) {
		progressStatus.setText(status);
	}
	
	@Override
	protected void updateProgress(int progress) {
		progressBar.setProgress(progress);
	}
	
	private void loadResource() {
		Log.d("Statup loadResource", "loadResource");
		Log.d("context.getQuestions()", String.valueOf(context.getQuestions() == null));
		Log.d("context.getLevels()", String.valueOf(context.getLevels() == null));
		progressBar = (ProgressBar)findViewById(R.id.startup_progressBar);
		progressStatus = (TextView)findViewById(R.id.startup_progressStatus);
		progressContainer = (LinearLayout)findViewById(R.id.startup_resourceDownloadContainer);
		buttonContainer = (LinearLayout)findViewById(R.id.startup_buttonsContainer);
		if(context.getQuestions() == null || context.getLevels() == null) { // check if resource has been loaded into memory
			threadHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					processMessage(msg, context);
					super.handleMessage(msg);
				}
			};
			if (!isResourcesAvailable()) { // check if resource has been downloaded into data folder
				progressContainer.setVisibility(View.VISIBLE);
				buttonContainer.setVisibility(View.GONE);
				final StartupActivity thisActivity = this;
				((Button)findViewById(R.id.startup_downloadButton)).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						context.vibrateIfEnabled();
						setProcessingState();
						
						// Start the download process and showing the process dialog
						Log.d("Statup", "Displaying download dialog");
						isDownloading = true;
						downloadTask = new DownloadFilesTask(thisActivity, context);
						downloadTask.execute(getOnlineDataFileUrl(), MyApplication.APPLICATION_SAVING_ZIP_FILE_PATH);
					}
				});
				
			} else {
				isLoadingResource = true;
				loaderTask = new ResourceLoaderThread(this, context);
				loaderTask.execute(new String[0]);
			}
		} else {
			showHomeActivity();
		}
	}
	
	private void setProcessingState() {
		findViewById(R.id.startup_confirmMessage).setVisibility(View.GONE);
		findViewById(R.id.startup_downloadButton).setVisibility(View.GONE);
		
		progressContainer.setVisibility(View.VISIBLE);
		buttonContainer.setVisibility(View.GONE);
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
	@Override
	protected void onDownloadTaskCompleted(boolean completed, String errorMessage) {
		Log.i("Startup Activity", "Activity " + this + " has been notified the task is complete.");
		isDownloading = false;
		if(completed) {
			// start extracting downloaded file
			isExtracting = true;
			extractTask = new ExtractFilesTask(this, context);
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
	@Override
	protected void onExtractTaskCompleted(boolean completed, String errorMessage) {
		Log.i("Startup Activity", "Activity " + this + " has been notified the task is complete.");
		isExtracting = false;
		if(completed) {
			// start loading resource
			loaderTask = new ResourceLoaderThread(this, context);
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
	@Override
	protected void onLoadingTaskCompleted(boolean completed, String errorMessage) {
		Log.i("Startup Activity", "Activity " + this + " has been notified the task is complete.");
		isLoadingResource = false;
		if(completed) {
			showHomeActivity();
		} else {
			// send message to notify the error
			sendMessageToHandler(StartupActivity.WHAT_ERROR, errorMessage);
		}
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
			
			progressContainer.setVisibility(View.VISIBLE);
			buttonContainer.setVisibility(View.GONE);
			
			findViewById(R.id.startup_confirmMessage).setVisibility(View.VISIBLE);
			findViewById(R.id.startup_downloadButton).setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			progressStatus.setVisibility(View.GONE);
			
			// displaying error message
			new AlertDialog.Builder(StartupActivity.this)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle(R.string.download_Resource_Title)
			.setMessage(application.getString(R.string.download_Resource_Error_Occurred) + (String)msg.obj)
			.setPositiveButton(R.string.exam_exit_confirm_ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
			.show();
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
		progressContainer.setVisibility(View.GONE);
		buttonContainer.setVisibility(View.VISIBLE);
		initLayout();
		initAdMob();
	}
	
	private void initLayout() {
		ArrayList<Level> levels = context.getLevels();
		if(levels == null || levels.size() < 1) {
			// resource loading problem
			Toast.makeText(context, context.getString(R.string.error_data_corrupted), Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		final String[] menuItems = new String[levels.size()];
		for(int i = 0; i < levels.size(); i++) {
			menuItems[i] = levels.get(i).getName();
		}
	    // Register the onClick listener with the implementation above
		((Button)findViewById(R.id.startup_btn_startexam)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				AlertDialog.Builder selectLevelDialog = new AlertDialog.Builder(StartupActivity.this);
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
						context.vibrateIfEnabled();
						Log.d("Selected level index to create new exam", String.valueOf(context.getRecentlyLevel()));
						if(context.getRecentlyLevel() >= 0) {
							//startActivity(new Intent((MyApplication)getApplicationContext(), ExamActivity.class));
							Intent i = new Intent((MyApplication)getApplicationContext(), ExamActivity.class);      
					        startActivityForResult(i, REQUEST_CODE_START_EXAM);
						} else {
							Toast.makeText(context, context.getString(R.string.error_pleaseSelect_Level), Toast.LENGTH_LONG)
								.show();
						}
					}
				}).setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// on cancel button action
						context.vibrateIfEnabled();
						dialog.dismiss();
					}
				});
				selectLevelDialog.show();
			}
		});

	    // Register the onClick listener with the implementation above
		((Button)findViewById(R.id.startup_btn_exit)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Toast.makeText(context, context.getString(R.string.see_you_again), Toast.LENGTH_LONG).show();
				context.vibrateIfEnabled();
				// exit
				finish();
			}
		});
		
		// show preference screen when clicking on preference button
		((Button)findViewById(R.id.startup_btn_config)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				Intent settingsActivity = new Intent((MyApplication)getApplicationContext(), Preferences.class);
				startActivity(settingsActivity);
			}
		});
		
		// show 30 most incorrect question screen when clicking on most incorrect button
		((Button)findViewById(R.id.startup_btn_mostincorrect)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				Intent settingsActivity = new Intent((MyApplication)getApplicationContext(), MostIncorrectQuestionActivity.class);
				startActivity(settingsActivity);
			}
		});
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
		case REQUEST_CODE_START_EXAM: // back from exam screen
	        if(resultCode == RESULT_CANCELED) { // seems like we never have this case
	            // do nothing
	        } else if(resultCode == RESULT_OK) { // user has completed the test, show the result screen
	        	Intent intent = new Intent((MyApplication)getApplicationContext(), ResultActivity.class);
	        	intent.putExtra(PARAM_KEY, (QuestionReviewSession)data.getSerializableExtra(PARAM_KEY));
	        	startActivityForResult(intent, REQUEST_CODE_VIEW_RESULT);
	        }
			break;
		case REQUEST_CODE_VIEW_RESULT: // back from result screen
			// check if user want to see questions review
			if(resultCode == RESULT_CANCELED) {
				// they dont want to check questions review, do nothing
			} else {
				// display question review screen
	        	Intent intent = new Intent((MyApplication)getApplicationContext(), QuestionReviewActivity.class);
	        	intent.putExtra(QuestionReviewActivity.PARAM_KEY, (QuestionReviewSession)data.getSerializableExtra(QuestionReviewActivity.PARAM_KEY));
	        	startActivity(intent);
			}
			break;
		default:
			break;
		}
    }

	/**
	 * Check if resources are available
	 * 
	 * @return true if resources exist, false otherwise.
	 */
	private boolean isResourcesAvailable() {
		return isFileExist(MyApplication.APPLICATION_DATA_PATH + "161.png")
				&& isFileExist(MyApplication.APPLICATION_DATA_PATH + "268.png")
				&& isFileExist(MyApplication.APPLICATION_DATA_PATH + "330.png")
				&& isFileExist(MyApplication.APPLICATION_DATA_PATH + "377.png")
				&& isFileExist(MyApplication.APPLICATION_DATA_PATH + "398.png")
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
}