package vn.tonnguyen.sathach.bean;

import java.util.ArrayList;

public class Level {
	private int id;
	private String name;
	private String dataFilePath;
	private ArrayList<ExamFormat> examsFormat;
	
	public Level() {
		
	}
	
	public Level(int id, String name, String dataFilePath, ArrayList<ExamFormat> examsFormat) {
		this.id = id;
		this.name = name;
		this.dataFilePath = dataFilePath;
		this.examsFormat = examsFormat;
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
}
