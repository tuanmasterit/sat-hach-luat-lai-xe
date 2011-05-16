package vn.tonnguyen.sathach;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * Provides a base class for all activity
 * @author Ton Nguyen
 *
 */
public class BaseActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);

//		// Hiding the status bar at the top in Android
//		// This will clear WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN flag 
//		// and set WindowManager.LayoutParams.FLAG_FULLSCREEN flag for main window.
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
}
