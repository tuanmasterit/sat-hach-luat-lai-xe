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

import net.londatiga.android.ActionItem;

import com.tonnguyen.sathach.bean.QuestionState;

/**
 * @author Ton Nguyen
 *
 */
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
