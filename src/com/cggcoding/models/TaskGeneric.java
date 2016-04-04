package com.cggcoding.models;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.cggcoding.exceptions.DatabaseException;
import com.cggcoding.exceptions.ValidationException;
import com.cggcoding.utils.Constants;
import com.cggcoding.utils.database.DatabaseActionHandler;
import com.cggcoding.utils.database.MySQLActionHandler;

public class TaskGeneric extends Task implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static DatabaseActionHandler dao= new MySQLActionHandler();
	
	/**
	 * Default constructor.  Designated public so Task.getTaskType() can be accessed outside of the package.
	 */
	public TaskGeneric(){
		super();
	}
	
	private TaskGeneric(int taskID, int userID) {
		super(taskID, userID);
	}

	//constructor without taskID - for Tasks objects that haven't been saved to database and don't have a taskID yet
	protected TaskGeneric(int stageID, int userID, int taskTypeID, int parentTaskID, String title, String instructions, String resourceLink,
			int clientTaskOrder, boolean extraTask, boolean template, int templateID, int clientRepetition) {
		//super(userID, taskTypeID, parentTaskID, title, instructions, resourceLink, extraTask, template);
		super(stageID, userID, taskTypeID, parentTaskID, title, instructions, resourceLink, clientTaskOrder, extraTask, template, templateID, clientRepetition);
	}
	
	//full constructor
	private TaskGeneric(int taskID, int stageID, int userID, int taskTypeID, int parentTaskID, String title, String instructions, String resourceLink,
			boolean completed, LocalDateTime dateCompleted, int clientTaskOrder, boolean extraTask, boolean template, int templateID, int clientRepetition, Map<Integer, Keyword> keywords) {
		//super(userID, taskTypeID, parentTaskID, title, instructions, resourceLink, extraTask, template);
		super(taskID, stageID, userID, taskTypeID, parentTaskID, title, instructions, resourceLink, completed, dateCompleted, clientTaskOrder, extraTask, template, templateID, clientRepetition, keywords);
	}
	
	//Static Factory Methods
	public static TaskGeneric getInstanceBareBones(int taskID, int userID){
		return new TaskGeneric(taskID, userID);
	}
	
	public static TaskGeneric getInstanceWithoutTaskID(int stageID, int userID, int taskTypeID, int parentTaskID, String title, String instructions, String resourceLink,
			int clientTaskOrder, boolean extraTask, boolean template, int templateID, int clientRepetition){
		return new TaskGeneric(stageID, userID, taskTypeID, parentTaskID, title, instructions, resourceLink, clientTaskOrder, extraTask, template, templateID, clientRepetition);
	}
	
	public static TaskGeneric getInstanceFull(int taskID, int stageID, int userID, int taskTypeID, int parentTaskID, String title, String instructions, String resourceLink,
			boolean completed, LocalDateTime dateCompleted, int clientTaskOrder, boolean extraTask, boolean template, int templateID, int clientRepetition, Map<Integer, Keyword> keywords){
		return new TaskGeneric(taskID, stageID, userID, taskTypeID, parentTaskID, title, instructions, resourceLink, completed, dateCompleted, clientTaskOrder, extraTask, template, templateID, clientRepetition, keywords);
	}	
	
	public static TaskGeneric convertToGeneric(Task task){
		return getInstanceFull(task.getTaskID(), task.getStageID(), task.getUserID(), Constants.TASK_TYPE_ID_GENERIC_TASK, task.getParentTaskID(), task.getTitle(), task.getInstructions(), 
				task.getResourceLink(), task.isCompleted(), task.getDateCompleted(), task.getClientTaskOrder(), task.isExtraTask(), task.isTemplate(), task.getTemplateID(), 
				task.getClientRepetition(), task.getKeywords());
	}
	

	/**This class is a concretized version of Task to map up with the GenericTask table in the database.  There is no "Task" table in the database
	 * due to the database design choice. The factory method saves a GerericTask to the database. Since this is to create a template, which is 
	 * therefore independent of any stage or treatment plan, parameters for stageID, parentTaskID and clientTaskOrder are set to 0.  Additionally other
	 * parameters receive defaults: taskID = 0 since it has not been generated by the database, completed = false, dateCompleted = null, template=true, templateID=0, clientRepetition=1
	 * @param taskID
	 * @param userID
	 * @param title
	 * @param instructions
	 * @throws DatabaseException
	 * @throws ValidationException
	 */
	public static TaskGeneric getTemplateInstance(int userID, int taskTypeID, String title, String instructions, 
			String resourceLink, boolean extraTask, Map<Integer, Keyword> keywords){
		return new TaskGeneric(0, 0, userID, taskTypeID, 0, 
				title, instructions, resourceLink, false, null, 0, extraTask, true, 0, 1, keywords);
	}
	
	protected static TaskGeneric loadGeneric(Connection cn, int taskID) throws SQLException{
		return (TaskGeneric)dao.taskGenericLoad(cn, taskID);
	}
	
	@Override
	protected boolean updateAdditionalData (Connection cn) throws SQLException {
		return true;//there is no additional data in GenericTask to update
	}

	@Override
	protected void loadAdditionalData(Connection cn, TaskGeneric genericTask) {
		//there is no additional data to load for GenericTask
	}

	@Override
	protected void createAdditionalData(Connection cn) throws ValidationException {
		//there is no additional data to save for GenericTask
		
	}

	@Override
	protected void deleteAdditionalData(Connection cn) throws ValidationException, SQLException {
		// nothing to do here for TaskGeneric	
	}

	@Override
	public Task copy(){
		TaskGeneric gTask = getInstanceFull(0, getStageID(), getUserID(), getTaskTypeID(), getParentTaskID(), getTitle(), getInstructions(), getResourceLink(), 
					isCompleted(), getDateCompleted(), getClientTaskOrder(), isExtraTask(), false, getTemplateID(), getClientRepetition(), getKeywords());
		
		return gTask;

	}
	
	@Override
	public Task copyAndSave(int stageID, int userID)throws DatabaseException, ValidationException {
		TaskGeneric copy = (TaskGeneric) copy();
		copy.setStageID(stageID);
		copy.setUserID(userID);
		
		return copy.create();
	}

	@Override
	public void transferAdditionalData(Task taskWithNewData) {
		//there is no additional data with this task
		
	}

	
	
}
