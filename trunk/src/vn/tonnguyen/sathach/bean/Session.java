package vn.tonnguyen.sathach.bean;

import java.io.Serializable;

public class Session implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4829567263083730556L;
	private Question[] questions;
	private int currentQuestionIndex;
	private long remainingTime;
	
	public Session() {
		
	}
	
	public Session(Question[] questions, int currentQuestionIndex, long remainingTime) {
		this.questions = questions;
		this.currentQuestionIndex = currentQuestionIndex;
		this.remainingTime = remainingTime;
	}

	public Question[] getQuestions() {
		return questions;
	}

	public void setQuestions(Question[] questions) {
		this.questions = questions;
	}

	public int getCurrentQuestionIndex() {
		return currentQuestionIndex;
	}

	public void setCurrentQuestionIndex(int currentQuestionIndex) {
		this.currentQuestionIndex = currentQuestionIndex;
	}

	public long getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(long remainingTime) {
		this.remainingTime = remainingTime;
	}
}
