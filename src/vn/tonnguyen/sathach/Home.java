package vn.tonnguyen.sathach;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Home extends Activity {
	private Button newExamButton;
	private Button exitButton;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home);

		// Capture our button from layout
		newExamButton = (Button)findViewById(R.id.button_new_game);
	    // Register the onClick listener with the implementation above
		newExamButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent((MyApplication)getApplicationContext(), ExamScreen.class));
			}
		});

		exitButton = (Button)findViewById(R.id.button_exit);
	    // Register the onClick listener with the implementation above
		exitButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// exit
				finish();
			}
		});
	}
}