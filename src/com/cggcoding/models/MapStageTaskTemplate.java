package com.cggcoding.models;

import java.sql.Connection;
import java.sql.SQLException;

import com.cggcoding.utils.database.DatabaseActionHandler;
import com.cggcoding.utils.database.MySQLActionHandler;

/**This class is always to be accessed through a Stage object since it is an extension of a Stage's functions
 * @author cgrid
 *
 */
public class MapStageTaskTemplate {

	private int stageID;
	private int taskID;
	private int templateTaskOrder;
	private int templateTaskRepetitions;
	
	private static DatabaseActionHandler dao = new MySQLActionHandler();
	
	public MapStageTaskTemplate(){
		this.stageID = 0;
		this.taskID = 0;
		this.templateTaskOrder = 0;
		this.templateTaskRepetitions = 1;
	}
	
	public MapStageTaskTemplate(int stageID, int taskID, int templateTaskOrder, int templateTaskRepetitions) {
		this.stageID = stageID;
		this.taskID = taskID;
		this.templateTaskOrder = templateTaskOrder;
		this.templateTaskRepetitions = templateTaskRepetitions;
	}


	public int getStageID() {
		return stageID;
	}


	public void setStageID(int stageID) {
		this.stageID = stageID;
	}


	public int getTaskID() {
		return taskID;
	}


	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}


	public int getTemplateTaskOrder() {
		return templateTaskOrder;
	}


	public void setTemplateTaskOrder(int templateTaskOrder) {
		this.templateTaskOrder = templateTaskOrder;
	}

	/**Since templateTaskOrder is based off List indexes, it starts with 0.  So for displaying the order to users on the front end, add 1 so
	 *the order values start with 1.
	 * @return
	 */
	public int getTemplateTaskOrderForUserDisplay(){
		return templateTaskOrder + 1;
	}

	public int getTemplateTaskRepetitions() {
		return templateTaskRepetitions;
	}

	public void setTemplateTaskRepetitions(int templateTaskRepetitions) {
		this.templateTaskRepetitions = templateTaskRepetitions;
	}
	
	protected void update(Connection cn) throws SQLException{
		dao.mapStageTaskTemplateUpdate(cn, this);
	}
	
	protected void create(Connection cn) throws SQLException{
		dao.mapStageTaskTemplateCreate(cn, this);
	}
	
	protected static void delete(Connection cn, int taskID, int stageID) throws SQLException{
		dao.mapStageTaskTemplateDelete(cn, taskID, stageID);
	}
	

}
