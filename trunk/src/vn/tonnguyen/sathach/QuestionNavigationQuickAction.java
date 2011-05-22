package vn.tonnguyen.sathach;

import vn.tonnguyen.sathach.bean.QuestionState;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QuestionNavigationQuickAction extends net.londatiga.android.QuickAction {

	public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_AUTO = 4;
	
	private boolean isActionListCreated = false;
	
	private final int numberOfRow = 6;
	private final int numberOfQuestionInRow = 5;
	
	public QuestionNavigationQuickAction(View anchor) {
		super(anchor);
	}

	@Override
	public void show() {
		preShow();

		int[] location 		= new int[2];
		
		anchor.getLocationOnScreen(location);

		Rect anchorRect 	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] 
		                	+ anchor.getHeight());

		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		int rootWidth 		= root.getMeasuredWidth();
		int rootHeight 		= root.getMeasuredHeight();

		int screenWidth 	= windowManager.getDefaultDisplay().getWidth();
		//int screenHeight 	= windowManager.getDefaultDisplay().getHeight();

		int xPos 			= (screenWidth - rootWidth) / 2;
		int yPos	 		= anchorRect.top - rootHeight;

		boolean onTop		= true;
		
		// display on bottom
		if (rootHeight > anchorRect.top) {
			yPos 	= anchorRect.bottom;
			onTop	= false;
		}

		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX());
		
		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		
		if(!isActionListCreated) { // prevent duplication items
			createActionList();
		}
		
		window.showAtLocation(this.anchor, Gravity.NO_GRAVITY, xPos, yPos);
		
		if (animateTrack) mTrack.startAnimation(mTrackAnim);
	}
	
	/**
	 * Create action list
	 * 	 
	 */
	private void createActionList() {
		View view;
		String title;
		OnClickListener listener;
		
		// create 6 rows, each row has 5 buttons
		int i = 0;
		LinearLayout row;
		for(int rowIndex = 0; rowIndex < numberOfRow; rowIndex++) {
			row = (LinearLayout) inflater.inflate(R.layout.quickaction_row, mTrack, false);
			for(int item = 0; item < numberOfQuestionInRow; item++) {
				title 		= actionList.get(i).getTitle();
				listener	= actionList.get(i).getListener();
		
				view 		= getActionItem(title, row, listener);
			
				view.setFocusable(true);
				view.setClickable(true);
				 
				Log.d("ActionMenu", "Added question " + item + " to row " + rowIndex);
				row.addView(view, item);
				
				i++;
			}
			mTrack.addView(row, rowIndex);
		}
		isActionListCreated = true;
	}
	
	/**
	 * Get action item {@link View}
	 * 
	 * @param title action item title
	 * @param listener {@link View.OnClickListener} action item listener
	 * @return action item {@link View}
	 */
	private View getActionItem(String title, ViewGroup root, OnClickListener listener) {
		LinearLayout container	= (LinearLayout) inflater.inflate(R.layout.action_item, root, false);
		TextView text 			= (TextView) container.findViewById(R.id.title);
		
		if (title != null) {
			text.setText(title);
		} else {
			text.setVisibility(View.GONE);
		}
		
		setButtonState(text, QuestionState.UNANSWERED);
		
		if (listener != null) {
			container.setOnClickListener(listener);
		}

		return container;
	}
	
	/**
	 * Set style for button, according to the state.
	 * @param text TextView control to set style
	 * @param state State of the question
	 */
	private void setButtonState(TextView text, QuestionState state) {
		if(state == QuestionState.UNANSWERED) {
			//text.setBackgroundColor(R.color.titleBar_unanswered_question);
			text.setTextColor(R.color.titleBar_unanswered_question);
			text.setTypeface(Typeface.DEFAULT_BOLD);
			text.setTextSize(15);
		} else {
			//text.setBackgroundColor(R.color.question_action_menu_answered_question_background_color);
			text.setTextColor(R.color.question_action_menu_answered_question_color);
			text.setTypeface(Typeface.DEFAULT);
			text.setTextSize(11);
		}
	}
	
	/**
	 * Update quick action button, according to the state of question
	 * @param questionIndex Index of the question, which will be used to look up the TextView control
	 * @param state State of the question
	 */
	public void updateQuestionState(int questionIndex, QuestionState state) {
		if(!isActionListCreated) { // make sure all items have been added
			createActionList();
		}
		int rowIndex = questionIndex / numberOfRow;
		int buttonIndex = questionIndex % numberOfQuestionInRow;
		
		Log.d("ActionMenu", "rowIndex=" + rowIndex + " - buttonIndex=" + buttonIndex);
		
		LinearLayout row = (LinearLayout)mTrack.getChildAt(rowIndex);
		LinearLayout buttonContainer = (LinearLayout)row.getChildAt(buttonIndex);
		setButtonState((TextView)buttonContainer.getChildAt(1), state);
	}
}
