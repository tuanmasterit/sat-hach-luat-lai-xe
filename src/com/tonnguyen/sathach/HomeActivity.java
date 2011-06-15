package com.tonnguyen.sathach;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tonnguyen.sathach.bean.Level;
import com.tonnguyen.sathach.bean.QuestionReviewSession;

public class HomeActivity extends BaseActivity {
	public static final int REQUEST_CODE_START_EXAM = 1;
	public static final int REQUEST_CODE_VIEW_RESULT = 2;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("HomeActivity onCreate", "Displaying Home screen");
		
		setContentView(R.layout.activity_home);
		
		initAdMob();
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
				AlertDialog.Builder selectLevelDialog = new AlertDialog.Builder(HomeActivity.this);
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
}
