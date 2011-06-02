package vn.tonnguyen.sathach;

import android.app.Activity;
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
}
