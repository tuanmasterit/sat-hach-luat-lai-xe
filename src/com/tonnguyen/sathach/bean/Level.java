/*
 * Copyright (C) 2011  Nguyen Hoang Ton, a.k.a Ton Nguyen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.tonnguyen.sathach.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Ton Nguyen
 *
 */
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
