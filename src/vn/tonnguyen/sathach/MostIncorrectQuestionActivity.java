package vn.tonnguyen.sathach;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import vn.tonnguyen.sathach.bean.Question;
import vn.tonnguyen.sathach.bean.QuestionReviewSession;
import vn.tonnguyen.sathach.bean.QuestionState;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MostIncorrectQuestionActivity extends BaseActivity {
	private int currentQuestionIndex; // to mark the index of the current displaying question
	private Question[] examQuestions; // hold the list of random questions
	private WebView questionView; // a WebView, to display question image
	private TextView questionNavigation; // text control to display current question in total
	private Button previousButton;
	private Button nextButton;
	private QuestionNavigationQuickAction quickActionMenu; // a quick action menu which will be popped up when clicking on question navigation button
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("MostIncorrectQuestionActivity", "onCreate");
		
		context = (MyApplication)getApplicationContext();
		setContentView(R.layout.activity_exam);
		initAdMob();
		questionView = (WebView)findViewById(R.id.exam_QuestionView);
        //Make sure links in the webview is handled by the webview and not sent to a full browser
		questionView.getSettings().setSupportZoom(true);       //Zoom Control on web (You don't need this
        //if ROM supports Multi-Touch     
		questionView.getSettings().setBuiltInZoomControls(true);
		questionView.getSettings().setUseWideViewPort(true);
		questionView.setInitialScale(context.getRecentlyZoom()); // so the phone can display the whole image on screen. 
		// This value should be persist if user change the zoom level
		// so they dont have to change the zoom level every time they view a question
		
		findViewById(R.id.exam_radioGroup).setVisibility(View.INVISIBLE);
		findViewById(R.id.exam_titleBar_RemainingTime).setVisibility(View.INVISIBLE);
		
		questionNavigation = (TextView)findViewById(R.id.exam_titleBar_QuestionInfo);
		
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
		
		if(savedInstanceState != null) { 
			// question will be retrieve back and display in onRestoreInstanceState
		} else {
			initQuestions();
			// add question navigation buttons
			addQuestionNavigationButtons();
			currentQuestionIndex = 0;
			updateQuestionStates(examQuestions);
			showQuestion(currentQuestionIndex);
		}
	}
	
	/***
	 * Listen on back button pressed, to navigate through questions instead, or asking user to exit if they are viewing the first question 
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	        if(currentQuestionIndex == 0) {
	        	finish();
	        } else {
	        	previous();
	        }
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private void initQuestions() {
		QuestionDbAdapter dbHelper = new QuestionDbAdapter(this);
		dbHelper.open();
		Cursor cursor = dbHelper.fetchAllResults();
		//startManagingCursor(cursor);
		ArrayList<Question> list = new ArrayList<Question>();
		String questionName;
		int count = 0;
		while(cursor.moveToNext()) {
			if(count >= 30) {
				break;
			}
			questionName = cursor.getString(0);
			questionName = questionName.substring(0, 3);
			Log.d("ResultActivity", questionName);
			list.add(context.getQuestions().get(Integer.parseInt(questionName) - 1));
			count++;
		}
		cursor.close();
		examQuestions = list.toArray(new Question[list.size()]);
		dbHelper.close();
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
	 * Update state for a question button in navigation section
	 * @param button Question button to update UI
	 * @param state New state for questions
	 */
	private void updateQuestionNagivationState(Button button, QuestionState state) {
		if(state == QuestionState.ANSWERED) {
			button.setBackgroundResource(R.color.titleBar_answered_question);
		} else if(state == QuestionState.UNANSWERED) {
			button.setBackgroundResource(R.color.titleBar_unanswered_question);
		} else if(state == QuestionState.CORRECT) {
			button.setBackgroundResource(R.color.titleBar_correct_question);
		} else if(state == QuestionState.INCORRECT) {
			button.setBackgroundResource(R.color.titleBar_incorrect_question);
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
			questionButton.setTitle(null);
			questionButton.setIcon(examQuestions[index].isCorrect() ? getResources().getDrawable(R.drawable.correct) : getResources().getDrawable(R.drawable.incorrect));
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
	
	private void displayQuestion(Question questionToShow) throws IOException {
		Log.d("Question review", "IsCorrect: " + questionToShow.isCorrect() + ", Answer: " + questionToShow.getAnswer() + ", User choice: " + questionToShow.getUserChoice());
		/* Create a new Html that contains the full-screen image */
		Log.d("Displaying question", questionToShow.toString());
		String html = readFileAsText(getAssets().open(questionToShow.getQuestionFileName()));
		html = html.replaceAll("none;", "inline;");
		/* Finally, display the content using WebView */
		int scale = (int)(100 * questionView.getScale()); // keep the last zoom ratio
		context.setRecentlyZoom(scale);
		// base URL will point to the data folder, which includes images and css files
		questionView.loadDataWithBaseURL("file:///" + MyApplication.APPLICATION_DATA_PATH, html, "text/html", "utf-8", "");
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
	
	@Override
	protected void onSaveInstanceState(Bundle state) {
		Log.d("ExamScreen", "onSaveInstanceState " + state.toString());
		// save the current session, so next time when user come back, we will load it
		if(examQuestions != null) {
			state.putSerializable(SESSION_KEY, new QuestionReviewSession(examQuestions, currentQuestionIndex, 0, 0, null));
		}
		super.onSaveInstanceState(state);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		Log.d("ExamScreen", "onRestoreInstanceState " + state.toString());
		Log.d("Get question from saved state", "onRestore");
		// restore the last session of user
		Serializable obj = state.getSerializable(SESSION_KEY);
		if(obj != null) {
			QuestionReviewSession session = (QuestionReviewSession)obj;
			examQuestions = session.getQuestions();
			currentQuestionIndex = session.getCurrentQuestionIndex();
			// add question navigation buttons
			addQuestionNavigationButtons();
			updateQuestionStates(examQuestions);
			showQuestion(currentQuestionIndex); // display the last viewed question
		}
		super.onRestoreInstanceState(state);
	}
	
	private void updateQuestionStates(Question[] examQuestions) {
		for(int x = 0; x < examQuestions.length; x++) {
			updateQuestionNagivationState(x, QuestionState.INCORRECT);
		}
	}
}
