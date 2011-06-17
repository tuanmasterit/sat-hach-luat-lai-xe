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

import java.io.Serializable;

/**
 * @author Ton Nguyen
 *
 */
public class Question implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4160490554549912221L;
	
	private String questionFileName;
	private int numberOfAnswers;
	private int answer;
	private int userChoice; // which answer user has chosen. <= 0 means no answer
	private boolean isCorrect;

	public Question(String questionFileName, int numberOfAnswers, int answer) {
		this.questionFileName = questionFileName;
		this.numberOfAnswers = numberOfAnswers;
		this.answer = answer;
	}

	public String getQuestionFileName() {
		return questionFileName;
	}

	public void setQuestionFileName(String questionFileName) {
		this.questionFileName = questionFileName;
	}

	public int getNumberOfAnswers() {
		return numberOfAnswers;
	}

	public void setNumberOfAnswers(int numberOfAnswers) {
		this.numberOfAnswers = numberOfAnswers;
	}

	public int getAnswer() {
		return answer;
	}

	public void setAnswer(int answer) {
		this.answer = answer;
	}
	
	/**
	 * Gets which answer user has chosen. <= 0 means no answer
	 * @return
	 */
	public int getUserChoice() {
		return userChoice;
	}

	/**
	 * Sets which answer user has chosen. <= 0 means no answer
	 * @param userChoice answer that user has chosen. <= 0 means no answer given
	 */
	public void setUserChoice(int userChoice) {
		this.userChoice = userChoice;
	}

	@Override
	public String toString() {
		return String.format("Question %s, number of answer : %d, answer: %d user choice: %d",
				getQuestionFileName(), getNumberOfAnswers(), getAnswer(), getUserChoice());
	}
	
	/**
	 * Return a copy of this
	 */
	public Question clone() {
		return new Question(questionFileName, numberOfAnswers, answer);
	}

	public boolean isCorrect() {
		return isCorrect;
	}

	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}
}
