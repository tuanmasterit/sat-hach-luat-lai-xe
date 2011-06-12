package com.tonnguyen.sathach;

import net.londatiga.android.ActionItem;

import com.tonnguyen.sathach.bean.QuestionState;

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
