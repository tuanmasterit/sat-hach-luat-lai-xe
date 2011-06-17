/*
 * Copyright (C) 2011  Nguyen Hoang Ton, a.k.a Ton Nguyen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.tonnguyen.sathach;

import java.io.IOException;
import java.io.Serializable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.tonnguyen.sathach.bean.Question;
import com.tonnguyen.sathach.bean.QuestionReviewSession;
import com.tonnguyen.sathach.bean.QuestionState;

/**
 * @author Ton Nguyen
 *
 */
public class QuestionReviewActivity extends BaseActivity {
	private int currentQuestionIndex; // to mark the index of the current displaying question
	private Question[] examQuestions; // hold the list of random questions
	private QuestionReviewSession session;
	private long totalTime; // total time that user has spent for the last exam
	private WebView questionView; // a WebView, to display question image
	private TextView questionNavigation; // text control to display current question in total
	private RadioGroup radioGroup; // radio group which contains all answer choices
	private TextView remainingTimeTextView; // text control to display remaining time
	private Button previousButton;
	private Button nextButton;
	private QuestionNavigationQuickAction quickActionMenu; // a quick action menu which will be popped up when clicking on question navigation button
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Log.d("QuestionReviewActivity", "onCreate");
		
		if(savedInstanceState == null) { // load at firt time, not resume
			Bundle extras = getIntent().getExtras();
			if(extras == null)
			{
				Toast.makeText(context, context.getString(R.string.error_question_review_session_not_found), Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			session = (QuestionReviewSession)extras.getSerializable(PARAM_KEY);
			examQuestions = session.getQuestions();
			totalTime = session.getTotalTime();
		}
		
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
			// add question navigation buttons
			addQuestionNavigationButtons();
			currentQuestionIndex = 0;
			remainingTimeTextView.setText(getTimeAsString(totalTime));
			updateQuestionStates(examQuestions);
			showQuestion(currentQuestionIndex);
		}
	}
	
	private void updateQuestionStates(Question[] examQuestions) {
		for(int x = 0; x < examQuestions.length; x++) {
			updateQuestionNagivationState(x, examQuestions[x].isCorrect() ? QuestionState.CORRECT : QuestionState.INCORRECT);
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
	
	/**
	 * Create option menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_question_review_screen, menu);
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
			Intent settingsActivity = new Intent(context, Preferences.class);
			startActivity(settingsActivity);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle state) {
		Log.d("ExamScreen", "onSaveInstanceState " + state.toString());
		// save the current session, so next time when user come back, we will load it
		if(examQuestions != null) {
			session.setCurrentQuestionIndex(currentQuestionIndex);
			state.putSerializable(SESSION_KEY, session);
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
			session = (QuestionReviewSession)obj;
			examQuestions = session.getQuestions();
			currentQuestionIndex = session.getCurrentQuestionIndex();
			totalTime = session.getTotalTime();
			remainingTimeTextView.setText(getTimeAsString(totalTime));
			// add question navigation buttons
			addQuestionNavigationButtons();
			updateQuestionStates(examQuestions);
			showQuestion(currentQuestionIndex); // display the last viewed question
		}
		super.onRestoreInstanceState(state);
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
    	new AlertDialog.Builder(QuestionReviewActivity.this)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setTitle(R.string.questionreview_exit_confirm_title)
		.setMessage(R.string.questionreview_exit_confirm_message)
		.setPositiveButton(R.string.questionreview_exit_confirm_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						context.vibrateIfEnabled();
						finish();
					}
				})
		.setNegativeButton(R.string.questionreview_exit_confirm_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						context.vibrateIfEnabled();
						// do nothing
					}
				}).show();
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
		
		setAnswerAndUserChoice(questionToShow);
		
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
	
	private void setAnswerAndUserChoice(Question question) {
		radioGroup.clearCheck(); // clear answer
		int childCount = radioGroup.getChildCount();
		// disable all radio button in this group
		for(int i = 0; i < childCount; i++) {
			//radioGroup.getChildAt(i).setEnabled(false);
			radioGroup.getChildAt(i).setVisibility(View.INVISIBLE);
		}
		int userChoice = question.getUserChoice();
		if(userChoice >= 1) {
			RadioButton button = (RadioButton)radioGroup.getChildAt(userChoice - 1);
			button.setVisibility(View.VISIBLE);
			button.setBackgroundDrawable(null);
			button.setButtonDrawable(null);
			if(!question.isCorrect()) {
				button.setButtonDrawable(userChoice == 1 ? R.drawable.radio_button_1_incorrect 
																		: userChoice == 2 ? R.drawable.radio_button_2_incorrect
																		: userChoice == 3 ? R.drawable.radio_button_3_incorrect 
																		: R.drawable.radio_button_4_incorrect);
			} else {
				button.setButtonDrawable(userChoice == 1 ? R.drawable.radio_button_1_selected 
						: userChoice == 2 ? R.drawable.radio_button_2_selected
						: userChoice == 3 ? R.drawable.radio_button_3_selected 
						: R.drawable.radio_button_4_selected);
			}
		}
	}
}
