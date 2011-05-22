package vn.tonnguyen.sathach;

import java.util.ArrayList;

import vn.tonnguyen.sathach.bean.Level;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class HomeActivity extends BaseActivity {
	private MyApplication context;
	private AdView adView;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d("HomeActivity onCreate", "Displaying Home screen");
		
		setContentView(R.layout.activity_home);

		initAdMob();

		context = (MyApplication)getApplicationContext();
		initComponents();
	}
	
	private void initAdMob() {
		// Look up the AdView as a resource and load a request.
		if(adView == null) {
			adView = (AdView)this.findViewById(R.id.adViewComponent);
		}
	    adView.loadAd(createAdRequest());
	}
	
	private AdRequest createAdRequest() {
		AdRequest re = new AdRequest();
	    re.setTesting(true);
	    re.setGender(AdRequest.Gender.MALE);
	    return re;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_home);

		initAdMob();
		context = (MyApplication) getApplicationContext();
		initComponents();
	}

	
	/**
	 * Init data, bind event for buttons
	 */
	private void initComponents() {
		ArrayList<Level> levels = context.getLevels();
		if(levels == null || levels.size() < 1) {
			Toast.makeText(context, context.getString(R.string.error_data_corrupted), Toast.LENGTH_LONG)
				.show();
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
							startActivity(new Intent((MyApplication)getApplicationContext(), ExamActivity.class));
						} else {
							Toast.makeText(context, context.getString(R.string.error_pleaseSelect_Level), Toast.LENGTH_LONG)
								.show();
						}
					}
				}).setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// on cancel button action
						context.vibrateIfEnabled();
						dialog.cancel();
					}
				});
				selectLevelDialog.show();
			}
		});

	    // Register the onClick listener with the implementation above
		((Button)findViewById(R.id.startup_btn_exit)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
	}
}
