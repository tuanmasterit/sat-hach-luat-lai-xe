package vn.tonnguyen.sathach.bean;

public class ExamFormat {
	private int numberOfQuestion;
	private int from;
	private int to;

	public ExamFormat(int numberOfQuestion, int from, int to) {
		this.numberOfQuestion = numberOfQuestion;
		this.from = from;
		this.to = to;
	}

	public int getNumberOfQuestion() {
		return numberOfQuestion;
	}

	public void setNumberOfQuestion(int numberOfQuestion) {
		this.numberOfQuestion = numberOfQuestion;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}
}
