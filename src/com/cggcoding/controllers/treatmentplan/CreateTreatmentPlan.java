package com.cggcoding.controllers.treatmentplan;

import com.cggcoding.exceptions.DatabaseException;
import com.cggcoding.exceptions.ValidationException;
import com.cggcoding.models.TreatmentIssue;
import com.cggcoding.models.TreatmentPlan;
import com.cggcoding.models.User;
import com.cggcoding.models.UserAdmin;
import com.cggcoding.utils.messaging.ErrorMessages;

import java.io.IOException;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.jdbc.pool.DataSource;

/**
 * Created by cgrid_000 on 8/12/2015.
 * 
 */
@WebServlet("/CreateTreatmentPlan")
public class CreateTreatmentPlan extends HttpServlet {
	
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession();
    	DataSource datasource = (DataSource)request.getServletContext().getAttribute("datasource");
    	User user = (User)session.getAttribute("user");
    	String forwardTo = "index.jsp";
    	String requestedAction = request.getParameter("requestedAction");
    	String planName = "";
    	String planDescription = "";
    	String defaultIssueIDAsString = null;
    	String newDefaultIssueName = null;
    	String existingCustomIssueIDAsString = null;
    	String newCustomIssueName = null;
    	
    	
    	try {
			if(user.hasRole("client")){
				//UserClient userClient = (UserClient)session.getAttribute("user");
				forwardTo = "clientMainMenu.jsp";
	
			} else if(user.hasRole("therapist")){
				//UserTherapist userTherapist = (UserTherapist)session.getAttribute("user");
				switch (requestedAction){
	            case "planNameAndIssue":
	                planName = request.getParameter("planName");
	                planDescription = request.getParameter("planDescription");
	                int txIssueID;
					
					txIssueID = getTreatmentIssueID(user, defaultIssueIDAsString, existingCustomIssueIDAsString, newCustomIssueName, request);
					
	                TreatmentPlan newPlan = new TreatmentPlan(planName, user.getUserID(), planDescription, txIssueID);
	                request.setAttribute("newPlan", newPlan);
	                forwardTo = "/jsp/treatment-plans/create-treatment-plan-stages.jsp";
	                break;
	            default:
				}
				
			} else if(user.hasRole("admin")){
				UserAdmin userAdmin = (UserAdmin)session.getAttribute("user");
								
				switch (requestedAction){
					case "beginning":
						//get treatment issues associated with admin role
						ArrayList<TreatmentIssue> defaultreatmentIssues = userAdmin.getDefaultTreatmentIssues(datasource);//TODO change not use Datasource - update userAdmin model method
						session.setAttribute("defaultTreatmentIssues", defaultreatmentIssues);
						forwardTo = "/jsp/treatment-plans/create-treatment-plan-name.jsp";
						break;
		            case "planNameAndIssue":
		                planName = request.getParameter("planName");
		                planDescription = request.getParameter("planDescription");
		                defaultIssueIDAsString = request.getParameter("defaultTreatmentIssue");
		                existingCustomIssueIDAsString = request.getParameter("existingCustomTreatmentIssue");
		                newDefaultIssueName = request.getParameter("newDefaultTreatmentIssue");
		                
		                if(planName.isEmpty() || planDescription.isEmpty()){
		                	throw new ValidationException("You must enter a plan name and description.");
		                }
		                
		                //TODO uncomment this when resuming work of moving database calls to service layer/models - if(user.isValidNewTreatmentPlanName(datasource, planName) && user.isValidNewTreatmentIssue(datasource, newDefaultIssueName))
		                
		                //detect which treatment issue source was used and validate
		                int treatmentIssueID = getTreatmentIssueID(userAdmin, defaultIssueIDAsString, existingCustomIssueIDAsString, newDefaultIssueName, request);
		                
		                TreatmentPlan newPlan = new TreatmentPlan(planName, user.getUserID(), planDescription, treatmentIssueID);
		                
		                //submit to be validated and passes then inserted into database and get the treatmentplan with autogenerated id returned
		                newPlan.save(datasource);//TODO change not use Datasource - update TreatmentPlan model method and make sure validating properly, especially for if new name already exists, as at the time of this writing that is not getting checked
		
		                request.setAttribute("newPlan", newPlan);
		                forwardTo = "/jsp/treatment-plans/create-treatment-plan-stages.jsp";
		                break;
		            case "stageAndTask":
	            	
	            	forwardTo = "/jsp/createplan/createtxplan-review.jsp";
	            	break;
				}

			}
		
    	} catch (ValidationException | DatabaseException e) {
    		request.setAttribute("errorMessage", e.getMessage());
    		request.setAttribute("planName", planName);
    		request.setAttribute("planDescription", planDescription);
    		request.setAttribute("defaultTreatmentIssue", defaultIssueIDAsString);
    		request.setAttribute("existingCustomTreatmentIssue", existingCustomIssueIDAsString);
    		request.setAttribute("newDefaultTreatmentIssue", newDefaultIssueName);
    		request.setAttribute("newCustomTreatmentIssue", newCustomIssueName);
    		
    		forwardTo = "/jsp/treatment-plans/create-treatment-plan-name.jsp";
			//e.printStackTrace();
		}
    	
		request.getRequestDispatcher(forwardTo).forward(request,response);
		
    }
    
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
    
    
    
    /**
     * Gets the appropriate treatment issue id in the process of creating a new Treatment Plan.
     * There are 3 options for setting the issue type for the new plan.  Since only 1 issue can be selected, here we first get all the possible parameters from the request
     * and if more than one has been selected, notify the user that they can only choose 1.  Otherwise, use the selected treatment issue.
     * @param user
     * @param defaultIssueIDAsString
     * @param existingIssueIDAsString
     * @param newIssueName
     * @param request
     * @return the issueID of the selected TreatmentIssue
     * @throws ValidationException
     * @throws DatabaseException
     */
    private int getTreatmentIssueID(User user, String defaultIssueIDAsString, String existingIssueIDAsString, String newIssueName, HttpServletRequest request) throws ValidationException, DatabaseException{
        int issueID = -1;
        boolean hasNewCustomIssue = !newIssueName.isEmpty();

        int numOfIDs = 0;
        if(defaultIssueIDAsString != null){
	        if(!defaultIssueIDAsString.equals("")){
	            issueID = Integer.parseInt(defaultIssueIDAsString);
	            numOfIDs++;
	        }
        }
        
        if(existingIssueIDAsString != null){
        	if(!existingIssueIDAsString.equals("")){
	            issueID = Integer.parseInt(existingIssueIDAsString);
	            numOfIDs++;
        	}
        }    

        if(hasNewCustomIssue){
            numOfIDs++;
        }

        if(numOfIDs > 1){
            throw new ValidationException(ErrorMessages.USER_SELECTED_MULTIPLE_ISSUES);
        }else if(numOfIDs < 1) {
            throw new ValidationException(ErrorMessages.USER_SELECTED_NO_ISSUE);
        } else {
        	//if there are no validation problems and there is a new custom issue name, add the new issue to the database and get its id
        	if(hasNewCustomIssue){
        		DataSource datasource = (DataSource)request.getServletContext().getAttribute("datasource");

	        	TreatmentIssue issue = new TreatmentIssue(newIssueName, user.getUserID());
	        	
				issue = user.createTreatmentIssue(datasource, issue);
				
	
	            issueID = issue.getTreatmentIssueID();
        	}
        	
        }


        return issueID;
    }
}
