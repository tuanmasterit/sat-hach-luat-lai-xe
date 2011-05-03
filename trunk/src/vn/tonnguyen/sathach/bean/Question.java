package vn.tonnguyen.sathach.bean;

public class Question {
	private String pictureName;
	private int numberOfAnswers;
	private int answer;

	public Question(String pictureName, int numberOfAnswers, int answer) {
		this.pictureName = pictureName;
		this.numberOfAnswers = numberOfAnswers;
		this.answer = answer;
	}

	public String getPictureName() {
		return pictureName;
	}

	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
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

	@Override
	public String toString() {
		return String.format("Question %s, number of answer : %d, answer: %d",
				getPictureName(), getNumberOfAnswers(), getAnswer());
	}
}
