package com.cggcoding.controllers;

import com.cggcoding.exceptions.DatabaseException;
import com.cggcoding.exceptions.ValidationException;
import com.cggcoding.models.User;
import com.cggcoding.models.UserAdmin;
import com.cggcoding.models.UserClient;
import com.cggcoding.models.UserTherapist;
import com.cggcoding.utils.database.MySQLActionHandler;
import com.cggcoding.utils.messaging.ErrorMessages;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.jdbc.pool.DataSource;

/**
 * Servlet implementation class MasterController
 */
@WebServlet("/LogIn")
public class LogIn extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogIn() {
        super();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String userRole = "";
        DataSource datasource = (DataSource)request.getServletContext().getAttribute("datasource");
        MySQLActionHandler mySQLActionHandler = new MySQLActionHandler(datasource);
    	
        
	        try {
				boolean userExists = mySQLActionHandler.validateUser(email, password);

				//use the above to get authenticate the user and get create a User object
				if(userExists){
					User user = mySQLActionHandler.getUserInfo(email, password);
					request.getSession().setAttribute("user", user);
					
					if(user.hasRole("admin")){
				        request.getRequestDispatcher("admintools/adminMainMenu.jsp").forward(request, response);
					} else if(user.hasRole("therapist")){
				        request.getRequestDispatcher("therapisttools/therapistMainMenu.jsp").forward(request, response);
					}if(user.hasRole("client")){
				        request.getRequestDispatcher("clienttools/clientmainmenu.jsp").forward(request,response);
					}
					
				} else {
				    throw new DatabaseException(ErrorMessages.INVALID_USERNAME_OR_PASSWORD);
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
				request.setAttribute("errorMessage", e.getMessage());
				//response.sendRedirect("index.jsp");
			    request.getRequestDispatcher("index.jsp").forward(request, response);
			}

    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
