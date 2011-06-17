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

package com.tonnguyen.sathach.bean;

/**
 * @author Ton Nguyen
 *
 */
public class QuestionReviewSession extends Session {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4829567263083730556L;
	private Question[] questions;
	private int currentQuestionIndex;
	private long totalTime;
	private int correctAnswer;
	
	public QuestionReviewSession() {
		
	}
	
	public QuestionReviewSession(Question[] questions, int currentQuestionIndex, long totalTime, int correctAnswer, Level selectedLevel) {
		this.questions = questions;
		this.currentQuestionIndex = currentQuestionIndex;
		this.totalTime = totalTime;
		this.correctAnswer = correctAnswer;
		this.selectedLevel = selectedLevel;
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

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public int getCorrectAnswer() {
		return correctAnswer;
	}

	public void setCorrectAnswer(int correctAnswer) {
		this.correctAnswer = correctAnswer;
	}
	
	public boolean isPasses() {
		return correctAnswer >= selectedLevel.getPassPoint();
	}
}
