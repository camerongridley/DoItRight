package com.cggcoding.models.tasktypes;

import com.cggcoding.exceptions.DatabaseException;
import com.cggcoding.exceptions.ValidationException;
import com.cggcoding.models.DatabaseModel;
import com.cggcoding.models.Task;

import java.util.List;
import java.util.Random;

public class CognitiveTask extends Task implements DatabaseModel{
	private String automaticThought;
	private String alternativeThought;
	private int preSUDS;
	private int postSUDS;

	
	public CognitiveTask(int taskID, int userID) {
		super(taskID, userID);
	}

	public CognitiveTask (int taskID, int userID, String title, String instructions){
		super(taskID, userID, title, instructions);
	}

	public CognitiveTask (int taskID, int userID, int parentTaskID, String title, String instructions){
		super(taskID, userID, parentTaskID, title, instructions);
	}
	
	public CognitiveTask (int taskID, int userID, int parentTaskID, String title, String instructions, String automaticThought, String alternativeThought, int preSUDS, int postSUDS){
		super(taskID, userID, parentTaskID, title, instructions);
		this.automaticThought = automaticThought;
		this.alternativeThought = alternativeThought;
		this.preSUDS = preSUDS;
		this.postSUDS = postSUDS;
	}

	//static factory method
	/*public static CognitiveTask newInstance(int taskSetID, String title, String description){
		//this.taskID = TODO replace Random with call to database to create the task and get the autogenerated taskID
		int taskID = Math.abs(new Random(10000).nextInt());

		return new CognitiveTask(taskID, taskSetID, title, description);
	}*/

	public static CognitiveTask convertFromGenericTask(GenericTask genericTask, String automaticThought, String alternativeThought, int preSUDS, int postSUDS){
		return new CognitiveTask(genericTask.getTaskID(), genericTask.getUserID(), genericTask.getParentTaskID(), genericTask.getTitle(), genericTask.getInstructions(), automaticThought, alternativeThought, preSUDS, postSUDS);
	}
	
	public String getAutomaticThought() {
		return automaticThought;
	}
	public void setAutomaticThought(String automaticThought) {
		this.automaticThought = automaticThought;
	}
	public String getAlternativeThought() {
		return alternativeThought;
	}
	public void setAlternativeThought(String alternativeThought) {
		this.alternativeThought = alternativeThought;
	}
	public int getPreSUDS() {
		return preSUDS;
	}
	public void setPreSUDS(int preSUDS) {
		this.preSUDS = preSUDS;
	}
	public int getPostSUDS() {
		return postSUDS;
	}
	public void setPostSUDS(int postSUDS) {
		this.postSUDS = postSUDS;
	}


	//TODO DELETE method after done with db integration - move to update()?
	public boolean updateData(Task taskWithNewData) {
		//update universal properties
		this.setCompleted(taskWithNewData.isCompleted());
		this.setDateCompleted(taskWithNewData.getDateCompleted());


		//CognitiveTask cogTask = (CognitiveTask)persistentTask;
		CognitiveTask newData = (CognitiveTask)taskWithNewData;

		this.setAlternativeThought(newData.getAlternativeThought());
		this.setAutomaticThought(newData.getAutomaticThought());

		//updateCogTask DB call goes here

		return true;//TODO returns true if DB update was success
	}


	@Override
	protected boolean updateAdditionalData() {
		// TODO implement method
		return false;
	}

	@Override
	public Object saveNew() throws ValidationException, DatabaseException {
		// TODO implement method
		return null;
		
	}

	@Override
	public void update() throws ValidationException, DatabaseException {
		// TODO implement method
		
	}

	@Override
	public void delete() throws ValidationException, DatabaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Object> copy(int numberOfCopies) {
		// TODO implement method
		return null;
	}

	@Override
	protected void loadAdditionalData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveNewAdditionalData() throws DatabaseException, ValidationException {
		// TODO Auto-generated method stub
		
	}

}
