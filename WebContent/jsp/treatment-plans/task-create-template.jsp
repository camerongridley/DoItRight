
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>


<c:import url="/jsp/header.jsp" />

<div class="page-header">
	<h1>Create A Task</h1>
	<h2>Subheading</h2>
</div>
  
<c:import url="/jsp/message-modal.jsp"/>
	
<form class="form-horizontal" action="./CreateTaskTemplate" method="POST">
		<input type="hidden" name="requestedAction" value="taskInfo">
		<input type="hidden" name="creatingTemplate" value="${creatingTemplate }">
		<input type="hidden" name="taskID" value="${task.taskID }">
		<input type="hidden" name="stageID" value="${task.stageID }">
		<input type="hidden" name="parentTaskID" value="${task.parentTaskID }">
		<input type="hidden" name="isTemplate" value="${task.template }">
		<input type="hidden" name="isExtraTask" value="${task.extraTask }">
		
		<div class="form-group">
            <label for="taskTypeID" class="col-sm-2 control-label">Task Type</label>
            <div class="col-sm-10">
                <select class="form-control" id="taskTypeID" name="taskTypeID">
                    <option  value="">Select a default treatment issue.</option>
                    <c:forEach items="${taskTypeMap}" var="taskType">
                        <option value="${taskType.key}">${taskType.value}</option>
                    </c:forEach>
                </select>
            </div>
        </div>		
        <div class="form-group">
            <label for="taskTitle" class="col-sm-2 control-label">Task Name</label>
            <div class="col-sm-10">
                <input type="text" class="form-control" id="taskTitle" name="taskTitle" value="${task.title }" placeholder="Enter a task name here.">
            </div>
        </div>
        <div class="form-group">
            <label for="taskInstructions" class="col-sm-2 control-label">Instructions</label>
            <div class="col-sm-10">
                <input type="text" class="form-control" id="taskInstructions" name="taskInstructions" value="${task.instructions }" placeholder="Describe the stage.">
            </div>
        </div>
		<div class="form-group">
            <label for="resourceLink" class="col-sm-2 control-label">Resource Link</label>
            <div class="col-sm-10">
                <input type="text" class="form-control" id="resourceLink" name="resourceLink" value="${task.resourceLink }" placeholder="Add a link to related resources for this task.">
            </div>
        </div>
        <div class="form-group">
		  <div class="col-sm-offset-2 col-sm-10">
		    <div class="checkbox">
		      <label>
		        <input type="checkbox" id="hasSubtasks" name="hasSubtasks" <c:if test="${hasSubtasks.equals('on')}">checked</c:if>> Does this task have subtasks?
		        
		      </label>
		    </div>
		  </div>
		</div>
        
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" class="btn btn-default">Submit</button>
            </div>
        </div>
    </form>
	

<c:import url="/jsp/footer.jsp" />