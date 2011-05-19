package vn.tonnguyen.sathach.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class Level implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8374353737559473101L;
	
	private int id;
	private String name;
	private String dataFilePath;
	private long examTime;
	private int passPoint; // the minimum number of correct question need to be passed the exam
	private ArrayList<ExamFormat> examsFormat;
	
	public Level() {
		
	}
	
	public Level(int id, String name, String dataFilePath, ArrayList<ExamFormat> examsFormat, int passPoint, long examTime) {
		this.id = id;
		this.name = name;
		this.dataFilePath = dataFilePath;
		this.examsFormat = examsFormat;
		this.passPoint = passPoint;
		this.examTime = examTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataFilePath() {
		return dataFilePath;
	}

	public void setDataFilePath(String dataFilePath) {
		this.dataFilePath = dataFilePath;
	}

	public ArrayList<ExamFormat> getExamsFormat() {
		return examsFormat;
	}

	public void setExamsFormat(ArrayList<ExamFormat> examsFormat) {
		this.examsFormat = examsFormat;
	}

	public long getExamTime() {
		return examTime;
	}

	public void setExamTime(long examTime) {
		this.examTime = examTime;
	}

	public int getPassPoint() {
		return passPoint;
	}

	public void setPassPoint(int passPoint) {
		this.passPoint = passPoint;
	}
}
