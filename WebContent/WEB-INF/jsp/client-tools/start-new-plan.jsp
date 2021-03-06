
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>


<c:import url="/WEB-INF/jsp/header.jsp" />

<div class="page-header">
	<h1>Heading</h1>
	<h2>Subheading</h2>
</div>
  
	
	<div class="page-header">
        <h1>Select A Treatment Plan</h1>
        
    </div>
    
	<c:import url="/WEB-INF/jsp/message-modal.jsp"/>
	<p>Select a plan.</p>
	<form class="form-horizontal" action="/secure/ClientSelectPlan" method="POST">
		<input type="hidden" name="requestedAction" value="select-plan-load">
		<input type="hidden" name="path" value="${path }">

		
        <div class="form-group">
            <label for="assignedTreatmentPlanID" class="col-sm-2 control-label">Assigned Plans:</label>
            <div class="col-sm-10">
                <select class="form-control" id="selectedPlanID" name="selectedPlanID">
                    <option  value="">Select a plan.</option>
                    <c:forEach items="${assignedPlansList}" var="assignedPlan">
                        <option value="${assignedPlan.treatmentPlanID}" >${assignedPlan.title}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="form-group">
	        <div class="col-sm-offset-2 col-sm-10">
	        <p>Preview of selected client plans.</p>
	        </div>
        </div>
        
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" class="btn btn-default">Start!</button>
            </div>
        </div>
	</form>
	

<c:import url="/WEB-INF/jsp/footer.jsp" />