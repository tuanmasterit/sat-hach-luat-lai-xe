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

	private static final FrameLayout.LayoutParams ZOOM_PARAMS = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);

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

//		String text = "";
//		for (int i : examQuestions.keySet()) {
//			text += (examQuestions.get(i).toString() + "<br />");
//		}

		//TextView tv = new TextView(this);
		//tv.setText(Html.fromHtml(text));
		// setContentView(tv);

		// setContentView(R.layout.main);

		/* Using WebView to display the full-screen image */
		download();
		
		setContentView(R.layout.main);
		WebView full = (WebView)findViewById(R.id.webView);
//		FrameLayout mContentView = (FrameLayout) getWindow().getDecorView()
//				.findViewById(android.R.id.content);
//		final View zoom = full.getZoomControls();
//		mContentView.addView(zoom, ZOOM_PARAMS);
//		zoom.setVisibility(View.GONE);
		full.getSettings().setSupportZoom(true);       //Zoom Control on web (You don't need this
        //if ROM supports Multi-Touch     
		full.getSettings().setBuiltInZoomControls(true);
		//full.getSettings().setLoadWithOverviewMode(true);
		full.getSettings().setUseWideViewPort(true);
		full.setInitialScale(75);

		/* Create a new Html that contains the full-screen image */
		String html = ("<html><img src=\"323.jpg\"></html>");
		/* Finally, display the content using WebView */
		full.loadDataWithBaseURL("file:///sdcard/", html, "text/html", "utf-8",
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

	private void download() {
		try {
			// set the download URL, a url that points to a file on the internet
			// this is the file to be downloaded
			URL url = new URL(
					"http://5h0b154lyna.googlecode.com/files/323.JPG");

			// create the new connection
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();

			// set up some things on the connection
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);

			// and connect!
			urlConnection.connect();
		
			File dataFolder = new File(Environment.getExternalStorageDirectory() + "/data/vn.tonnguyen.sathach/");
			// have the object build the directory structure, if needed.
			dataFolder.mkdirs();
			
			Log.v("Saving location", dataFolder.getAbsolutePath());
			// create a File object for the output file
			File outputFile = new File(dataFolder, "323.jpg");
			if(outputFile.exists()) {
				outputFile.delete();
			}
			// this will be used to write the downloaded data into the file we
			// created
			FileOutputStream fileOutput = new FileOutputStream(outputFile);

			// this will be used in reading the data from the internet
			InputStream inputStream = urlConnection.getInputStream();

			// this is the total size of the file
			int totalSize = urlConnection.getContentLength();
			// variable to store total downloaded bytes
			int downloadedSize = 0;

			// create a buffer...
			byte[] buffer = new byte[1024];
			int bufferLength = 0; // used to store a temporary size of the
									// buffer

			// now, read through the input buffer and write the contents to the
			// file
			while ((bufferLength = inputStream.read(buffer)) > 0) {
				// add the data in the buffer to the file in the file output
				// stream (the file on the sd card
				fileOutput.write(buffer, 0, bufferLength);
				// add up the size so we know how much is downloaded
				downloadedSize += bufferLength;
				// this is where you would do something to report the prgress,
				// like this maybe
				// updateProgress(downloadedSize, totalSize);

			}
			// close the output stream when done
			fileOutput.close();

			// catch some possible errors...
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}