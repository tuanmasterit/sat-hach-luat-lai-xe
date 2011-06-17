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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tonnguyen.sathach.bean.QuestionReviewSession;

/**
 * @author Ton Nguyen
 *
 */
public class ResultActivity extends BaseActivity {
	private QuestionReviewSession session;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		Bundle extras = getIntent().getExtras();
		if(extras != null)
		{
			session = (QuestionReviewSession)extras.getSerializable(PARAM_KEY);
			TextView h1 = ((TextView)findViewById(R.id.result_h1));
			TextView h2 = ((TextView)findViewById(R.id.result_h2));
			TextView h3 = ((TextView)findViewById(R.id.result_h3));
			TextView h4 = ((TextView)findViewById(R.id.result_h4));
			String h1Text = getResources().getString(R.string.result_h1).replace("{0}", String.valueOf(session.getCorrectAnswer()))
																		.replace("{1}", String.valueOf(session.getQuestions().length));
			String h2Text = getResources().getString(R.string.result_h2).replace("{0}", String.valueOf(session.getSelectedLevel().getPassPoint()));
			String h3Text = getResources().getString(R.string.result_h3).replace("{0}", session.isPasses() ? getResources().getString(R.string.result_passed1) 
																										: getResources().getString(R.string.result_failed1));
			String h4Text = getResources().getString(R.string.result_h4).replace("{0}", getTimeAsString(session.getTotalTime()));
			
			h1.setText(h1Text);
			h2.setText(h2Text);
			h3.setText(h3Text);
			h4.setText(h4Text);
		}
		
		initAdMob();
		((Button)findViewById(R.id.result_show_review_screen)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				Intent data = new Intent();
				data.putExtra(PARAM_KEY, session);
				setResult(RESULT_OK, data); // display questions review screen, please
				finish();
			}
		});
		((Button)findViewById(R.id.result_exit)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				setResult(RESULT_CANCELED); // i dont want to view questions review screen
				finish();
			}
		});
	}
}
