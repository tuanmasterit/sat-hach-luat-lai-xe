package vn.tonnguyen.sathach.bean;


public class QuestionReviewSession extends Session {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4829567263083730556L;
	private Question[] questions;
	private int currentQuestionIndex;
	private long totalTime;
	private int correctAnswer;
	
	public QuestionReviewSession() {
		
	}
	
	public QuestionReviewSession(Question[] questions, int currentQuestionIndex, long totalTime, int correctAnswer, Level selectedLevel) {
		this.questions = questions;
		this.currentQuestionIndex = currentQuestionIndex;
		this.totalTime = totalTime;
		this.correctAnswer = correctAnswer;
		this.selectedLevel = selectedLevel;
	}

	public Question[] getQuestions() {
		return questions;
	}

	public void setQuestions(Question[] questions) {
		this.questions = questions;
	}

	public int getCurrentQuestionIndex() {
		return currentQuestionIndex;
	}

	public void setCurrentQuestionIndex(int currentQuestionIndex) {
		this.currentQuestionIndex = currentQuestionIndex;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public int getCorrectAnswer() {
		return correctAnswer;
	}

	public void setCorrectAnswer(int correctAnswer) {
		this.correctAnswer = correctAnswer;
	}
	
	public boolean isPasses() {
		return correctAnswer >= selectedLevel.getPassPoint();
	}
}
