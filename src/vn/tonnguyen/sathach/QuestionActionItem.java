package vn.tonnguyen.sathach;

import vn.tonnguyen.sathach.bean.QuestionState;
import net.londatiga.android.ActionItem;

public class QuestionActionItem extends ActionItem {
	private QuestionState questionState;

	public QuestionActionItem() {
		
	}
	
	public QuestionState getQuestionState() {
		return questionState;
	}

	public void setQuestionState(QuestionState questionState) {
		this.questionState = questionState;
	}
}
