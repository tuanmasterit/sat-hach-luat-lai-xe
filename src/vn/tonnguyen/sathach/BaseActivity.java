package vn.tonnguyen.sathach;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

/**
 * Provides a base class for all activity
 * @author Ton Nguyen
 *
 */
public class BaseActivity extends Activity {
	public static final String PARAM_KEY = "InputQuestionReviewData";
	public static final String SESSION_KEY = "CurrentQuestionReviewSession";
	
	protected AdView adView;
	protected MyApplication context;
	
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
	
	public void initAdMob() {
		if(findViewById(R.id.adViewComponent) == null) {
			return;
		}
		// Look up the AdView as a resource and load a request.
		//if(adView == null) {
			adView = (AdView)findViewById(R.id.adViewComponent);
			AlphaAnimation animation = new AlphaAnimation( 0.0f, 1.0f );
            animation.setDuration( 400 );
            animation.setFillAfter( true );
            animation.setInterpolator( new AccelerateInterpolator() );
			adView.setAnimation(animation);
		//}
		if(!adView.isRefreshing()) {
			adView.loadAd(createAdRequest());
		}
	}
	
	private AdRequest createAdRequest() {
		AdRequest re = new AdRequest();
	    re.setTesting(false);
	    //re.setKeywords(createKeywords());
	    return re;
	}
	
//	private Set<String> createKeywords() {
//		Set<String> set = new HashSet<String>();
//		set.add("bảo hiểm");
//		set.add("xe hơi");
//		set.add("oto");
//		set.add("ô tô");
//		set.add("auto");
//		set.add("nội thất");
//		return set;
//	}
	
	public String getTimeAsString(long timeLeft) {
		// make sure we don't have negative number
		if(timeLeft < 0) {
			timeLeft = 0;
		}
		return String.format("%02d:%02d", 
					(int)((timeLeft / 1000) / 60),
					(int)((timeLeft / 1000) % 60));
	}
	
	public boolean isLargeScreen() {
		return checkScreenSize(Configuration.SCREENLAYOUT_SIZE_LARGE);
	}
	
	public boolean isSmallScreen() {
		return checkScreenSize(Configuration.SCREENLAYOUT_SIZE_SMALL);
	}
	
	public boolean isNormalScreen() {
		return checkScreenSize(Configuration.SCREENLAYOUT_SIZE_NORMAL);
	}
	
	private boolean checkScreenSize(int targetScreenSize) {
		return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == targetScreenSize;
	}
	
	/**
	 * Read an input text file, and return as text lines
	 * @param filePath path to file to read
	 * @return A String array, which represents every lines of input file
	 * @throws IOException If file not found, or cannot execute BufferedReader.readLine()
	 */
	public String[] readFileAsStringArray(InputStream inputStream) throws IOException {
		ArrayList<String> stringArray = new ArrayList<String>();
		//Read text from file
	    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream), 8 * 1024);
	    String line;
	    while ((line = br.readLine()) != null) {
	    	stringArray.add(line);
	    }
	    return stringArray.toArray(new String[stringArray.size()]);
	}
	
	/**
	 * Read an input text file, and return as text
	 * @param filePath path to file to read
	 * @return A String array, which represents every lines of input file
	 * @throws IOException If file not found, or cannot execute BufferedReader.readLine()
	 */
	public String readFileAsText(InputStream inputStream) throws IOException {
		String content = "";
		//Read text from file
	    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream), 8 * 1024);
	    String line;
	    while ((line = br.readLine()) != null) {
	    	content += line;
	    }
	    return content;
	}
}
