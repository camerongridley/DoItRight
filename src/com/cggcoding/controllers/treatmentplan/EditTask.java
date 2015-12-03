package com.cggcoding.controllers.treatmentplan;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.cggcoding.exceptions.DatabaseException;
import com.cggcoding.exceptions.ValidationException;
import com.cggcoding.models.Stage;
import com.cggcoding.models.Task;
import com.cggcoding.models.User;
import com.cggcoding.utils.CommonServletFunctions;
import com.cggcoding.utils.ParameterUtils;
import com.cggcoding.utils.messaging.WarningMessages;

/**
 * Servlet implementation class EditTask
 */
@WebServlet("/secure/EditTask")
public class EditTask extends HttpServlet {
	private static final long serialVersionUID = 1L;
	int userID = 0;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EditTask() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		/*--Common Servlet variables that should be in every controller--*/
		HttpSession session = request.getSession();
		User user = (User)session.getAttribute("user");
		String forwardTo = "index.jsp";
		String requestedAction = request.getParameter("requestedAction");
		String path = request.getParameter("path");
		request.setAttribute("path", path);
		/*-----------End Common Servlet variables---------------*/
		
		userID =  user.getUserID();
		
		/*This variable helps remember where to send the user back to when they are done editing the Task.
		If the Task being edited is a template the stageID will be TEMPLATE_HOLDER_ID, and not the Stage template being working on.
		If the Task being edited is part of a client's plan, then the stageID will be the stageID that is contained within the task.
		*/
		int stageToReturnTo = 0;
		
		
		//performed here to get parameters for all tasks run below
		Task tempTask = CommonServletFunctions.getTaskParametersFromRequest(request, userID);
		
		

		try {
			//put user-independent attributes acquired from database in the request
			request.setAttribute("taskTypeMap", Task.getTaskTypeMap());
			request.setAttribute("taskTemplateList", Task.getDefaultTasks());
			
			if(user.hasRole("admin")){
				switch(requestedAction){
				case ("edit-task-start"):
					tempTask.setTemplate(true);
					//set tempTask in request so page knows value of isTemplate
					request.setAttribute("task", tempTask);
					
					//
					stageToReturnTo = tempTask.getStageID();
					request.setAttribute("stageToReturnTo", stageToReturnTo);
					
					forwardTo = "/WEB-INF/jsp/treatment-plans/task-edit.jsp";
					break;
				case ("edit-task-select-task"):
					int selectedTaskID = ParameterUtils.parseIntParameter(request, "taskID");
					
					request.setAttribute("task", Task.load(selectedTaskID));
					
					//maintain stageToReturnTo
					stageToReturnTo = tempTask.getStageID();
					request.setAttribute("stageToReturnTo", stageToReturnTo);
					
					if(path.equals("treatmentPlanTemplate")|| path.equals("stageTemplate")){
						request.setAttribute("warningMessage", WarningMessages.EDITING_TASK_TEMPLATE);
					}
					
					forwardTo = "/WEB-INF/jsp/treatment-plans/task-edit.jsp";
					break;
				case ("edit-task-select-task-type"):
					// most of the work for this case was moved to CommonServletFunctions.getTaskParametersFromRequest, so now it just needs to set forwardTo
					
					forwardTo = "/WEB-INF/jsp/treatment-plans/task-edit.jsp";
					break;
				case ("edit-task-update"):
					
					tempTask.update();
					
					stageToReturnTo = ParameterUtils.parseIntParameter(request, "stageToReturnTo");
					if(path.equals("treatmentPlanTemplate")|| path.equals("stageTemplate")){
						request.setAttribute("stage", Stage.load(stageToReturnTo));
						request.setAttribute("defaultStageList", Stage.getDefaultStages());
						
						forwardTo = "/WEB-INF/jsp/treatment-plans/stage-edit.jsp";
					}else{
						forwardTo = "/WEB-INF/jsp/admin-tools/admin-main-menu.jsp";
					}
				
					break;
				}
			}
				
			
		} catch (DatabaseException | ValidationException e) {
			//put in temporary task object so values can be saved in inputs after error
			request.setAttribute("task", tempTask);
			request.setAttribute("stageToReturnTo", stageToReturnTo);
			request.setAttribute("errorMessage", e.getMessage());

			forwardTo = "/WEB-INF/jsp/treatment-plans/task-edit.jsp";
		}
		
		request.getRequestDispatcher(forwardTo).forward(request, response);
	}
	
	

}
