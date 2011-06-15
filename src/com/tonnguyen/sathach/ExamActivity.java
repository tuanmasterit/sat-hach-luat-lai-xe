package com.tonnguyen.sathach;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tonnguyen.sathach.bean.ExamFormat;
import com.tonnguyen.sathach.bean.Level;
import com.tonnguyen.sathach.bean.Question;
import com.tonnguyen.sathach.bean.QuestionReviewSession;
import com.tonnguyen.sathach.bean.QuestionState;
import com.tonnguyen.sathach.bean.Session;

public class ExamActivity extends BaseActivity {
	private int currentQuestionIndex; // to mark the index of the current displaying question
	private long remainingTime;// = EXAM_TIME; // the remaining exam time that user has(they have 20 minutes for each exam)
	private Question[] examQuestions; // hold the list of random questions
	private WebView questionView; // a WebView, to display question image
	private TextView questionNavigation; // text control to display current question in total
	private RadioGroup radioGroup; // radio group which contains all answer choices
	private TextView remainingTimeTextView; // text control to display remaining time
	private Handler threadHandler; // handler to process messages from RemainingTimeUpdater thread
	private boolean isInExam; // flag to indicate whether user is in exam
	private Button previousButton;
	private Button nextButton;
	private Level selectedLevel;
	private QuestionNavigationQuickAction quickActionMenu; // a quick action menu which will be popped up when clicking on question navigation button
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Log.d("ExamScreen", "onCreate");
		int levelIndex = context.getRecentlyLevel();
		if(levelIndex < 0) {
			Toast.makeText(context, context.getString(R.string.error_pleaseSelect_Level), Toast.LENGTH_LONG)
				.show();
			return;
		}
		
		setContentView(R.layout.activity_exam);
		questionView = (WebView)findViewById(R.id.exam_QuestionView);
        //Make sure links in the webview is handled by the webview and not sent to a full browser
		//full.setWebViewClient(new WebViewClient());
		questionView.getSettings().setSupportZoom(true);       //Zoom Control on web (You don't need this
        //if ROM supports Multi-Touch     
		questionView.getSettings().setBuiltInZoomControls(true);
		//full.getSettings().setLoadWithOverviewMode(true);
		questionView.getSettings().setUseWideViewPort(true);
		questionView.setInitialScale(context.getRecentlyZoom()); // so the phone can display the whole image on screen. 
		// This value should be persist if user change the zoom level
		// so they dont have to change the zoom level every time they view a question
		
		questionNavigation = (TextView)findViewById(R.id.exam_titleBar_QuestionInfo);
		radioGroup = (RadioGroup)findViewById(R.id.exam_radioGroup);
		remainingTimeTextView = (TextView)findViewById(R.id.exam_titleBar_RemainingTime);
		
		// bind click event for next and previous buttons
		nextButton = (Button)findViewById(R.id.exam_buttonNext);
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				next();
			}
		});
		previousButton = (Button)findViewById(R.id.exam_buttonPrevious);
		previousButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				previous();
			}
		});
		((RadioButton)findViewById(R.id.exam_radioAnswer1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				updateChoice(1);
			}
		});
		((RadioButton)findViewById(R.id.exam_radioAnswer2)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				updateChoice(2);
			}
		});
		((RadioButton)findViewById(R.id.exam_radioAnswer3)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				updateChoice(3);
			}
		});
		((RadioButton)findViewById(R.id.exam_radioAnswer4)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				updateChoice(4);
			}
		});
		
		// add listen click event for question navigation buttons
		//addClickListenerForQuestionNavigationButtons();
		
		quickActionMenu = new QuestionNavigationQuickAction(findViewById(R.id.exam_titleBar_question_navigation_container));
		
		// clicking on navigation container will show a quick action dialog, to choose question to goto
		findViewById(R.id.exam_titleBar_question_navigation_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				quickActionMenu.show();
				quickActionMenu.setSelectedQuestion(currentQuestionIndex);
			}
		});
		// add question navigation buttons
		addQuestionNavigationButtons();
		
		// create thread handler for processing message from RemainingTimeUpdater
		threadHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				processMessage(msg, context);
				super.handleMessage(msg);
			}
		};
		
		if(savedInstanceState != null) { 
			// question will be retrieve back and display in onRestoreInstanceState
		} else {
			selectedLevel = context.getLevels().get(levelIndex);
			// generate exam
			examQuestions = generateRandomQuestion(selectedLevel);
			// display the first question
			currentQuestionIndex = 0;
			remainingTimeTextView.setText("20:00");
			setAllQuestionNavitionToUnAnswered();
			showQuestion(currentQuestionIndex);
			// start a thread to display, update remaining time, the exam duration is 20 minutes
			isInExam = true;
			remainingTime = selectedLevel.getExamTime();
			
			// showing first-time-help message
			if(context.isFirstTimeDoingExam()) {
				Toast.makeText(context, R.string.exam_firsttime_help_message, Toast.LENGTH_LONG).show();
			}
			
			//remainingTime = 60000;
			
			// this thread will be started in onResume event
			//new RemainingTimeUpdater().start();
		}
	}
	
	/**
	 * Add 30 question navigation buttons to the status bar on top
	 */
	private void addQuestionNavigationButtons() {
		LinearLayout container = (LinearLayout)findViewById(R.id.exam_titleBar_question_navigation_container);
		int count = container.getChildCount();
		for(int x = 0; x < count; x++) {
			addQuestionNavigationButtonsForALine((LinearLayout)container.getChildAt(x), x);
		}
	}
	
	/**
	 * Add 10 questions navigation buttons to a line, which is a LinearLayout
	 * @param line A buttons container to add button to
	 * @param lineIndex Index of line
	 */
	private void addQuestionNavigationButtonsForALine(LinearLayout line, int lineIndex) {
		Button button;
		int start = lineIndex * 10;
		int end = start + 10;
		for(int x = start; x < end; x++) {
			final int index = x;
			// create a button from template
			button = (Button)getLayoutInflater().inflate(R.layout.navigation_button, line, false);
			// add that button to the linearlayout
			line.addView(button);
			// Add click event for this button, to show quick action menu
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					context.vibrateIfEnabled();
					quickActionMenu.show();
					quickActionMenu.setSelectedQuestion(currentQuestionIndex);
				}
			});
			
			// add another button to quick action menu
			QuestionActionItem questionButton = new QuestionActionItem();
			questionButton.setTitle(String.valueOf(index + 1));
			//chart.setIcon(getResources().getDrawable(R.drawable.chart));
			questionButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					context.vibrateIfEnabled();
					currentQuestionIndex = index;
					showQuestion(currentQuestionIndex);
					quickActionMenu.dismiss();
				}
			});
			quickActionMenu.addActionItem(questionButton);
		}
	}
	
	/**
	 * Set all question navigation at the very top to unanswered state
	 */
	private void setAllQuestionNavitionToUnAnswered() {
		for(int x = 0; x < 30; x++) {
			updateQuestionNagivationState(x, QuestionState.UNANSWERED);
		}
	}
	
	/**
	 * Update state for a question button in navigation section
	 * @param button Question button to update UI
	 * @param state New state for questions
	 */
	private void updateQuestionNagivationState(Button button, QuestionState state) {
		if(state == QuestionState.ANSWERED) {
			button.setBackgroundResource(R.color.titleBar_answered_question);
		} else {
			button.setBackgroundResource(R.color.titleBar_unanswered_question);
		}
	}
	
	/**
	 * Update state for a question, looking by question index, which starts from zero
	 * @param questionIndex Index of question to update state, which starts from zero
	 * @param state New state for questions
	 */
	private void updateQuestionNagivationState(int questionIndex, QuestionState state) {
		int lineNumber = (questionIndex / 10) + 1;
		int buttonIndex = questionIndex % 10;
		
		LinearLayout line;
		switch (lineNumber) {
		case 1:
			line = (LinearLayout)findViewById(R.id.exam_titleBar_question_navigation_line1);
			break;
		case 2:
			line = (LinearLayout)findViewById(R.id.exam_titleBar_question_navigation_line2);
			break;
		default:
			line = (LinearLayout)findViewById(R.id.exam_titleBar_question_navigation_line3);
			break;
		}
		updateQuestionNagivationState((Button)line.getChildAt(buttonIndex), state);
		
		quickActionMenu.updateQuestionState(questionIndex, state);
	}
	
	/**
	 * Create option menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_exam_screen, menu);
	    return true;
	}
	
	/**
	 * Process when user click on option menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.exam_screen_end_exam:
	    	context.vibrateIfEnabled();
	        onExit();
	        return true;
	    case R.id.exam_screen_preference:
	    	context.vibrateIfEnabled();
			startActivity(new Intent(context, Preferences.class));
	        return true;
	    case R.id.exam_screen_help:
	    	context.vibrateIfEnabled();
			startActivity(new Intent(context, ExamHelpActivity.class));
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle state) {
		Log.d("ExamScreen", "onSaveInstanceState " + state.toString());
		// stop the remaining time updater thread
		isInExam = false;
		// save the current session, so next time when user come back, we will load it
		if(examQuestions != null) {
			state.putSerializable(SESSION_KEY, new Session(examQuestions, currentQuestionIndex, remainingTime, selectedLevel));
		}
		super.onSaveInstanceState(state);
	}
	
	@Override
	public void onResume() {
		Log.d("ExamScreen onResume", "onResume");
		super.onResume();
		restartRemainingTimeUpdater();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		Log.d("ExamScreen", "onRestoreInstanceState " + state.toString());
		Log.d("Get question from saved state", "onRestore");
		// restore the last session of user
		Serializable obj = state.getSerializable(SESSION_KEY);
		if(obj != null) {
			Session session = (Session)obj;
			examQuestions = session.getQuestions();
			currentQuestionIndex = session.getCurrentQuestionIndex();
			remainingTime = session.getRemainingTime();
			selectedLevel = session.getSelectedLevel();
			// update question navigation section, to mark which questions have been answered, which was not
			for(int x = 0; x < examQuestions.length; x++) {
				updateQuestionNagivationState(x, examQuestions[x].getUserChoice() > 0 ? QuestionState.ANSWERED : QuestionState.UNANSWERED);
			}
			showQuestion(currentQuestionIndex); // display the last viewed question
			//// start the remaining time updater thread -> No, this will be invoked on onResume
			//restartRemainingTimeUpdater();
		}
		super.onRestoreInstanceState(state);
	}
	
	private void restartRemainingTimeUpdater() {
		// make sure this flag it true which means that user is in exam
		isInExam = true;
		new RemainingTimeUpdater().start();
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
    	new AlertDialog.Builder(ExamActivity.this)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setTitle(R.string.exam_exit_confirm_title)
		.setMessage(R.string.exam_exit_confirm_message)
		.setPositiveButton(R.string.exam_exit_confirm_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						context.vibrateIfEnabled();
						isInExam = false;
						// user has done at least once, let's turn the first-time flag to false
						if(context.isFirstTimeDoingExam()) {
							context.setFirstTimeDoingExam(false);
						}
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
						context.vibrateIfEnabled();
						// do nothing
					}
				}).show();
	}
	
	private void showResult() {
		// get the number of right answers
		int rightChoice = 0;
		QuestionDbAdapter dbHelper = new QuestionDbAdapter(this);
		dbHelper.open();
		for(Question question : examQuestions) {
			if(question.getAnswer() == question.getUserChoice()) {
				question.setCorrect(true);
				rightChoice++;
			} else {
				question.setCorrect(false);
			}
			// store the result to DB, then we can list most incorrect answer later
			dbHelper.increaseResult(question.getQuestionFileName(), question.isCorrect());
		}
		dbHelper.close();
		QuestionReviewSession session = new QuestionReviewSession(examQuestions, 0, selectedLevel.getExamTime() - remainingTime, rightChoice, selectedLevel);
		Intent data = new Intent();
		data.putExtra(PARAM_KEY, session);
		setResult(RESULT_OK, data);
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
		Log.d("User answer", "Question " + currentQuestion.getQuestionFileName() + 
								" - User choice: " + choice + 
								" - Answer: " + currentQuestion.getAnswer());
		currentQuestion.setUserChoice(choice);
		updateQuestionNagivationState(currentQuestionIndex, QuestionState.ANSWERED);
		// show help message to guide user how to exit, when they have answered the last question, at the first exam
		if(currentQuestionIndex == examQuestions.length - 1 && context.isFirstTimeDoingExam()) {
			Toast.makeText(context, R.string.exam_firsttime_howtoexit_message, Toast.LENGTH_LONG).show();
		}
	}
	
	private void displayQuestion(Question questionToShow) throws IOException {
		/* Create a new Html that contains the full-screen image */
		Log.d("Displaying question", questionToShow.toString());
		String html = readFileAsText(getAssets().open(questionToShow.getQuestionFileName()));
		/* Finally, display the content using WebView */
		int scale = (int)(100 * questionView.getScale()); // keep the last zoom ratio
		context.setRecentlyZoom(scale);
		// base URL will point to the data folder, which includes images and css files
		questionView.loadDataWithBaseURL("file:///" + MyApplication.APPLICATION_DATA_PATH, html, "text/html", "utf-8", "");
		//questionView.loadUrl("file:///" + MyApplication.APPLICATION_DATA_PATH + questionToShow.getQuestionFileName());
		questionView.setInitialScale(scale);
	}
	
	/**
	 * Display the question to UI
	 * @param questionIndex index of the question, to get from examQuestions
	 */
	private void showQuestion(int questionIndex) {
		Question questionToShow = examQuestions[questionIndex];
		try {
			displayQuestion(questionToShow);
		} catch (IOException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			Log.e("Exam screen", e.getMessage());
		}
		
		// update text to show current question in total:
		questionNavigation.setText(String.format("%02d", questionIndex + 1) + "/" + examQuestions.length);
		
		// disable answer-radio-button that won't be used:
		radioGroup.clearCheck(); // clear answer
		Log.d("radioGroup.getChildCount()", String.valueOf(radioGroup.getChildCount()));
		int childCount = radioGroup.getChildCount();
		// disable all radio button in this group
		for(int i = 0; i < childCount; i++) {
			radioGroup.getChildAt(i).setEnabled(false);
			radioGroup.getChildAt(i).setVisibility(View.INVISIBLE);
		}
		// then enable only if answer is available
		for(int i = 0; i < questionToShow.getNumberOfAnswers(); i++) {
			radioGroup.getChildAt(i).setEnabled(true);
			radioGroup.getChildAt(i).setVisibility(View.VISIBLE);
		}
		// if user has chosen a choice for this question, lets select it
		if(questionToShow.getUserChoice() > 0) {
			((RadioButton)radioGroup.getChildAt(questionToShow.getUserChoice() - 1)).setChecked(true);
		}
		
		// disable previous button if questionIndex == 0, and next button if questionIndex == last question index
		previousButton.setEnabled(true);
		previousButton.setVisibility(View.VISIBLE);
		nextButton.setEnabled(true);
		nextButton.setVisibility(View.VISIBLE);
		if(questionIndex <= 0) {
			previousButton.setEnabled(false);
			previousButton.setVisibility(View.INVISIBLE);
		} else if(questionIndex >= examQuestions.length - 1) {
			nextButton.setEnabled(false);
			nextButton.setVisibility(View.INVISIBLE);
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
				Log.d("questionIndex", String.valueOf(questionIndex));
				// invoke clone to make sure it was not referenced to the original questions,
				// which may lead to already-been-answered question when doing exam
				examQuestions.put(questionIndex, context.getQuestions().get(questionIndex).clone());
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

	public static final int WHAT_UPDATE_REMAINING_TIME = 1;
	public static final int WHAT_TIME_IS_UP = 2;
	
	/**
	 * Pushes a message onto the end of the message queue after all pending messages before the current time. 
	 * It will be received in handleMessage(Message), in the thread attached to this handler.
	 * @param messageID Integer ID of the message, to identify a message
	 */
	private void sendMessageToHandler(int messageID) {
		Message msg = new Message();
		msg.what = messageID;
		threadHandler.sendMessage(msg);
	}
	
	private void processMessage(Message msg, MyApplication application) {
		// process incoming messages here
		switch(msg.what) {
		case WHAT_UPDATE_REMAINING_TIME:
			//Log.d("ExamScreen", "Updating remaining time...");
			remainingTimeTextView.setText(getTimeAsString(remainingTime));
			break;
		case WHAT_TIME_IS_UP:
			Log.d("ExamScreen", "Time is up!");
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle(R.string.exam_exit_timeIsUpDialog_title)
			.setMessage(R.string.exam_exit_timeIsUpDialog_message)
			.setPositiveButton(R.string.exam_exit_timeIsUpDialog_viewResult_button,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							context.vibrateIfEnabled();
							// show result
							showResult();
							finish();
						}
					})
			.show();
			break;
//		default: // error occurred
//			break;
		}
	}
	
	
	
	/**
	 * A thread to update remaining time of Exam screen
	 * @author Ton Nguyen
	 *
	 */
	private class RemainingTimeUpdater extends Thread {
		
		private static final long ONE_SECOND = 1000;
		
		private long lastMoment;
		
		/**
		 * Create an instance of remaining time updater.
		 */
		public RemainingTimeUpdater() {
		}
		
		/**
		 * Thread's start point
		 */
		public void run() {
			lastMoment = System.currentTimeMillis();
			doWork();
		}

		/**
		 * Update remaining time while user is in exam
		 */
		private void doWork() {
			while(isInExam) {
				Log.d("ExamScreen", "RemainingTimeUpdater is running...");
				// check if user has time left, otherwise, notify them to stop and see the exam result
				if(remainingTime > 0) {
					remainingTime = remainingTime - (System.currentTimeMillis() - lastMoment);
					lastMoment = System.currentTimeMillis();
					sendMessageToHandler(WHAT_UPDATE_REMAINING_TIME);
					try {
						if(isInExam) {
							sleep(ONE_SECOND);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					// time is up
					isInExam = false;
					sendMessageToHandler(WHAT_TIME_IS_UP);
				}
			}
			Log.d("ExamScreen", "RemainingTimeUpdater has been destroyed");
			// user decided to end exam, lets destroy this thread
		}
	}
}
