package vn.tonnguyen.sathach;

import java.util.Hashtable;
import java.util.Random;

import vn.tonnguyen.sathach.bean.ExamFormat;
import vn.tonnguyen.sathach.bean.Question;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ExamScreen extends BaseActivity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		MyApplication application = (MyApplication)getApplicationContext();
		Hashtable<Integer, Question> examQuestions = new Hashtable<Integer, Question>();
		Random randomGenerator = new Random();
		int questionIndex;
		// generate B1 exam:
		for (ExamFormat examformat : application.getLevels().get(0).getExamsFormat()) {
			// loop through NumberOfQuestion to get enough questions
			for (int i = 1; i <= examformat.getNumberOfQuestion(); i++) {
				// get random question index, and make sure it does not exist in
				// the list of questions
				do {
					questionIndex = getRandomInteger(examformat.getFrom(),
							examformat.getTo(), randomGenerator);
				} while (examQuestions.containsKey(questionIndex));
				// Now we have the question that was not existed in question list
				Log.v("questionIndex", String.valueOf(questionIndex));
				examQuestions.put(questionIndex, application.getQuestions().get(questionIndex));
			}
		}
		
		String text = "";
		for(Integer key : examQuestions.keySet()) {
			text += (application.getQuestions().get(key).toString() + "\n");
		}

		/* Using WebView to display the full-screen image */
		
		setContentView(R.layout.exam);
		WebView full = (WebView)findViewById(R.id.webView);
        //Make sure links in the webview is handled by the webview and not sent to a full browser
		full.setWebViewClient(new WebViewClient());
		
		full.getSettings().setSupportZoom(true);       //Zoom Control on web (You don't need this
        //if ROM supports Multi-Touch     
		full.getSettings().setBuiltInZoomControls(true);
		//full.getSettings().setLoadWithOverviewMode(true);
		full.getSettings().setUseWideViewPort(true);
		full.setInitialScale(75);

		/* Create a new Html that contains the full-screen image */
		String html = (text + "<html><img src=\"017.jpg\"></html>");
		/* Finally, display the content using WebView */
		full.loadDataWithBaseURL("file:///" + MyApplication.APPLICATION_DATA_PATH, html, "text/html", "utf-8",
				"");
	}
	
	private static int getRandomInteger(int aStart, int aEnd, Random aRandom) {
		if (aStart > aEnd) {
			throw new IllegalArgumentException("Start cannot exceed End.");
		}
		// get the range, casting to long to avoid overflow problems
		long range = (long) aEnd - (long) aStart + 1;
		// compute a fraction of the range, 0 <= frac < range
		long fraction = (long) (range * aRandom.nextDouble());
		int randomNumber = (int) (fraction + aStart);
		return randomNumber;
	}
}
