package com.cggcoding.models.tasktypes;

import java.time.LocalDateTime;
import java.util.List;

import com.cggcoding.exceptions.DatabaseException;
import com.cggcoding.exceptions.ValidationException;
import com.cggcoding.models.DatabaseModel;
import com.cggcoding.models.Task;
import com.cggcoding.utils.database.DatabaseActionHandler;
import com.cggcoding.utils.database.MySQLActionHandler;

public class TwoTextBoxesTask extends Task implements DatabaseModel{
	private String extraTextLabel1;
	private String extraTextValue1;
	private String extraTextLabel2;
	private String extraTextValue2;
	
	private static DatabaseActionHandler databaseActionHandler = new MySQLActionHandler();
	

	private TwoTextBoxesTask(int taskID, int stageID, int userID, int taskTypeID, int parentTaskID, String title,
			String instructions, String resourceLink, boolean completed, LocalDateTime dateCompleted, int taskOrder,
			boolean extraTask, boolean template,
			String extraTextLabel1, String extraTextValue1,
			String extraTextLabel2, String extraTextValue2) {
		super(taskID, stageID, userID, taskTypeID, parentTaskID, title, instructions, resourceLink, completed,
				dateCompleted, taskOrder, extraTask, template);
		this.extraTextLabel1 = extraTextLabel1;
		this.extraTextValue1 = extraTextValue1;
		this.extraTextLabel2 = extraTextLabel2;
		this.extraTextValue2 = extraTextValue2;
	}

	public static TwoTextBoxesTask getInstanceFull(int taskID, int stageID, int userID, int taskTypeID, int parentTaskID, String title,
			String instructions, String resourceLink, boolean completed, LocalDateTime dateCompleted, int taskOrder,
			boolean extraTask, boolean template,
			String extraTextLabel1, String extraTextValue1,
			String extraTextLabel2, String extraTextValue2){
		
		return new TwoTextBoxesTask(taskID, stageID, userID, taskTypeID, parentTaskID, title,
			instructions, resourceLink, completed, dateCompleted, taskOrder,
			extraTask, template,
			extraTextLabel1, extraTextValue1,
			extraTextLabel2, extraTextValue2);
	}
	
	public static TwoTextBoxesTask addDataToGenericTask(GenericTask genericTask, String extraTextLabel1, String extraTextValue1,String extraTextLabel2, String extraTextValue2){
		return new TwoTextBoxesTask(genericTask.getTaskID(), genericTask.getStageID(), genericTask.getUserID(), genericTask.getTaskTypeID(), genericTask.getParentTaskID(), genericTask.getTitle(),
				genericTask.getInstructions(), genericTask.getResourceLink(), genericTask.isCompleted(), genericTask.getDateCompleted(), genericTask.getTaskOrder(),
				genericTask.isExtraTask(), genericTask.isTemplate(),
				extraTextLabel1, extraTextValue1,
				extraTextLabel2, extraTextValue2);
	}
	
	public String getExtraTextLabel1() {
		return extraTextLabel1;
	}

	public void setExtraTextLabel1(String extraTextLabel1) {
		this.extraTextLabel1 = extraTextLabel1;
	}

	public String getExtraTextValue1() {
		return extraTextValue1;
	}

	public void setExtraTextValue1(String extraTextValue1) {
		this.extraTextValue1 = extraTextValue1;
	}

	public String getExtraTextLabel2() {
		return extraTextLabel2;
	}

	public void setExtraTextLabel2(String extraTextLabel2) {
		this.extraTextLabel2 = extraTextLabel2;
	}

	public String getExtraTextValue2() {
		return extraTextValue2;
	}

	public void setExtraTextValue2(String extraTextValue2) {
		this.extraTextValue2 = extraTextValue2;
	}

	public static Task load(int taskID) throws DatabaseException {
		return databaseActionHandler.taskTwoTextBoxesLoad(taskID);
	}
	
	@Override
	protected void saveNewAdditionalData() throws DatabaseException, ValidationException{
		databaseActionHandler.taskTwoTextBoxesSaveNewAdditionalData(this);
	}
	
	@Override
	protected boolean updateAdditionalData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void loadAdditionalData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveNew() throws ValidationException, DatabaseException {
		super.saveNewGeneralDataInDatabase();
		saveNewAdditionalData();
		
	}

	@Override
	public void update() throws ValidationException, DatabaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete() throws ValidationException, DatabaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Object> copy(Object o, int numberOfCopies) {
		// TODO Auto-generated method stub
		return null;
	}

}
