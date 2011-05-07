package vn.tonnguyen.sathach;

import java.util.ArrayList;

import vn.tonnguyen.sathach.bean.Level;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Home extends BaseActivity {
	private Button newExamButton;
	private Button exitButton;
	private ArrayList<Level> levels;
	private MyApplication application;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		application = (MyApplication)getApplicationContext();
		// get list of menu item, which will be shown to ask user to select a level to create new exam
		levels = application.getLevels();
		if(levels == null || levels.size() < 1) {
			Toast.makeText(application, application.getString(R.string.error_data_corrupted), Toast.LENGTH_LONG)
				.show();
			return;
		}
		final String[] menuItems = new String[levels.size()];
		for(int i = 0; i < levels.size(); i++) {
			menuItems[i] = levels.get(i).getName();
		}
		
		// Capture our button from layout
		newExamButton = (Button)findViewById(R.id.button_new_game);
	    // Register the onClick listener with the implementation above
		newExamButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO check if we have a pending exam, to asking for resume or create new
				AlertDialog.Builder selectLevelDialog = new AlertDialog.Builder(Home.this);
				selectLevelDialog.setTitle(R.string.home_SelectlLevel_Title);
				selectLevelDialog.setSingleChoiceItems(menuItems, application.getRecentlyLevel(), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// onClick Action
						// the level list has been ordered by index, so whichButton will be the selected index
						application.setRecentlyLevel(whichButton);
					}
				}).setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// on Ok button action
						Log.v("Selected level index to create new exam", String.valueOf(application.getRecentlyLevel()));
						if(application.getRecentlyLevel() >= 0) {
							startActivity(new Intent((MyApplication)getApplicationContext(), ExamScreen.class));
						} else {
							Toast.makeText(application, application.getString(R.string.error_pleaseSelect_Level), Toast.LENGTH_LONG)
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

		exitButton = (Button)findViewById(R.id.button_exit);
	    // Register the onClick listener with the implementation above
		exitButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// exit
				finish();
			}
		});
	}
}