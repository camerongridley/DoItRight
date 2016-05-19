
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>


<c:import url="/WEB-INF/jsp/header.jsp" />

	
<div class="page-header">
    <h2><span class="glyphicon glyphicon-dashboard panel-icon" aria-hidden="true"></span>Main Menu</h2>
  </div>
  <c:import url="/WEB-INF/jsp/message-modal.jsp"/>
  
  <div class="alert alert-info" role="alert">
	  <span class="glyphicon glyphicon-asterisk" aria-hidden="true"></span>
	  <span class="sr-only"></span>
	  <strong>Inspiration Of The Day: </strong>${user.affirmation.affirmation }
		<button type="button" class="btn btn-xs btn-default" aria-label="Left Align" data-toggle="modal" data-target="#newAffirmation" 
		title="Add a new inspiration.">
			<span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
		</button>
	</div>
  
  <div class="panel panel-primary">
	  <div class="panel-heading" title="Your Stats and Achievements">
	    <h3 class="panel-title">Your Stats and Achievements</h3>
	  </div>
	  <div class="panel-body">
	  		<div class="alert alert-success" role="alert">
			  <c:if test="${user.activityStreak>0}"><span class="glyphicon glyphicon-thumbs-up" aria-hidden="true"></span></c:if>
			  <span class="sr-only">Activity Streak:</span>
			  <strong>Current Activity Streak: </strong>${user.activityStreak} - ${user.activityStreakMessage } 
			</div>
			
			<div class="alert alert-success" role="alert">
			  <c:if test="${user.loginStreak>1}"><span class="glyphicon glyphicon-thumbs-up" aria-hidden="true"></span></c:if>
			  <span class="sr-only">Login Streak:</span>
			  <strong>Current Login Streak: </strong>${user.loginStreak} ${user.loginStreakMessage }
			</div>
	  </div>
  </div>
  
  
  <c:set var="activePlan" value="${user.getActiveTreatmentPlan() }"></c:set>
  <c:if test="${activePlan != null }">
  <div class="panel panel-primary">
	  <div class="panel-heading" title="Continue Where You Left Off">
	    <form class="form-inline form-inline-controls" action="/secure/ClientSelectPlan" method="POST">
	      <div>
	      <h3 class="panel-title">Continue Working On Your Treatment Plan
	      <button type="submit" class="btn btn-info btn-sm">Manage Plans</button>
	      </h3>
	      <input type="hidden" name="path" value="clientManagePlans">
	      <input type="hidden" name="requestedAction" value="select-plan-start">
	      </div>
	      
	    </form>
	  </div>
	  <div class="panel-body">
	  		<div class="panel panel-default">
			  <div class="panel-heading" title="${activePlan.title}">
			    <h3 class="panel-title">${activePlan.title}</h3>
			  </div>
			  <div class="panel-body">
			    <div class="progress" title="This plan is ${activePlan.percentComplete()}% complete.">
				  <div class="progress-bar progress-bar-success" role="progressbar" aria-valuenow="${activePlan.percentComplete()}" aria-valuemin="0" aria-valuemax="100" style="width: ${activePlan.percentComplete()}%;">
				    ${activePlan.percentComplete()}%
				  </div>
				</div>
				<p><strong>Current Stage: </strong>${activePlan.currentStage.title } (${activePlan.currentStage.percentComplete}%)</p>
				
				<form class="form-horizontal form-inline-controls" action="/secure/ClientSelectPlan" method="POST">
					<input type="hidden" name="requestedAction" value="select-plan-load">
					<input type="hidden" name="path" value="${path }">
					<input type="hidden" name="initialize" value="no">
					<input type="hidden" name="clientUUID" value="${clientUUID}">
					<input type="hidden" name="treatmentPlanID" value="${activePlan.treatmentPlanID}">
					
					<button type="submit" class="btn btn-lg btn-default" aria-label="Left Align" title="Continue working on this this treatment plan.">
					  <span class="glyphicon glyphicon-play" aria-hidden="true"></span>
					</button>
				</form>
				
			  </div>
			</div>
	  </div>
  </div>
  
  	
  </c:if>
  
  <c:if test="${activePlan == null }">
  	<h3>You do not have any plans that are active.  Please visit your Manage Treatment Plans page to start a working on a plan.</h3>
  </c:if>
  
 <!--  
  
  <p>
    <form class="form-inline" action="/secure/ClientSelectPlan" method="POST">
      <div><button type="submit" class="btn btn-primary">Manage Your Treatment Plans</button></div>
      <input type="hidden" name="path" value="clientManagePlans">
      <input type="hidden" name="requestedAction" value="select-plan-start">
    </form>
  </p>

  <p>
    <form class="form-inline" action="" method="POST">
      <div><button type="submit" class="btn btn-primary" disabled>Another Option</button></div>
      <input type="hidden" name="requestedAction" value="assign-treatment-plan-start">
      <input type="hidden" name="path" value="assignClientTreatmentPlan">
    </form>
  </p>

 -->
 
 <!-- New Affirmation -->
<div class="modal fade" id="newAffirmation" tabindex="-1"
	role="dialog" aria-labelledby="newAffirmationModalLabel">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<form class="form-horizontal" action="/secure/AffirmationManagement" method="POST">
				<input type="hidden" name="requestedAction" value="create-new-affirmation"> 
				<input type="hidden" name="path" value="${path }"> 
				<input type="hidden" name="treatmentPlanID" value="${treatmentPlan.treatmentPlanID}">
				<input type="hidden" name="clientUUID" value="${clientUUID }" >
				
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title" id="newAffirmationModalLabel">Enter
						a new affirmation</h4>
				</div>
				<div class="modal-body">
					<input type="text" class="form-control"
						id="newAffirmation" name="newAffirmation"
						value="<c:out value="${newAffirmation }"/>"
						placeholder="Enter a new affirmation.">
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
					<button type="submit" class="btn btn-primary" >Save</button>
				</div>
			</form>
		</div>
	</div>
</div>

<!-- End New Affirmation -->

<c:import url="/WEB-INF/jsp/footer.jsp" />

