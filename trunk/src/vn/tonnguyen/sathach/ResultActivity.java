package vn.tonnguyen.sathach;

import vn.tonnguyen.sathach.bean.QuestionReviewSession;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends BaseActivity {
	private QuestionReviewSession session;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		final MyApplication context = (MyApplication)getApplicationContext();
		//if(savedInstanceState == null) { // load at firt time, not resume
			Bundle extras = getIntent().getExtras();
			if(extras != null)
			{
				session = (QuestionReviewSession)extras.getSerializable(QuestionReviewActivity.PARAM_KEY);
				TextView h1 = ((TextView)findViewById(R.id.result_h1));
				TextView h2 = ((TextView)findViewById(R.id.result_h2));
				TextView h3 = ((TextView)findViewById(R.id.result_h3));
				String h1Text = getResources().getString(R.string.result_h1).replace("{0}", String.valueOf(session.getCorrectAnswer()))
																			.replace("{1}", String.valueOf(session.getQuestions().length));
				String h2Text = getResources().getString(R.string.result_h2).replace("{0}", String.valueOf(session.getSelectedLevel().getPassPoint()));
				String h3Text = getResources().getString(R.string.result_h3).replace("{0}", session.isPasses() ? getResources().getString(R.string.result_passed1) 
																											: getResources().getString(R.string.result_failed1));
				
				h1.setText(h1Text);
				h2.setText(h2Text);
				h3.setText(h3Text);
			}
		//}
		
		initAdMob();
		((Button)findViewById(R.id.result_show_review_screen)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.vibrateIfEnabled();
				Intent data = new Intent();
				data.putExtra(QuestionReviewActivity.PARAM_KEY, session);
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
