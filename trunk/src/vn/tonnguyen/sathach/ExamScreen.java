package vn.tonnguyen.sathach;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import vn.tonnguyen.sathach.bean.ExamFormat;
import vn.tonnguyen.sathach.bean.Level;
import vn.tonnguyen.sathach.bean.Question;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class ExamScreen extends BaseActivity {
	private MyApplication application;
	private int currentQuestionIndex; // to mark the index of the current displaying question
	private Question[] examQuestions; // hold the list of random questions
	private WebView full; // a WebView, to display question image
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		application = (MyApplication)getApplicationContext();
		if(application.getSelectedLevelIndex() < 0) {
			Toast.makeText(application, application.getString(R.string.error_pleaseSelect_Level), Toast.LENGTH_LONG)
				.show();
			return;
		}
		
		setContentView(R.layout.exam);
		
		// generate exam
		examQuestions = generateRandomQuestion(application.getLevels().get(application.getSelectedLevelIndex()));

		full = (WebView)findViewById(R.id.webView);
        //Make sure links in the webview is handled by the webview and not sent to a full browser
		full.setWebViewClient(new WebViewClient());
		full.getSettings().setSupportZoom(true);       //Zoom Control on web (You don't need this
        //if ROM supports Multi-Touch     
		full.getSettings().setBuiltInZoomControls(true);
		//full.getSettings().setLoadWithOverviewMode(true);
		full.getSettings().setUseWideViewPort(true);
		full.setInitialScale(75); // so the phone can display the whole image on screen. This value should be persist if user change the zoom level
		// so they dont have to change the zoom level every time they view a question
		
		// bind click event for next and previous buttons
		((Button)findViewById(R.id.buttonFirst)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoFirst();
			}
		});
		((Button)findViewById(R.id.buttonLast)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoLast();
			}
		});
		((Button)findViewById(R.id.buttonNext)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				next();
			}
		});
		((Button)findViewById(R.id.buttonPrevious)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				previous();
			}
		});
		
		// display the first question
		currentQuestionIndex = 0;
		showQuestion(currentQuestionIndex);
	}
	
	/***
	 * Listen on back button pressed, to navigate through questions instead, or asking user to exit if they are viewing the first question 
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	        if(currentQuestionIndex == 0) {
	        	// asking user to exit if they are viewing the first question
	        	onExit();
	        } else {
	        	previous();
	        }
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private void onExit() {
		// confirm to exit exam
    	new AlertDialog.Builder(ExamScreen.this)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setTitle(R.string.exam_exit_confirm_title)
		.setMessage(R.string.exam_exit_confirm_message)
		.setPositiveButton(R.string.exam_exit_confirm_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
		.setNegativeButton(R.string.exam_exit_confirm_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						// do nothing
					}
				}).show();
	}
	
	private void gotoFirst() {
		currentQuestionIndex = 0;
		showQuestion(currentQuestionIndex);
	}
	
	private void gotoLast() {
		currentQuestionIndex = examQuestions.length - 1;
		showQuestion(currentQuestionIndex);
	}
	
	private void next() {
		showQuestion(++currentQuestionIndex);
	}
	
	private void previous() {
		showQuestion(--currentQuestionIndex);
	}
	
	/**
	 * Display the question to UI
	 * @param questionIndex index of the question, to get from examQuestions
	 */
	private void showQuestion(int questionIndex) {
		/* Create a new Html that contains the full-screen image */
		Log.d("Displaying question", "Index: " + questionIndex + " - " + examQuestions[questionIndex].toString());
		String html = "<html><img src=\"" + examQuestions[questionIndex].getPictureName() + "\"></html>";
		/* Finally, display the content using WebView */
		full.loadDataWithBaseURL("file:///" + MyApplication.APPLICATION_DATA_PATH, html, "text/html", "utf-8", "");
	}
	
	private Question[] generateRandomQuestion(Level selectedLevel) {
		// generate exam:
		Hashtable<Integer, Question> examQuestions = new Hashtable<Integer, Question>(); // hold the list of random questions
		Random randomGenerator = new Random(); // used to generate random int
		ArrayList<ExamFormat> examFormats = selectedLevel.getExamsFormat();
		int questionIndex;
		ExamFormat examformat;
		int formatSize = examFormats.size();
		for (int x = 0; x < formatSize; x++) { // for loop will be faster that for each loop with ArrayLis
			examformat = examFormats.get(x);
			// loop through NumberOfQuestion to get enough questions
			for (int i = 1; i <= examformat.getNumberOfQuestion(); i++) {
				// get random question index, and make sure it does not exist in the list of questions
				do {
					questionIndex = getRandomInteger(examformat.getFrom(), examformat.getTo(), randomGenerator);
				} while (examQuestions.containsKey(questionIndex));
				// Now we have the question that was not existed in question list
				Log.v("questionIndex", String.valueOf(questionIndex));
				examQuestions.put(questionIndex, application.getQuestions().get(questionIndex));
			}
		}
		return examQuestions.values().toArray(new Question[examQuestions.size()]);
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
