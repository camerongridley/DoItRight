package com.cggcoding.models.tasktypes;

import com.cggcoding.models.Task;
import com.cggcoding.models.Updateable;

import java.util.Random;

public class PsychEdTask extends Task implements Updateable{

	public PsychEdTask (int taskID) {
		super(taskID);
	}


	public PsychEdTask(int taskID, int userID, String title, String instructions) {
		super(taskID, userID, title, instructions);
	}

	public PsychEdTask(int taskID, int userID, int taskSetID, String title, String instructions) {
		super(taskID, userID, taskSetID, title, instructions);
	}

	//static factory method
	public static PsychEdTask newInstance(int taskSetID, String name, String instructions){
		//this.taskID = TODO replace Random with call to database to create the task and get the autogenerated taskID
		int taskID = Math.abs(new Random(10000).nextInt());

		return new PsychEdTask(taskID, taskSetID, name, instructions);
	}

	@Override
	public boolean updateData(Task taskWithNewData) {
		//update universal properties
		this.setCompleted(taskWithNewData.isCompleted());
		this.setDateCompleted(taskWithNewData.getDateCompleted());

		//updateCogTask DB call goes here

		return true;//TODO returns true if DB update was success
	}
}
