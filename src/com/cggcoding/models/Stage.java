package com.cggcoding.models;

import com.cggcoding.exceptions.DatabaseException;
import com.cggcoding.exceptions.ValidationException;
import com.cggcoding.factories.TaskFactory;
import com.cggcoding.utils.database.DatabaseActionHandler;
import com.cggcoding.utils.database.MySQLActionHandler;

import java.util.*;

public class Stage implements Completable {

	private int stageID;
	private int treatmentPlanID;
	private int userID;
	private String name;
	private String description;
	private int stageOrder; //the order of the stage within its treatment plan - if present decides the index it will be in the TreatmentPlan's List of Stages
	private List<Task> tasks;
	private List<Task> extraTasks; //for when user chooses to do more tasks than asked of - won't count toward progress meter but can be saved for review or other analysis (e.g. themes)
	private boolean completed;
	private double percentComplete;
	private List<StageGoal> goals;
	private boolean inProgress;//TODO implement inProgress - add logic to update it appropriately - dynamic or simple?
	private boolean isTemplate;
	private static DatabaseActionHandler databaseActionHandler = new MySQLActionHandler();

	private Stage (int stageID, int userID, String name, String description, int stageOrder){
		this.stageID = stageID;
		this.userID = userID;
		this.name = name;
		this.description = description;
		this.stageOrder = stageOrder;
		this.tasks = new ArrayList<>();
		this.extraTasks = new ArrayList<>();
		this.completed = false;
		this.percentComplete = 0;
		this.goals = new ArrayList<>();
		this.inProgress = false;
		this.isTemplate = false;
	}

	private Stage (int userID, String name, String description){
		this.userID = userID;
		this.stageID = 0;
		this.name = name;
		this.description = description;
		this.tasks = new ArrayList<>();
		this.extraTasks = new ArrayList<>();
		this.completed = false;
		this.percentComplete = 0;
		this.goals = new ArrayList<>();
		this.inProgress = false;
		this.isTemplate = false;
	}

	private Stage(int stageID, int treatmentPlanID, int userID, String name, String description, int stageOrder,
			List<Task> tasks, List<Task> extraTasks, boolean completed, double percentComplete, List<StageGoal> goals,
			boolean isTemplate) {
		this.stageID = stageID;
		this.treatmentPlanID = treatmentPlanID;
		this.userID = userID;
		this.name = name;
		this.description = description;
		this.stageOrder = stageOrder;
		this.tasks = tasks;
		this.extraTasks = extraTasks;
		this.completed = completed;
		this.percentComplete = percentComplete;
		this.goals = goals;
		//this.inProgress = inProgress;
		this.isTemplate = isTemplate;
	}

	public static Stage getInstance(int stageID, int treatmentPlanID, int userID, String name, String description, int stageOrder,
			List<Task> tasks, List<Task> extraTasks, boolean completed, double percentComplete, List<StageGoal> goals, boolean isTemplate){
		return new Stage(stageID, treatmentPlanID, userID, name, description, stageOrder, tasks, extraTasks, completed, percentComplete, goals, isTemplate);
	}
	
	public static Stage saveNewTemplateInDatabase(int userID, String name, String description) throws ValidationException, DatabaseException{
		return databaseActionHandler.stageTemplateValidateAndCreate(new Stage(userID, name, description));
	}

	//TODO delete this method after finishing transition to database
	public static Stage getInstanceAndCreateID(int userID, String name, String description, int stageOrder){
		int stageID = Math.abs(new Random().nextInt(10000));
		return new Stage(stageID, userID, name, description, stageOrder);
		
	}
	
	public static Stage getInstanceFromDatabase(int stageID) throws DatabaseException, ValidationException{
		return databaseActionHandler.stageLoad(stageID);
	}
	
	public void setStageID(int stageID) {
		this.stageID = stageID;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> taskList) {
		this.tasks = taskList;
	}

	public List<Task> getExtraTasks() {
		return extraTasks;
	}

	public void setExtraTasks(List<Task> extraTasks) {
		this.extraTasks = extraTasks;
	}

	public Task getTaskByID(int taskID){
		Task returnMe = null;
		for(Task task : tasks){
			if(task.getTaskID() == taskID){
				returnMe = task;
			}
		}

		return returnMe;
	}

	public String getTaskTypeNameByID(int taskID){
		Task returnMe = null;
		for(Task task : tasks){
			if(task.getTaskID() == taskID){
				returnMe = task;
			}
		}

		return returnMe.getTaskTypeName();
	}

	public int getStageID() {
		return stageID;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}

	public int getStageOrder() {
		return stageOrder;
	}

	//sets the order of the stage in the treatment plan if relevant
	public void setStageOrder(int stageOrder) {
		this.stageOrder = stageOrder;
	}

	public boolean isTemplate() {
		return isTemplate;
	}

	public void setTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}

	//Tasks will be displayed in the order in which they are in the List
	public void addTask(Task task){
		tasks.add(task);
	}
	
	public void addExtraTask(Task extraTask){
		extraTasks.add(extraTask);
	}

	public List<StageGoal> getGoals() {
		return goals;
	}

	public void setGoals(List<StageGoal> goals) {
		this.goals = goals;
	}

	@Override
	public boolean isCompleted(){
		return completed;
	}

	@Override
	public void markComplete() {
		completed = true;
	}

	@Override
	public void markIncomplete() {
		completed = false;
	}

	//returns a double digit number representing percentage of stage completion
	@Override
	public int getPercentComplete(){
		return (int)(percentComplete * 100);
	}

	public int getNumberOfTasksCompleted() {
		int numberOfTasksCompleted = 0;
		for(Task task : tasks){
			if(task.isCompleted()){
				numberOfTasksCompleted++;
			}
		}

		return numberOfTasksCompleted;
	}

	public int getTotalNumberOfTasks() {
		return tasks.size();
	}

	//when a task's completion state is changed it checks if all tasks are complete and if will lead to stage being complete and any other actions desired at this time
	public Stage updateTaskList(Map<Integer, Task> updatedTasksMap){
		//iterate through task map to update with info from updatedTasks list
		for(Task persistentTask : this.tasks){
			Task taskWithNewInfo = updatedTasksMap.get(persistentTask.getTaskID());
			persistentTask.updateData(taskWithNewInfo);
			//updateTaskData(persistentTask, taskWithNewInfo);
		}

		updateProgress();
		return this;
	}

	//TODO move this method to Task? so would be persistantTask.updateData(taskWithNewInfo) then would need an interface for Tasks with updateData() - each taskType would have it's own. Then maybe I don't need to do a switch statement, it would just know!?!?
	/*private Task updateTaskData(Task persistentTask, Task taskWithNewInfo){

		//update universal properties
		persistentTask.setCompleted(taskWithNewInfo.isCompleted());
		persistentTask.setDateCompleted(taskWithNewInfo.getDateCompleted());

		//update case-specific properties
		switch (persistentTask.getTaskTypeName()) {
			case "CognitiveTask" :
				CognitiveTask cogTask = (CognitiveTask)persistentTask;
				CognitiveTask newData = (CognitiveTask)taskWithNewInfo;

				cogTask.setAlternativeThought(newData.getAlternativeThought());
				cogTask.setAutomaticThought(newData.getAutomaticThought());

				//updateCogTask DB call goes here

				break;

		}

		return persistentTask;
	}*/

	// TODO - should progress update be called by the controller or handled all on the service side?
	//once a task is completed this is called to update the progress meter and associated metrics
	public void updateProgress(){
		
		percentComplete = ((double)getNumberOfTasksCompleted()/(double)getTotalNumberOfTasks());
		
		if(getPercentComplete()==100){
			this.markComplete();
		}
		
	}
	
	
	public List<Task> getIncompleteTasks(){
		List<Task> incompleteTasks = new ArrayList<>();

		for(Task task : tasks){
			if(!task.isCompleted()){
				incompleteTasks.add(task);
			}
		}
		return incompleteTasks;
	}

	public List<Task> getCompletedTasks(){
		List<Task> completeTasks = new ArrayList<>();

		for(Task task : tasks){
			if(task.isCompleted()){
				completeTasks.add(task);
			}
		}
		return completeTasks;
	}

	//TODO can I use Wildcards here to handle that I want to create Tasks without having to check which subclass type it is - I just want to create duplicates save for the id - or the Factory, Factory Method or Abstract Factory design patterns?
	//creates the new TaskSet and then makes the corresponding number of repetitions of the task and adds then to the Stage's tasks List
	public void addTaskSet(Task taskWithNoID, int repetitions){
		
		
		/*TODO REPLACE ALL THIS CODE SO WORKS WITH parentTaskID
		 * //factory method creates the TaskSet
		TaskSet taskSet = TaskSet.newInstance(this.stageID, repetitions);
		taskSetMap.put(taskSet.getTaskSetID(), taskSet);

		//set the taskSetID in the task
		taskWithNoID.setTaskSetID(taskSet.getTaskSetID());

		//now make the number of repetitions desired and add to the Stage's list of tasks
		tasks.addAll(TaskFactory.makeCopies(taskWithNoID, repetitions));
		*/

	}
	
	public void addGoal(StageGoal goal){
		this.goals.add(goal);
	}
	
	public StageGoal getGoalByID(int stageGoalID){
		for(StageGoal goal : goals){
			if(goal.getStageGoalID() == stageGoalID){
				return goal;
			}
		}
		
		return null;
	}

/*	removed in place of static factory method
 *  @Override
	public boolean saveNewInDatabase() throws ValidationException, DatabaseException {
		if(this.validateForDatabase()){
			databaseActionHandler.stageTemplateCreate(this);
			return true;
		}
		
		return false;
	}*/



	public void updateInDatabase()  throws ValidationException, DatabaseException {
		//if(this.validateForDatabase()){
			databaseActionHandler.stageTemplateUpdate(this);
		//}
		
	}

	public boolean deleteFromDatabase() throws ValidationException, DatabaseException  {
		// TODO implement method
		return false;
	}
/*
	@Override
	public boolean validateForDatabase() throws ValidationException, DatabaseException {
		return databaseActionHandler.stageValidateNewName(name, userID);
	}

	@Override
	public boolean loadDataFromDatabase() throws ValidationException, DatabaseException {
		return getInstanceFromDatabase(this.stageID) != null;
	}
*/
}
