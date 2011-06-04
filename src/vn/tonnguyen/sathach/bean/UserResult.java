package vn.tonnguyen.sathach.bean;

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
