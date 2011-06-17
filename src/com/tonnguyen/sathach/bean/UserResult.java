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
public class UserResult {
	private String questionName;
	private int numOfAppear;
	private int correct;
	private int incorrect;
	
	public UserResult() {
		this(null, 0, 0, 0);
	}
	
	public UserResult(String questionName, int numOfAppear, int correct, int incorrect) {
		this.questionName = questionName;
		this.numOfAppear = numOfAppear;
		this.correct = correct;
		this.incorrect = incorrect;
	}

	public String getQuestionName() {
		return questionName;
	}

	public void setQuestionName(String questionName) {
		this.questionName = questionName;
	}

	public int getNumOfAppear() {
		return numOfAppear;
	}

	public void setNumOfAppear(int numOfAppear) {
		this.numOfAppear = numOfAppear;
	}

	public int getCorrect() {
		return correct;
	}

	public void setCorrect(int correct) {
		this.correct = correct;
	}

	public int getIncorrect() {
		return incorrect;
	}

	public void setIncorrect(int incorrect) {
		this.incorrect = incorrect;
	}
	
	@Override
	public String toString() {
		return "questionName=" + questionName
				+ ", numOfAppear=" + numOfAppear
				 + ", correct=" + correct
				 + ", incorrect=" + incorrect;
	}
}
