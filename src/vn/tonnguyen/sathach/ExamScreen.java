package vn.tonnguyen.sathach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class ExamScreen extends BaseActivity {
	private MyApplication context;
	private int currentQuestionIndex; // to mark the index of the current displaying question
	private Question[] examQuestions; // hold the list of random questions
	private WebView webView; // a WebView, to display question image
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		context = (MyApplication)getApplicationContext();
		int levelIndex = context.getRecentlyLevel();
		if(levelIndex < 0) {
			Toast.makeText(context, context.getString(R.string.error_pleaseSelect_Level), Toast.LENGTH_LONG)
				.show();
			return;
		}
		
		setContentView(R.layout.exam);
		webView = (WebView)findViewById(R.id.webView);
        //Make sure links in the webview is handled by the webview and not sent to a full browser
		//full.setWebViewClient(new WebViewClient());
		webView.getSettings().setSupportZoom(true);       //Zoom Control on web (You don't need this
        //if ROM supports Multi-Touch     
		webView.getSettings().setBuiltInZoomControls(true);
		//full.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.setInitialScale(context.getRecentlyZoom()); // so the phone can display the whole image on screen. 
		// This value should be persist if user change the zoom level
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
		((RadioButton)findViewById(R.id.radioAnswer1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateChoice(1);
			}
		});
		((RadioButton)findViewById(R.id.radioAnswer2)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateChoice(2);
			}
		});
		((RadioButton)findViewById(R.id.radioAnswer3)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateChoice(3);
			}
		});
		((RadioButton)findViewById(R.id.radioAnswer4)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateChoice(4);
			}
		});
		
		// TODO Check to restore session
		if(savedInstanceState != null) { // question will be retrieve back and display in onRestoreInstanceState
			//Log.d("Get question from saved state", "onCreate");
			//examQuestions = (Question[])savedInstanceState.getSerializable("CurrentQuestions");
		} else {
			// generate exam
			examQuestions = generateRandomQuestion(context.getLevels().get(levelIndex));
			// display the first question
			currentQuestionIndex = 0;
			showQuestion(currentQuestionIndex);
		}
	}
	
	/**
	 * Create option menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.exam_screen_menu, menu);
	    return true;
	}
	
	/**
	 * Process when user click on option menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.end_exam:
	        onExit();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle state) {
		Log.d("ExamScreen", "onSaveInstanceState " + state.toString());
		// save the current session, so next time when user come back, we will load it
		state.putSerializable("CurrentQuestions", examQuestions);
		state.putInt("currentQuestionIndex", currentQuestionIndex);
		saveSession();
		super.onSaveInstanceState(state);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		Log.d("ExamScreen", "onRestoreInstanceState " + state.toString());
		Log.d("Get question from saved state", "onRestore");
		// restore the last session of user
		examQuestions = (Question[])state.getSerializable("CurrentQuestions");
		currentQuestionIndex = state.getInt("currentQuestionIndex");
		showQuestion(currentQuestionIndex); // display the last viewed question
		restoreSession();
		super.onRestoreInstanceState(state);
	}
	
	/**
	 * Store current session, so next time when user come back, we will restore it. Only 1 session will be stored
	 */
	private void saveSession() {
		
	}
	
	/**
	 * Get the last session and restore it
	 */
	private void restoreSession() {
		
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
						// show result
						showResult();
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
	
	private void showResult() {
		// get the number of right answers
		int rightChoice = 0;
		for(Question question : examQuestions) {
			if(question.getAnswer() == question.getUserChoice()) {
				rightChoice++;
			}
		}
		
		Toast toast = Toast.makeText(context, rightChoice + "/" + examQuestions.length, Toast.LENGTH_LONG);
		toast.show();
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
		if(currentQuestionIndex >= examQuestions.length - 1) { // no more question left to show
			return;
		}
		showQuestion(++currentQuestionIndex);
	}
	
	private void previous() {
		if(currentQuestionIndex <= 0) { // already at the first question
			return;
		}
		showQuestion(--currentQuestionIndex);
	}
	
	/**
	 * Update user choice for current viewing question
	 * @param choice choice that user has chosen
	 */
	private void updateChoice(int choice) {
		Question currentQuestion = examQuestions[currentQuestionIndex];
		Log.d("User answer", "Question " + currentQuestion.getPictureName() + 
								" - User choice: " + choice + 
								" - Answer: " + currentQuestion.getAnswer());
		currentQuestion.setUserChoice(choice);
	}
	
	/**
	 * Display the question to UI
	 * @param questionIndex index of the question, to get from examQuestions
	 */
	private void showQuestion(int questionIndex) {
		RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radioGroup); 
		Question questionToShow = examQuestions[questionIndex];
		/* Create a new Html that contains the full-screen image */
		Log.d("Displaying question", "Index: " + questionIndex + " - " + questionToShow.toString());
		String html = "<html><img src=\"" + questionToShow.getPictureName() + "\"></html>";
		/* Finally, display the content using WebView */
		int scale = (int)(100 * webView.getScale());
		context.setRecentlyZoom(scale);
		webView.loadDataWithBaseURL("file:///" + MyApplication.APPLICATION_DATA_PATH, html, "text/html", "utf-8", "");
		webView.setInitialScale(scale);
		
		// disable answer-radio-button that won't be used:
		radioGroup.clearCheck(); // clear answer
		Log.d("radioGroup.getChildCount()", String.valueOf(radioGroup.getChildCount()));
		int childCount = radioGroup.getChildCount();
		// disable all radio button in this group
		for(int i = 0; i < childCount; i++) {
			radioGroup.getChildAt(i).setEnabled(false);
		}
		// then enable only if answer is available
		for(int i = 0; i < questionToShow.getNumberOfAnswers(); i++) {
			radioGroup.getChildAt(i).setEnabled(true);
		}
		// if user has chosen a choice for this question, lets select it
		if(questionToShow.getUserChoice() > 0) {
			((RadioButton)radioGroup.getChildAt(questionToShow.getUserChoice() - 1)).setChecked(true);
		}
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
				examQuestions.put(questionIndex, context.getQuestions().get(questionIndex));
			}
		}
		// sort the question list as random
		Question[] questionArr = examQuestions.values().toArray(new Question[examQuestions.size()]);
		Collections.shuffle(Arrays.asList(questionArr));
		return questionArr;
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
