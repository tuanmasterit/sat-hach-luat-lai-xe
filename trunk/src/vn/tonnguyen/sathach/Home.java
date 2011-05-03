package vn.tonnguyen.sathach;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import vn.tonnguyen.sathach.bean.ExamFormat;
import vn.tonnguyen.sathach.bean.Question;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class Home extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String[] dapan = new String[17];
		dapan[0] = "23331313231313133312132313221344444434232344243313";
		dapan[1] = "23231323134433443333444433444433241244333323441323";
		dapan[2] = "22134412232213222323442333234413134433332233331344";
		dapan[3] = "23233323333344333313331444122333131333124433443313";
		dapan[4] = "24444433444423334423232344331323243324142344341334";
		dapan[5] = "34442414142434441323132323232323442244333313331344";
		dapan[6] = "44124433333344134444332222232244334444444433444433";
		dapan[7] = "13232212442323131223143344331222233313224422441344";
		dapan[8] = "13441223121322124413132212332313342222131323333313";
		dapan[9] = "34143414443344133444242313231423333324132313233423";
		dapan[10] = "13332323124413443313233323232313232344133312231434";
		dapan[11] = "24121212341434333322132213233312334422122323222212";
		dapan[12] = "22122313233312233333231323233323233313333323442333";
		dapan[13] = "24144434122213232213122212132224133414231314342222";
		dapan[14] = "44341322122212244414223424122234333412332333133314";
		dapan[15] = "33443423132214122422333422333423332322223444142333";
		dapan[16] = "2414232434";

		ArrayList<ExamFormat> b1ExamFormat = new ArrayList<ExamFormat>();
		ArrayList<ExamFormat> notB1ExamFormat = new ArrayList<ExamFormat>();

		Hashtable<Integer, Question> questions = new Hashtable<Integer, Question>();
		for (int i = 1; i <= 405; i++) {
			String pictureName = String.format("%03d.JPG", i);
			int questionIndex = (i - 1) % 25;
			int questionList = (i - 1) / 25;

			int answer = Integer.parseInt(dapan[questionList].substring(
					2 * questionIndex, 2 * questionIndex + 1));
			int numberOfAnswer = Integer.parseInt(dapan[questionList]
					.substring(2 * questionIndex + 1, 2 * questionIndex + 2));
			Question question = new Question(pictureName, numberOfAnswer,
					answer);

			questions.put(i, question);
		}

		b1ExamFormat.add(new ExamFormat(2, 1, 21));
		b1ExamFormat.add(new ExamFormat(1, 121, 135));
		b1ExamFormat.add(new ExamFormat(3, 22, 62));
		b1ExamFormat.add(new ExamFormat(4, 63, 120));
		b1ExamFormat.add(new ExamFormat(1, 166, 175));
		b1ExamFormat.add(new ExamFormat(1, 176, 225));
		b1ExamFormat.add(new ExamFormat(5, 226, 275));
		b1ExamFormat.add(new ExamFormat(5, 276, 325));
		b1ExamFormat.add(new ExamFormat(4, 326, 375));
		b1ExamFormat.add(new ExamFormat(4, 376, 405));

		notB1ExamFormat.add(new ExamFormat(2, 1, 21));
		notB1ExamFormat.add(new ExamFormat(1, 121, 135));
		notB1ExamFormat.add(new ExamFormat(3, 22, 62));
		notB1ExamFormat.add(new ExamFormat(4, 63, 120));
		notB1ExamFormat.add(new ExamFormat(1, 136, 175));
		notB1ExamFormat.add(new ExamFormat(1, 176, 225));
		notB1ExamFormat.add(new ExamFormat(5, 226, 275));
		notB1ExamFormat.add(new ExamFormat(5, 276, 325));
		notB1ExamFormat.add(new ExamFormat(4, 326, 375));
		notB1ExamFormat.add(new ExamFormat(4, 376, 405));

		Hashtable<Integer, Question> examQuestions = new Hashtable<Integer, Question>();
		Random randomGenerator = new Random();
		int questionIndex;
		// generate B1 exam:
		for (ExamFormat examformat : b1ExamFormat) {
			// loop through NumberOfQuestion to get enough questions
			for (int i = 1; i <= examformat.getNumberOfQuestion(); i++) {
				// get random question index, and make sure it does not exist in
				// the list of questions
				do {
					questionIndex = getRandomInteger(examformat.getFrom(),
							examformat.getTo(), randomGenerator);
				} while (examQuestions.containsKey(questionIndex));
				// Now we have the question that was not existed in question
				// list
				examQuestions.put(questionIndex, questions.get(questionIndex));
			}
		}

		/* Using WebView to display the full-screen image */
		
		setContentView(R.layout.main);
		WebView full = (WebView)findViewById(R.id.webView);
		full.getSettings().setSupportZoom(true);       //Zoom Control on web (You don't need this
        //if ROM supports Multi-Touch     
		full.getSettings().setBuiltInZoomControls(true);
		//full.getSettings().setLoadWithOverviewMode(true);
		full.getSettings().setUseWideViewPort(true);
		full.setInitialScale(75);

		/* Create a new Html that contains the full-screen image */
		String html = ("<html><img src=\"017.jpg\"></html>");
		/* Finally, display the content using WebView */
		full.loadDataWithBaseURL("file:///" + MyApplication.APPLICATION_DATA_PATH, html, "text/html", "utf-8",
				"");
	}

	private static int getRandomInteger(int aStart, int aEnd, Random aRandom) {
		if (aStart > aEnd) {
			throw new IllegalArgumentException("Start cannot exceed End.");
		}
		// get the range, casting to long to avoid overflow problems
		long range = (long) aEnd - (long) aStart + 1;
		// compute a fraction of the range, 0 <= frac < range
		long fraction = (long) (range * aRandom.nextDouble());
		int randomNumber = (int) (fraction + aStart);
		return randomNumber;
	}
}