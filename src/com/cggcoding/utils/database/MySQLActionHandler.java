package com.cggcoding.utils.database;

import java.io.Serializable;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.cggcoding.exceptions.DatabaseException;
import com.cggcoding.exceptions.ValidationException;
import com.cggcoding.messaging.invitations.Invitation;
import com.cggcoding.models.*;
import com.cggcoding.utils.Constants;
import com.cggcoding.utils.SqlBuilders;
import com.cggcoding.utils.messaging.ErrorMessages;

/**
 * Created by cgrid_000 on 8/26/2015.
 */
/**
 * @author cgrid_000
 *
 */
public class MySQLActionHandler implements Serializable, DatabaseActionHandler{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DatabaseConnection mysqlConn;

    public MySQLActionHandler(){
    	this.mysqlConn = new MySQLConnection();
    }

    /* (non-Javadoc)
	 * @see com.cggcoding.utils.database.DatabaseActionHandler#getConnection()
	 */
	@Override
    public Connection getConnection() throws DatabaseException{
		Connection cn = null;
		
		cn = mysqlConn.getConnection();
		if(cn == null){
			throw new DatabaseException(ErrorMessages.CONNECTION_IS_NULL);
		}
		
		return cn;
    }
	
	//TODO Move these 3 methods to CommonValidation.java?
	@Override
	public boolean throwValidationExceptionIfTemplateHolderID(int templateHolderObjectID) throws ValidationException{
		if(templateHolderObjectID == Constants.TEMPLATES_HOLDER_PRIMARY_KEY_ID){
			throw new ValidationException(ErrorMessages.DEFAULTS_HOLDER_ID_SELECTED);
		}
		
		return true;
	}
	
	@Override
	public boolean throwValidationExceptionIfNull(Object o) throws ValidationException{
		if(o == null){
			throw new ValidationException(ErrorMessages.OBJECT_IS_NULL);
		}
		
		return true;
	}
	
	@Override
	public boolean throwValidationExceptionIfZero(int id) throws ValidationException{
		if(id == 0){
			throw new ValidationException(ErrorMessages.INVALID_SELECTION);
		}
		
		return true;
	}
    
	
	@Override
	public boolean userValidateNewUsername(Connection cn, String userName) throws SQLException{

		PreparedStatement ps = null;
	    ResultSet userInfo = null;
	    int userNameExists = 0;
	    
	    try {
			ps = cn.prepareStatement("SELECT COUNT(*) FROM user WHERE user_name=?");
	        ps.setString(1, userName);
	
	        userInfo = ps.executeQuery();
	
	
	        while (userInfo.next()){
	            userNameExists = userInfo.getInt("COUNT(*)");
	        }
	
	    } finally {
			DbUtils.closeQuietly(userInfo);
			DbUtils.closeQuietly(ps);
		}
	
	
	    if(userNameExists == 1){
	        return false;
	    } else {
	        return true;
	    }
	}

	@Override
	public boolean userValidateNewEmail(Connection cn, String email) throws SQLException{

		PreparedStatement ps = null;
	    ResultSet userInfo = null;
	    int emailNameExists = 0;
	    
	    try {
			ps = cn.prepareStatement("SELECT COUNT(*) FROM user WHERE email=?");
	        ps.setString(1, email);
	
	        userInfo = ps.executeQuery();
	
	
	        while (userInfo.next()){
	            emailNameExists = userInfo.getInt("COUNT(*)");
	        }
	
	    } finally {
			DbUtils.closeQuietly(userInfo);
			DbUtils.closeQuietly(ps);
		}
	
	
	    if(emailNameExists == 1){
	        return false;
	    } else {
	        return true;
	    }
	}
	
	@Override
	public User userCreateNewUser(Connection cn, User newUser, byte[] encryptedPassword, byte[] passwordSalt) throws SQLException{

		PreparedStatement ps = null;
		ResultSet generatedKeys = null;
	    
	    try {
	    	String sql = "INSERT INTO user (user_user_role_id_fk, user_name, first_name, last_name, email, encrypted_password, password_salt) VALUES (?, ?, ?, ?, ?, ?, ?)";
	    	
	    	ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	    	
	        ps.setInt(1, newUser.getRoleID());
	        ps.setString(2, newUser.getUserName());
	        ps.setString(3, newUser.getFirstName());
	        ps.setString(4, newUser.getLastName());
	        ps.setString(5, newUser.getEmail());
	        ps.setBytes(6, encryptedPassword);
	        ps.setBytes(7, passwordSalt);

	        int success = ps.executeUpdate();
	
	        generatedKeys = ps.getGeneratedKeys();
	        
            while (generatedKeys.next()){
            	newUser.setUserID(generatedKeys.getInt(1));;
            }
	
	    } finally {
			DbUtils.closeQuietly(generatedKeys);
			DbUtils.closeQuietly(ps);
		}
	
	
	    return newUser;
	}
	
	@Override
	public boolean userUpdate(Connection cn, User user, UserPassword newUserPassword) throws SQLException{
		PreparedStatement ps = null;
        int success = 0;
        StringBuilder sqlBuilder = new StringBuilder("UPDATE user SET user_name=?, first_name=?, last_name=?, email=? ");
        
        try {
        	
        	if(newUserPassword==null){
        		sqlBuilder.append("WHERE user_id=?;");
            	
                ps = cn.prepareStatement(sqlBuilder.toString());

                ps.setString(1, user.getUserName());
    	        ps.setString(2, user.getFirstName());
    	        ps.setString(3, user.getLastName());
    	        ps.setString(4, user.getEmail());
    	      
    	        ps.setInt(5, user.getUserID());
        	}else{
        		sqlBuilder.append(", encrypted_password=?, password_salt=? WHERE user_id=?;");
        		//sql = "UPDATE user SET user_name=?, first_name=?, last_name=?, email=?, encrypted_password=?, password_salt=? WHERE user_id=?;";
            	
                ps = cn.prepareStatement(sqlBuilder.toString());

                ps.setString(1, user.getUserName());
    	        ps.setString(2, user.getFirstName());
    	        ps.setString(3, user.getLastName());
    	        ps.setString(4, user.getEmail());
    	        ps.setBytes(5, newUserPassword.getEncryptedPassword());
    	        ps.setBytes(6, newUserPassword.getPasswordSalt());
    	        ps.setInt(7, user.getUserID());
        	}
        	
            

            success = ps.executeUpdate();
        	
        } finally {
			DbUtils.closeQuietly(ps);
        }
        
        return success == 1;
	}
	
	@Override
	public boolean userClientUpdateActiveTreatmentPlanID(Connection cn, UserClient client) throws SQLException{
		PreparedStatement ps = null;
        int success = 0;
        String sql = "";
        
        try {

    		sql = "UPDATE user SET active_treatment_plan_id=? WHERE user_id=?;";
        	
            ps = cn.prepareStatement(sql);

            ps.setInt(1, client.getActiveTreatmentPlanID());
	        ps.setInt(2, client.getUserID());

            success = ps.executeUpdate();
        	
        } finally {
			DbUtils.closeQuietly(ps);
        }
        
        return success == 1;
	}
	
    /* (non-Javadoc)
	 * @see com.cggcoding.utils.database.DatabaseActionHandler#validateUser(java.lang.String, java.lang.String)
	 */
    @Override
	public boolean userValidate(String email, String password) throws DatabaseException{
    	Connection cn = null;
    	PreparedStatement ps = null;
        ResultSet userInfo = null;
        int userExists = 0;
        
        try {
        	cn = getConnection();
    		ps = cn.prepareStatement("SELECT COUNT(*) FROM user WHERE email=? AND password=?");
            ps.setString(1, email);
            ps.setString(2, password);

            userInfo = ps.executeQuery();


            while (userInfo.next()){
                userExists = userInfo.getInt("COUNT(*)");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException(ErrorMessages.GENERAL_DB_ERROR);
        } finally {
			DbUtils.closeQuietly(userInfo);
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(cn);
			
		}


        if(userExists == 1){
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public UserPassword userGetEncryptedPasswordAndSalt(Connection cn, String emailAddress) throws SQLException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        UserPassword userPasswordInfo = null;
        
        try {
            ps = cn.prepareStatement("SELECT encrypted_password, password_salt FROM user WHERE email = ?");
            ps.setString(1, emailAddress);

            rs = ps.executeQuery();
            
            while (rs.next()){
            	userPasswordInfo = new UserPassword(rs.getBytes("encrypted_password"), rs.getBytes("password_salt"));
            }
            
            //XXX decide if want to uncomment this - commented out because I want it to return null when there is no user found for when checking if a user with this email exists when sending invitations to join the site
            /*if(user==null){
            	throw new ValidationException(ErrorMessages.USER_NOT_FOUND);
            }*/

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        return userPasswordInfo;
    }
    
    //XXX can I move the switch statement into the User model?
    @Override
	public User userLoadInfo(Connection cn, String email, String password) throws DatabaseException{
    	PreparedStatement ps = null;
        ResultSet rsUserInfo = null;
        User user = null;
        
        try {
            ps = cn.prepareStatement("SELECT user.user_id, user.user_name, user.first_name, user.last_name, user.email, user.active_treatment_plan_id, user_role.role FROM user_role INNER JOIN (user) ON user_role.user_role_id = user.user_user_role_id_fk WHERE (((user.email)=?))");
            ps.setString(1, email);

            rsUserInfo = ps.executeQuery();
            
            //XXX - replace the use of downcasting? - e.g. UserClient.setActiveTreatmentPlanID
            // see http://programmers.stackexchange.com/questions/258655/ood-java-inheritance-and-access-to-child-methods-via-casting 
            while (rsUserInfo.next()){
            	switch (rsUserInfo.getString("role")){
            		case "admin":
            			user = new UserAdmin(rsUserInfo.getInt("user_id"), rsUserInfo.getString("user_name"), rsUserInfo.getString("first_name"), rsUserInfo.getString("last_name"), rsUserInfo.getString("email"));
            			break;
            		case "therapist":
            			user = new UserTherapist(rsUserInfo.getInt("user_id"), rsUserInfo.getString("user_name"), rsUserInfo.getString("first_name"), rsUserInfo.getString("last_name"), rsUserInfo.getString("email"));
            			break;
            		case "client":
            			user = new UserClient(rsUserInfo.getInt("user_id"), rsUserInfo.getString("user_name"), rsUserInfo.getString("first_name"), rsUserInfo.getString("last_name"), rsUserInfo.getString("email"));

            			((UserClient)user).setActiveTreatmentPlanID(rsUserInfo.getInt("active_treatment_plan_id"));
            			break;
            	}
            }
            

        } catch (SQLException e) {
        	e.printStackTrace();
        	throw new DatabaseException(ErrorMessages.GENERAL_DB_ERROR);
        } finally {
        	DbUtils.closeQuietly(rsUserInfo);
			DbUtils.closeQuietly(ps);
        }

        return user;
    }
    
    @Override
	public User userLoadByID(int userID) throws DatabaseException, ValidationException{
    	Connection cn = null;
    	PreparedStatement ps = null;
        ResultSet rsUserInfo = null;
        User user = null;
        
        try {
        	cn = getConnection();
            ps = cn.prepareStatement("SELECT * FROM user WHERE user_id = ?");
            ps.setInt(1, userID);

            rsUserInfo = ps.executeQuery();
            
            while (rsUserInfo.next()){
            	user = new UserAdmin(rsUserInfo.getInt("user_id"), rsUserInfo.getString("user_name"), rsUserInfo.getString("first_name"), rsUserInfo.getString("last_name"), rsUserInfo.getString("email"));
            }
            
            if(user==null){
            	throw new ValidationException(ErrorMessages.USER_NOT_FOUND);
            }

        } catch (SQLException e) {
        	e.printStackTrace();
        	throw new DatabaseException(ErrorMessages.GENERAL_DB_ERROR);
        } finally {
        	DbUtils.closeQuietly(rsUserInfo);
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(cn);
        }

        return user;
    }
    
    @Override
	public User userLoadByEmailAddress(Connection cn, String emailAddress) throws ValidationException, SQLException{

    	PreparedStatement ps = null;
        ResultSet rsUserInfo = null;
        User user = null;
        
        try {
            ps = cn.prepareStatement("SELECT * FROM user WHERE email = ?");
            ps.setString(1, emailAddress);

            rsUserInfo = ps.executeQuery();
            
            while (rsUserInfo.next()){
            	user = new UserAdmin(rsUserInfo.getInt("user_id"), rsUserInfo.getString("user_name"), rsUserInfo.getString("first_name"), rsUserInfo.getString("last_name"), rsUserInfo.getString("email"));
            }
            
            //XXX decide if want to uncomment this - commented out because I want it to return null when there is no user found for when checking if a user with this email exists when sending invitations to join the site
            /*if(user==null){
            	throw new ValidationException(ErrorMessages.USER_NOT_FOUND);
            }*/

        } finally {
        	DbUtils.closeQuietly(rsUserInfo);
			DbUtils.closeQuietly(ps);
        }

        return user;
    }
    
    @Override
	public boolean invitationAlreadyExists(Connection cn,  Invitation invitation) throws SQLException{

		PreparedStatement ps = null;
	    ResultSet invitationInfo = null;
	    int invitationExists = 0;
	    
	    try {
			ps = cn.prepareStatement("SELECT COUNT(*) FROM invitation WHERE recipient_email=? AND sender_user_id_fk=?");
	        ps.setString(1, invitation.getRecipientEmail());
	        ps.setInt(2, invitation.getSenderUserID());
	        
	        invitationInfo = ps.executeQuery();
	
	
	        while (invitationInfo.next()){
	            invitationExists = invitationInfo.getInt("COUNT(*)");
	        }
	
	    } finally {
			DbUtils.closeQuietly(invitationInfo);
			DbUtils.closeQuietly(ps);
		}
	
	
	    if(invitationExists >= 1){
	        return true;
	    } else {
	        return false;
	    }
	}
    
    @Override
	public void invitationCreate(Connection cn, Invitation invitation) throws SQLException{

		PreparedStatement ps = null;
	    
	    try {
	    	//first insert primary invitation data into the invitation table
	    	String sql = "INSERT INTO invitation (invitation_code, recipient_email, sender_user_id_fk, date_invited, date_accepted, accepted, recipient_first_name, recipient_last_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	    	
	    	ps = cn.prepareStatement(sql);
	    	
	        ps.setString(1, invitation.getInvitationCode());
	        ps.setString(2, invitation.getRecipientEmail());
	        ps.setInt(3, invitation.getSenderUserID());
	        ps.setTimestamp(4, convertLocalTimeDateToTimstamp(invitation.getDateInvited()));
	        ps.setTimestamp(5, convertLocalTimeDateToTimstamp(invitation.getDateAccepted()));
	        ps.setBoolean(6, invitation.isAccepted());
	        ps.setString(7, invitation.getRecipientFirstName());
	        ps.setString(8, invitation.getRecipientLastName());

	        int success = ps.executeUpdate();
	        
	        DbUtils.closeQuietly(ps);
	
	        //now loop through all the treatmentPlanIDs in the invitation that are to be copied into the invitees account when they register
	        
        	sql = "INSERT INTO invitation_treatment_plans (invitation_code_fk, invitation_treatment_plan_id_fk) VALUES (?, ?)";
	    	
	    	ps = cn.prepareStatement(sql);
		    	
		    for(int treatmentPlanID : invitation.getTreatmentPlanIDsToCopy()){
		        ps.setString(1, invitation.getInvitationCode());
		        ps.setInt(2, treatmentPlanID);

		        ps.addBatch();
	        }
		    
		    ps.executeBatch();
	
	    } finally {
			DbUtils.closeQuietly(ps);
		}
	
	}
    
    @Override
    public void invitationDelete(Connection cn, String invitationCode) throws SQLException{
    	PreparedStatement ps = null;
        
    	try{
	        ps = cn.prepareStatement("DELETE FROM invitation WHERE invitation_code=?");
	        ps.setString(1, invitationCode);
	
	        ps.executeUpdate();
    	}finally{
    		DbUtils.closeQuietly(ps);
    	}
    }
    
    //TODO load the treamtnPlanIDsToCopy - just set to null for now
    @Override
    public Invitation invitationLoad(Connection cn, String invitationCode) throws SQLException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        Invitation invitation = null;
        List<Integer> treatmentPlanIDsToCopy = new ArrayList<>();
        
        try {
    		String sql = "SELECT * FROM invitation WHERE invitation_code =?";
        	
            ps = cn.prepareStatement(sql);
            
            ps.setString(1, invitationCode);
            
            rs = ps.executeQuery();
   
            while (rs.next()){
            	Timestamp timestamp = rs.getTimestamp("date_invited");
            	LocalDateTime dateInvited = convertTimestampToLocalDateTime(timestamp);
            	
            	timestamp = rs.getTimestamp("date_accepted");
            	LocalDateTime dateAccepted = convertTimestampToLocalDateTime(timestamp);
            	
            	invitation = new Invitation(rs.getString("invitation_code"), rs.getInt("sender_user_id_fk"), rs.getString("recipient_email"), 
            			rs.getString("recipient_first_name"), rs.getString("recipient_last_name"), dateInvited, dateAccepted, rs.getBoolean("accepted"), treatmentPlanIDsToCopy); 
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        //throwValidationExceptionIfNull(stage);
        
        return invitation;
    	
    }
    
    @Override
    public boolean invitationUpdate(Connection cn, Invitation invitation) throws SQLException{
    	PreparedStatement ps = null;
        int success = 0;
        
        try {
        		
    		String sql = "UPDATE invitation SET recipient_email=?, sender_user_id_fk=?, date_invited=?, date_accepted=?, accepted=?, recipient_first_name=?, recipient_last_name=? WHERE invitation_code=?";
        	
            ps = cn.prepareStatement(sql);

            ps.setString(1, invitation.getRecipientEmail());
            ps.setInt(2, invitation.getSenderUserID());
            ps.setTimestamp(3, convertLocalTimeDateToTimstamp(invitation.getDateInvited()));
            ps.setTimestamp(4, convertLocalTimeDateToTimstamp(invitation.getDateAccepted()));
            ps.setBoolean(5, invitation.isAccepted());
            ps.setString(6, invitation.getRecipientFirstName());
            ps.setString(7, invitation.getRecipientLastName());
            ps.setString(8, invitation.getInvitationCode());


            success = ps.executeUpdate();
        	
        } finally {
			DbUtils.closeQuietly(ps);
        }
        
        return success == 1;
    	
    }
    
    @Override
    public List<String> invitationGetSentInvitationCodes(Connection cn, int senderUserID) throws SQLException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        Invitation invitation = null;
        List<String> invitationCodes = new ArrayList<>();
        
        try {
    		String sql = "SELECT invitation_code FROM invitation WHERE sender_user_id_fk =? ORDER BY date_invited";
        	
            ps = cn.prepareStatement(sql);
            
            ps.setInt(1, senderUserID);
            
            rs = ps.executeQuery();
   
            while (rs.next()){
            	invitationCodes.add(rs.getString("invitation_code"));
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        //throwValidationExceptionIfNull(stage);
        
        return invitationCodes;
    }
    
    @Override
    public void therapistCreateClientConnection(Connection cn, int therapistUserID, int clientUserID) throws SQLException{

		PreparedStatement ps = null;
	    
	    try {
	    	//first insert primary invitation data into the invitation table
	    	String sql = "INSERT INTO therapist_user_id_client_user_id_maps (therapist_user_id, client_user_id) VALUES (?, ?)";
	    	
	    	ps = cn.prepareStatement(sql);
	    	
	        ps.setInt(1, therapistUserID);
	        ps.setInt(2, clientUserID);

	        int success = ps.executeUpdate();
	
	    } finally {
			DbUtils.closeQuietly(ps);
		}
    }
    
    //XXX Make this public and called from User class?
    private List<Integer> userGetAdminIDs(Connection cn) throws SQLException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        List<Integer> adminIDList = new ArrayList<>();
        
        try {
    		String sql = "SELECT user.user_id FROM user_role INNER JOIN (user) ON user_role.user_role_id = user.user_user_role_id_fk WHERE (((user_role.user_role_id)=" + Constants.ADMIN_ROLE_ID + "))";    	

    		ps = cn.prepareStatement(sql);
    		
            rs = ps.executeQuery();
   
            while (rs.next()){
            	adminIDList.add(rs.getInt("user_id"));
            }
        	
        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        return adminIDList;
    }
    
    @Override
    public Map<Integer, UserClient> userGetClientsByTherapistID(Connection cn, int therapistID) throws SQLException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        Map<Integer, UserClient> clients = new LinkedHashMap<>();
        
        try {

            ps = cn.prepareStatement("SELECT therapist_user_id_client_user_id_maps.therapist_user_id, "
            		+ "therapist_user_id_client_user_id_maps.client_user_id, user.user_name, user.first_name, user.last_name, user.email, user.password, "
            		+ "user.user_user_role_id_fk, user.active_treatment_plan_id, user_role.role "
            		+ "FROM user_role INNER JOIN ((user) INNER JOIN therapist_user_id_client_user_id_maps "
            		+ "ON user.user_id = therapist_user_id_client_user_id_maps.client_user_id) "
            		+ "ON user_role.user_role_id = user.user_user_role_id_fk "
            		+ "WHERE (((therapist_user_id_client_user_id_maps.therapist_user_id)=?))"
            		+ "ORDER BY user.email");

            ps.setInt(1, therapistID);


            rs = ps.executeQuery();


            while (rs.next()){
            	UserClient client = new UserClient(rs.getInt("client_user_id"), rs.getString("user_name"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("email"));
            	client.setRoleID(rs.getInt("user_user_role_id_fk"));
            	client.addRole(rs.getString("role"));
                clients.put(client.getUserID(), client);
            }

        } finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
		}
        return clients;
    }
    
    @Override
	public List<TreatmentPlan> userGetTreatmentPlans(Connection cn, int clientUserID) throws ValidationException, SQLException {

    	PreparedStatement ps = null;
        ResultSet rs = null;
        List<TreatmentPlan> assignedTreatmentPlans = new ArrayList<>();
        
        try {

    		ps = cn.prepareStatement("SELECT treatment_plan_id FROM treatment_plan WHERE treatment_plan_user_id_fk = ?");
    		ps.setInt(1, clientUserID);
            
    		rs = ps.executeQuery();
   
            while (rs.next()){
            	assignedTreatmentPlans.add(TreatmentPlan.load(cn, rs.getInt("treatment_plan_id")));
            	
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        
        return assignedTreatmentPlans;
	}
    
    @Override
	public List<TreatmentPlan> userGetTherapistAssignedPlans(int clientUserID, int assignedByUserID) throws DatabaseException, ValidationException {
		Connection cn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        List<TreatmentPlan> assignedTreatmentPlans = new ArrayList<>();
        
        try {
        	cn = getConnection();

    		ps = cn.prepareStatement("SELECT * FROM treatment_plan WHERE treatment_plan_user_id_fk=? AND treatment_plan_assigned_by_user_id_fk=?");
    		ps.setInt(1, clientUserID);
    		ps.setInt(2, assignedByUserID);
            
    		rs = ps.executeQuery();
   
            while (rs.next()){
            	assignedTreatmentPlans.add(TreatmentPlan.load(cn, rs.getInt("treatment_plan_id")));
            	
            }


        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException(ErrorMessages.GENERAL_DB_ERROR);
        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(cn);
        }
        
        
        return assignedTreatmentPlans;
	}
    
    @Override
    public boolean userOwnsTreatmentPlan(Connection cn, User authenticatedUser, int treatmentPlanID) throws SQLException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        int planCount = 0;
        
        try {
    		ps = cn.prepareStatement("SELECT COUNT(*) FROM treatment_plan WHERE treatment_plan_id=? AND treatment_plan_user_id_fk=?");
            ps.setInt(1, treatmentPlanID);
            ps.setInt(2, authenticatedUser.getUserID());

            rs = ps.executeQuery();


            while (rs.next()){
                planCount = rs.getInt("COUNT(*)");
            }

        
        } finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);	
		}


        if(planCount == 1){
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean userAssignedTreatmentPlan(Connection cn, User authenticatedUser, int treatmentPlanID) throws SQLException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        int planCount = 0;
        
        try {
    		ps = cn.prepareStatement("SELECT COUNT(*) FROM treatment_plan WHERE treatment_plan_id=? AND treatment_plan_assigned_by_user_id_fk=?");
            ps.setInt(1, treatmentPlanID);
            ps.setInt(2, authenticatedUser.getUserID());

            rs = ps.executeQuery();


            while (rs.next()){
                planCount = rs.getInt("COUNT(*)");
            }

        
        } finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);	
		}


        if(planCount == 1){
            return true;
        } else {
            return false;
        }
    }
    
	@Override
	public List<TreatmentPlan> treatmentPlanGetCoreList() throws DatabaseException, ValidationException {
		Connection cn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        List<TreatmentPlan> corePlansList = new ArrayList<>();
        
        try {
        	cn = getConnection();
        	
        	List<Integer> adminIDList = userGetAdminIDs(cn);
        	
        	String baseStatement = "SELECT * FROM treatment_plan WHERE treatment_plan_is_template=? AND treatment_plan_user_id_fk in (";
        	
        	String orderByClause = "ORDER BY treatment_plan_title";
        	
        	String sql = SqlBuilders.includeMultipleIntParams(baseStatement, adminIDList, orderByClause);
        	
    		ps = cn.prepareStatement(sql);
    		
    		ps.setBoolean(1, true);
    		for(int i = 0; i < adminIDList.size(); i++){
    			ps.setInt(i+2, adminIDList.get(i));
    		}
            
            rs = ps.executeQuery();
   
            while (rs.next()){
            	if(rs.getInt("treatment_plan_id") != Constants.TEMPLATES_HOLDER_PRIMARY_KEY_ID){ //TreatmentPlan with id=1 is the Plan that holds all Stage Defaults and so should not be included in the results of this query.
            		corePlansList.add(treatmentPlanLoadBasic(cn, rs.getInt("treatment_plan_id")));
            	}
            	
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException(ErrorMessages.GENERAL_DB_ERROR);
        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(cn);
        }
        
        throwValidationExceptionIfNull(corePlansList);
        
        return corePlansList;
	}
	
	@Override
    public TreatmentPlan treatmentPlanLoadBasic(Connection cn, int treatmentPlanID) throws SQLException, ValidationException{
    	PreparedStatement ps = null;
        ResultSet rsPlanInfo = null;
        TreatmentPlan plan = null;
        
        throwValidationExceptionIfTemplateHolderID(treatmentPlanID);
        
        try {
            ps = cn.prepareStatement("SELECT * from treatment_plan WHERE treatment_plan_id=?");
            ps.setInt(1, treatmentPlanID);


            rsPlanInfo = ps.executeQuery();

            while (rsPlanInfo.next()){
            	plan = TreatmentPlan.getInstanceBasic(rsPlanInfo.getInt("treatment_plan_id"), rsPlanInfo.getInt("treatment_plan_user_id_fk"), 
            			rsPlanInfo.getString("treatment_plan_title"), rsPlanInfo.getString("treatment_plan_description"), rsPlanInfo.getInt("treatment_plan_treatment_issue_id_fk"),
            			rsPlanInfo.getBoolean("in_progress"), rsPlanInfo.getBoolean("treatment_plan_is_template"), rsPlanInfo.getBoolean("treatment_plan_completed"),
            			rsPlanInfo.getInt("current_stage_index"), rsPlanInfo.getInt("active_view_stage_index"), rsPlanInfo.getInt("template_id"), 
            			rsPlanInfo.getInt("treatment_plan_assigned_by_user_id_fk"));
            	
            }
            
        } finally {
        	DbUtils.closeQuietly(rsPlanInfo);
			DbUtils.closeQuietly(ps);
        }

        //throwValidationExceptionIfNull(plan);
        
        return plan;
    }
    
	@Override
    public List<Stage> treatmentPlanLoadClientStages(Connection cn, int treatmentPlanID) throws SQLException, ValidationException {
    	PreparedStatement ps = null;
        ResultSet rs = null;
        List<Stage> stages = new ArrayList<>();
        
        throwValidationExceptionIfTemplateHolderID(treatmentPlanID);
        
        try {
        	ps = cn.prepareStatement("SELECT stage_id FROM stage WHERE stage_treatment_plan_id_fk=? ORDER BY client_stage_order");
            ps.setInt(1, treatmentPlanID);


            rs = ps.executeQuery();

            while (rs.next()){
            	stages.add(Stage.load(cn, rs.getInt("stage_id")));
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }

        return stages;
    }
	
	//OPTIMIZE instead of calling Stage.load() for each record, could change SELECT statement to return all records from Stage that match and build each Task inside this method.
	//I like the current way because means I have fewer methods to update should the Stage object change.
	@Override
	public List<Stage> treatmentPlanLoadTemplateStages(Connection cn, int treatmentPlanID) throws SQLException, ValidationException {
		PreparedStatement ps = null;
        ResultSet rs = null;
        List<Stage> stages = new ArrayList<>();
        
        try {
            ps = cn.prepareStatement("SELECT stage_template_id_fk, template_stage_order FROM treatment_plan_template_stage_template_maps WHERE treatment_plan_template_id_fk=? ORDER BY template_stage_order");
            ps.setInt(1, treatmentPlanID);
            

            rs = ps.executeQuery();
            
            while (rs.next()){
            	Stage stageTemplate = Stage.load(cn, rs.getInt("stage_template_id_fk"));
            	stageTemplate.setClientStageOrder(rs.getInt("template_stage_order"));
            	stages.add(stageTemplate);
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);

        }

        return stages;
	}
	
	@Override
	public List<Stage> treatmentPlanUpdateTemplateStages(Connection cn, int treatmentPlanID, List<Stage> stageTemplates) throws SQLException{
		PreparedStatement ps = null;
        List<Stage> stages = new ArrayList<>();
        
        try {
        	for(Stage stage : stageTemplates){
        		ps = cn.prepareStatement("UPDATE treatment_plan_template_stage_template_maps SET stage_template_id_fk=?, treatment_plan_template_id_fk=?, template_stage_order=? WHERE stage_template_id_fk=? and treatment_plan_template_id_fk=?");
                ps.setInt(1,stage.getStageID());
                ps.setInt(2,treatmentPlanID);
                ps.setInt(3,stage.getClientStageOrder());
                ps.setInt(4,stage.getStageID());
                ps.setInt(5,treatmentPlanID);
                
                ps.executeUpdate();
        	}            
            
        } finally {
			DbUtils.closeQuietly(ps);
        }

        return stages;
	}
    
    @Override
	public TreatmentPlan treatmentPlanCreateBasic(Connection cn, TreatmentPlan treatmentPlan) throws SQLException, ValidationException{		
    	PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        
        throwValidationExceptionIfTemplateHolderID(treatmentPlan.getTreatmentPlanID());
        
        try {
        	String sql = "INSERT INTO treatment_plan (treatment_plan_user_id_fk, treatment_plan_treatment_issue_id_fk, treatment_plan_title, treatment_plan_description, "
        			+ "current_stage_index, active_view_stage_index, in_progress, treatment_plan_is_template, template_id, treatment_plan_assigned_by_user_id_fk) "
            		+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        	
            ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, treatmentPlan.getUserID());
            ps.setInt(2, treatmentPlan.getTreatmentIssueID());
            ps.setString(3, treatmentPlan.getTitle().trim());
            ps.setString(4, treatmentPlan.getDescription());
            ps.setInt(5, treatmentPlan.getCurrentStageIndex());
            ps.setInt(6, treatmentPlan.getActiveViewStageIndex());
            ps.setBoolean(7, treatmentPlan.isInProgress());
            ps.setBoolean(8, treatmentPlan.isTemplate());
            ps.setInt(9, treatmentPlan.getTemplateID());
            ps.setInt(10, treatmentPlan.getAssignedByUserID());

            int success = ps.executeUpdate();
            
            generatedKeys = ps.getGeneratedKeys();
   
            while (generatedKeys.next()){
            	treatmentPlan.setTreatmentPlanID(generatedKeys.getInt(1));;
            }

        } finally {
        	DbUtils.closeQuietly(generatedKeys);
			DbUtils.closeQuietly(ps);
        }
        throwValidationExceptionIfNull(treatmentPlan);
        
        return treatmentPlan;
	}
    
    @Override
	public boolean treatmentPlanValidateUpdatedTitle(Connection cn, TreatmentPlan treatmentPlan) throws ValidationException, SQLException{
    	PreparedStatement ps = null;
        ResultSet issueCount = null;
        int comboExists = 0;
        
        try {
            ps = cn.prepareStatement("SELECT COUNT(*)  FROM treatment_plan WHERE treatment_plan.treatment_plan_title=? AND treatment_plan_id!=? AND treatment_plan_is_template=? AND treatment_plan.treatment_plan_user_id_fk=?");
            ps.setString(1, treatmentPlan.getTitle().trim());
            ps.setInt(2, treatmentPlan.getTreatmentPlanID());
            ps.setBoolean(3, treatmentPlan.isTemplate());
            ps.setInt(4, treatmentPlan.getUserID());

            issueCount = ps.executeQuery();


            while (issueCount.next()){
                comboExists = issueCount.getInt("COUNT(*)");
            }

        } finally {
			DbUtils.closeQuietly(issueCount);
			DbUtils.closeQuietly(ps);
		}
		
        if(comboExists > 0){
        	throw new ValidationException(ErrorMessages.PLAN_TITLE_EXISTS);
        } else {
        	return true;
        }
		
	}

    @Override
	public void treatmentPlanUpdateBasic(Connection cn, TreatmentPlan treatmentPlan) throws SQLException, ValidationException {
		PreparedStatement ps = null;
        
        throwValidationExceptionIfNull(treatmentPlan);
        
        try {
        	String sql = "UPDATE treatment_plan SET treatment_plan_user_id_fk=?, treatment_plan_treatment_issue_id_fk=?, treatment_plan_title=?, treatment_plan_description=?, current_stage_index=?, "
        			+ "active_view_stage_index=?, in_progress=?, treatment_plan_is_template=?, treatment_plan_completed=?, template_id=?, treatment_plan_assigned_by_user_id_fk=?"
        			+ " WHERE treatment_plan_id=?";
        	
            ps = cn.prepareStatement(sql);
            
            ps.setInt(1, treatmentPlan.getUserID());
            ps.setInt(2, treatmentPlan.getTreatmentIssueID());
            ps.setString(3, treatmentPlan.getTitle().trim());
            ps.setString(4, treatmentPlan.getDescription());
            ps.setInt(5, treatmentPlan.getCurrentStageIndex());
            ps.setInt(6, treatmentPlan.getActiveViewStageIndex());
            ps.setBoolean(7, treatmentPlan.isInProgress());
            ps.setBoolean(8, treatmentPlan.isTemplate());
            ps.setBoolean(9, treatmentPlan.isCompleted());
            ps.setInt(10, treatmentPlan.getTemplateID());
            ps.setInt(11, treatmentPlan.getAssignedByUserID());
            
            ps.setInt(12, treatmentPlan.getTreatmentPlanID());
            

            int success = ps.executeUpdate();

        } finally {
			DbUtils.closeQuietly(ps);
        }
	}
    
    @Override
	public boolean treatmentPlanValidateNewTitle(Connection cn, int userID, String planTitle) throws ValidationException, SQLException{
    	PreparedStatement ps = null;
        ResultSet issueCount = null;
        int comboExists = 0;
        
        try {

            ps = cn.prepareStatement("SELECT COUNT(*)  FROM treatment_plan WHERE treatment_plan.treatment_plan_title=? AND treatment_plan.treatment_plan_user_id_fk=?");
            ps.setString(1, planTitle.trim());
            ps.setInt(2, userID);

            issueCount = ps.executeQuery();


            while (issueCount.next()){
                comboExists = issueCount.getInt("COUNT(*)");
            }

        } finally {
			DbUtils.closeQuietly(issueCount);
			DbUtils.closeQuietly(ps);
		}
		
        if(comboExists > 0){
        	throw new ValidationException(ErrorMessages.PLAN_TITLE_EXISTS);
        } else {
        	return true;
        }
		
	}
	
	@Override
	public void treatmentPlanDelete(Connection cn, int treatmentPlanID) throws SQLException, ValidationException {
		throwValidationExceptionIfTemplateHolderID(treatmentPlanID);
		
		PreparedStatement ps = null;

		try {
            ps = cn.prepareStatement("DELETE FROM treatment_plan WHERE treatment_plan_id=?");
            ps.setInt(1, treatmentPlanID);

            ps.executeUpdate();
		
		} finally {
			DbUtils.closeQuietly(ps);
	    }

	}
	
	
	
	/** Validating a new Stage title involves checking is there is already a match for the combination of the new title and the userID.
	 * If there is a match then the new title is invalid
	 * @param cn
	 * @param newStage - A Stage object containing at least a title and userID
	 * @return true if valid combination, false throws ValidationException
	 * @throws ValidationException
	 * @throws DatabaseException
	 */
	@Override
	public boolean stageValidateNewTitle(Connection cn, Stage newStage) throws ValidationException, SQLException{
		PreparedStatement ps = null;
        ResultSet stageCount = null;
        int comboExists = 0;

    	try {

			ps = cn.prepareStatement("SELECT COUNT(*) FROM stage WHERE stage_title=? AND stage_treatment_plan_id_fk=? AND stage_user_id_fk=? AND stage_is_template=?");
			ps.setString(1, newStage.getTitle().trim());
			ps.setInt(2, newStage.getTreatmentPlanID());
			ps.setInt(3, newStage.getUserID());
			ps.setBoolean(4, newStage.isTemplate());

			stageCount = ps.executeQuery();

			while (stageCount.next()){
			    comboExists = stageCount.getInt("COUNT(*)");
			}
			
			if(comboExists > 0){
				throw new ValidationException(ErrorMessages.STAGE_TITLE_EXISTS);
			}

        } finally {
			DbUtils.closeQuietly(stageCount);
			DbUtils.closeQuietly(ps);
		}

        
		return true;
	}
	
	/**Validating a new Stage title involves checking is there is already a match for the combination of the new title and the userID. However, since
	 * in case the title wasn't actually changed, need to also exclude any results that have a stageID equal to the stageID of the Stage parameter
	 * @param cn
	 * @param newStage
	 * @return
	 * @throws ValidationException
	 * @throws DatabaseException
	 */
	@Override
	public boolean stageValidateUpdatedTitle(Connection cn, Stage newStage) throws ValidationException, SQLException{
		PreparedStatement ps = null;
        ResultSet stageCount = null;
        int comboExists = 0;
	        
        try {
			ps = cn.prepareStatement("SELECT COUNT(*) FROM stage WHERE stage.stage_title = ? AND stage.stage_treatment_plan_id_fk= ?  AND stage.stage_id != ? AND stage.stage_user_id_fk = ? AND stage_is_template=?");
			ps.setString(1, newStage.getTitle().trim());
			ps.setInt(2, newStage.getTreatmentPlanID());
			ps.setInt(3, newStage.getStageID());
			ps.setInt(4, newStage.getUserID());
			ps.setBoolean(5, newStage.isTemplate());

			stageCount = ps.executeQuery();

			while (stageCount.next()){
			    comboExists = stageCount.getInt("COUNT(*)");
			}

        } finally {
			DbUtils.closeQuietly(stageCount);
			DbUtils.closeQuietly(ps);
		}
        
		if(comboExists > 0){
			throw new ValidationException(ErrorMessages.STAGE_TITLE_EXISTS);
		} else {
			return true;
		}
	}
	
	
	@Override
	public Stage stageCreateBasic(Connection cn, Stage newStage) throws ValidationException, SQLException{
    	PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        
        try {
    		String sql = "INSERT INTO stage (stage_user_id_fk, stage_treatment_plan_id_fk, stage_title, stage_description, stage_completed, client_stage_order, percent_complete, stage_in_progress, stage_is_template, template_id) "
            		+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        	
            ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, newStage.getUserID());
            ps.setInt(2, newStage.getTreatmentPlanID());
            ps.setString(3, newStage.getTitle().trim());
            ps.setString(4, newStage.getDescription());
            ps.setBoolean(5,  newStage.isCompleted());
            ps.setInt(6, newStage.getClientStageOrder());
            ps.setDouble(7, newStage.getPercentComplete());
            ps.setBoolean(8, newStage.isInProgress());
            ps.setBoolean(9, newStage.isTemplate());
            ps.setInt(10, newStage.getTemplateID());

            int success = ps.executeUpdate();
            
            generatedKeys = ps.getGeneratedKeys();
   
            while (generatedKeys.next()){
            	newStage.setStageID(generatedKeys.getInt(1));
            }
        	
        } finally {
        	DbUtils.closeQuietly(generatedKeys);
			DbUtils.closeQuietly(ps);
        }
        
        return newStage;
	}

	
	@Override
	public boolean stageUpdateBasic(Connection cn, Stage stage) throws ValidationException, SQLException{
    	PreparedStatement ps = null;
        int success = 0;
        
        throwValidationExceptionIfTemplateHolderID(stage.getStageID());
        
        if(stage.isTemplate()){  	
        	//as a precaution - make sure the template's order is 0 since all order values for how stage templates are a part of a treatment plan template is kept in their mapping table
        	stage.setClientStageOrder(0);
        }
        
        try {
        		
    		String sql = "UPDATE stage SET stage_treatment_plan_id_fk=?, stage_user_id_fk=?, stage_title=?, stage_description=?, stage_completed=?, client_stage_order=?, percent_complete=?, stage_in_progress=?, stage_is_template=?, template_id=? WHERE stage_id=?";
        	
            ps = cn.prepareStatement(sql);

            ps.setInt(1, stage.getTreatmentPlanID());
            ps.setInt(2, stage.getUserID());
            ps.setString(3, stage.getTitle().trim());
            ps.setString(4, stage.getDescription());
            ps.setBoolean(5, stage.isCompleted());
            ps.setInt(6, stage.getClientStageOrder());
            ps.setDouble(7, stage.getPercentComplete());
            ps.setBoolean(8, stage.isInProgress());
            ps.setBoolean(9, stage.isTemplate());
            ps.setInt(10, stage.getTemplateID());
            ps.setInt(11, stage.getStageID());

            success = ps.executeUpdate();
        	
        } finally {
			DbUtils.closeQuietly(ps);
        }
        
        return success == 1;
	}

	
	public List<Stage> stagesGetCoreList() throws DatabaseException, ValidationException{
		Connection cn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        List<Stage> coreStagesList = new ArrayList<>();
        
        try {
        	cn = getConnection();
        	
        	List<Integer> adminIDList = userGetAdminIDs(cn);
        	
        	String baseStatement = "SELECT * FROM stage WHERE stage_is_template=1 AND stage_user_id_fk in (";
        	
        	String orderByClause = "ORDER BY stage_title";
        	
        	String sql = SqlBuilders.includeMultipleIntParams(baseStatement, adminIDList, orderByClause);

    		ps = cn.prepareStatement(sql);
    		
    		for(int i = 0; i < adminIDList.size(); i++){
    			ps.setInt(i+1, adminIDList.get(i));
    		}
            
            rs = ps.executeQuery();
   
            while (rs.next()){
            	if(rs.getInt("stage_id") != Constants.TEMPLATES_HOLDER_PRIMARY_KEY_ID){// The Stage with id=1 is the Stage that holds all of the Task templates, so should not be returned in this query
            		coreStagesList.add(Stage.loadBasic(cn, rs.getInt("stage_id")));
            	}
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException(ErrorMessages.GENERAL_DB_ERROR);
        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(cn);
        }
        
        return coreStagesList;
	}

	
	@Override
	public Stage stageLoadBasic(Connection cn, int stageID) throws SQLException, ValidationException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        Stage stage = null;
        
        throwValidationExceptionIfTemplateHolderID(stageID);
        
        try {
    		String sql = "SELECT * FROM stage WHERE stage.stage_id =?";
        	
            ps = cn.prepareStatement(sql);
            
            ps.setInt(1, stageID);
            
            rs = ps.executeQuery();
   
            while (rs.next()){
            	List<Task> tasks = new ArrayList<>();
            	List<Task> extraTasks = new ArrayList<>();
            	List<StageGoal> goals = new ArrayList<>();
            	
            	//boolean completed = rs.getInt("stage_completed") == 1;
            	//boolean inProgress = rs.getInt("") == 1;
            	//boolean isTemplate = rs.getInt("stage_is_template") == 1;
            	
            	stage = Stage.getInstance(stageID, rs.getInt("stage_treatment_plan_id_fk"), rs.getInt("stage.stage_user_id_fk"), rs.getString("stage.stage_title"), rs.getString("stage.stage_description"), rs.getInt("stage.client_stage_order"), tasks, extraTasks, rs.getBoolean("stage_completed"), rs.getDouble("percent_complete"), goals, rs.getBoolean("stage_in_progress"), rs.getBoolean("stage_is_template"), rs.getInt("template_id"));
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        //throwValidationExceptionIfNull(stage);
        
        return stage;
	}
	
	//OPTIMIZE instead of calling Task.load() for each record, could change SELECT statement to return all records from Task that match and build each Task inside this method.
	@Override
	public List<Task> stageLoadClientTasks(Connection cn, int stageID) throws SQLException {
		PreparedStatement ps = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        //throwValidationExceptionIfTemplateHolderID(stageID);
        
        try {
            ps = cn.prepareStatement("SELECT task_generic_id FROM task_generic WHERE task_generic_stage_id_fk=? ORDER BY client_task_order");
            ps.setInt(1, stageID);
            

            rs = ps.executeQuery();

            while (rs.next()){
            	tasks.add(Task.load(cn, rs.getInt("task_generic_id")));//TODO change to add to list of taskIDs that is then passed to Task.load(List<Integer>);
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);

        }

        return tasks;
	}

	@Override
	public List<MapStageTaskTemplate> stageUpdateTemplateTasks(Connection cn, int stageID, List<MapStageTaskTemplate> taskTemplates) throws SQLException {
		PreparedStatement ps = null;
        
        
        try {
        	for(MapStageTaskTemplate taskInfo : taskTemplates){
        		ps = cn.prepareStatement("UPDATE stage_template_task_template_maps SET task_generic_template_id_fk=?, stage_template_id_fk=?, template_task_order=? WHERE task_generic_template_id_fk=? and stage_template_id_fk=?;");
                ps.setInt(1,taskInfo.getTaskID());
                ps.setInt(2,stageID);
                ps.setInt(3,taskInfo.getTemplateTaskOrder());
                ps.setInt(4,taskInfo.getTaskID());
                ps.setInt(5,stageID);
                
                //ps.executeUpdate();
                ps.addBatch();
        	}            

            ps.executeBatch();
            
        } finally {
			DbUtils.closeQuietly(ps);
        }

        return taskTemplates;
	}
	
	@Override
	public List<StageGoal> stageLoadGoals(Connection cn, int stageID) throws SQLException, ValidationException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        List<StageGoal> goals = new ArrayList<>();
        
        throwValidationExceptionIfTemplateHolderID(stageID);
        
        try {
            ps = cn.prepareStatement("SELECT * FROM stage_goal WHERE stage_goal_stage_id_fk=?");
            ps.setInt(1, stageID);

            rs = ps.executeQuery();

            while (rs.next()){
            	goals.add(StageGoal.getInstance(rs.getInt("stage_goal_id"), rs.getInt("stage_goal_stage_id_fk"), rs.getString("stage_goal_description")));
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }

        return goals;
	}
	
	@Override
	public void stageDelete(Connection cn, int stageID) throws SQLException, ValidationException{
		throwValidationExceptionIfTemplateHolderID(stageID);
    	PreparedStatement ps = null;
        
    	try{
	        ps = cn.prepareStatement("DELETE FROM stage WHERE stage_id=?");
	        ps.setInt(1, stageID);
	
	        ps.executeUpdate();
    	}finally{
    		DbUtils.closeQuietly(ps);
    	}
	
	}
	
	@Override
	public StageGoal stageGoalCreate(Connection cn, StageGoal stageGoal) throws SQLException, ValidationException {
		PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        
        throwValidationExceptionIfTemplateHolderID(stageGoal.getStageID());
        
        try {
	        	String sql = "INSERT INTO stage_goal (stage_goal_stage_id_fk, stage_goal_description) VALUES (?, ?)";
	        	
	            ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	            
	            ps.setInt(1, stageGoal.getStageID());
	            ps.setString(2, stageGoal.getDescription());
	
	            int success = ps.executeUpdate();
	            
	            generatedKeys = ps.getGeneratedKeys();
	   
	            int goalID = 0;
	            while (generatedKeys.next()){
	            	goalID = generatedKeys.getInt(1);;
	            }
	            
	            stageGoal.setStageGoalID(goalID);

        } finally {
        	DbUtils.closeQuietly(generatedKeys);
			DbUtils.closeQuietly(ps);
        }
        
        return stageGoal;
	}
	
	@Override
	public boolean stageGoalUpdate(Connection cn, StageGoal goal) throws ValidationException, SQLException{
    	PreparedStatement ps = null;
        int success = 0;
        
        throwValidationExceptionIfTemplateHolderID(goal.getStageID());
        
        try {
        		
    		String sql = "UPDATE stage_goal SET stage_goal_stage_id_fk=?, stage_goal_description=? WHERE stage_goal_id=?";
        	
            ps = cn.prepareStatement(sql);

            ps.setInt(1, goal.getStageID());
            ps.setString(2, goal.getDescription());
            ps.setInt(3, goal.getStageGoalID());
            

            success = ps.executeUpdate();
        	
        } finally {
			DbUtils.closeQuietly(ps);
        }
        
        return success == 1;
	}
	
	@Override
	public void stageGoalDelete(Connection cn, int stageGoalID) throws SQLException, ValidationException{
    	PreparedStatement ps = null;
        
    	try{
	        ps = cn.prepareStatement("DELETE FROM stage_goal WHERE stage_goal_id=?");
	        ps.setInt(1, stageGoalID);
	
	        ps.executeUpdate();
    	}finally{
    		DbUtils.closeQuietly(ps);
    	}
	
	}
	

	
	
	@Override
	public List<Task> taskGetCoreList() throws DatabaseException{
		Connection cn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        List<Task> defaultTaskList = new ArrayList<>();
        
        try {
        	cn = getConnection();
        	
        	List<Integer> adminIDList = userGetAdminIDs(cn);
        	
        	String baseStatement = "SELECT * FROM task_generic WHERE task_is_template=1 AND task_generic_user_id_fk in (";
        	
        	String orderByClause = "ORDER BY task_title";
        	
        	String sql = SqlBuilders.includeMultipleIntParams(baseStatement, adminIDList, orderByClause);
        	
    		ps = cn.prepareStatement(sql);
    		
    		for(int i = 0; i < adminIDList.size(); i++){
    			ps.setInt(i+1, adminIDList.get(i));
    		}
            
            rs = ps.executeQuery();
   
            while (rs.next()){
            	defaultTaskList.add(taskGenericLoad(cn, rs.getInt("task_generic_id")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException(ErrorMessages.GENERAL_DB_ERROR);
        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(cn);
        }
        
        return defaultTaskList;
	}
	
	@Override
	public Task taskGenericLoad(Connection cn, int taskID) throws SQLException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        Task task = null;
        Map<Integer, Keyword> keywords = new HashMap<>();
        try {
    		String sql = "SELECT task_generic.*, keyword.keyword_id, keyword.keyword, keyword.keyword_user_id_fk "
    				+ "FROM keyword RIGHT JOIN (task_generic LEFT JOIN task_keyword_maps "
    				+ "ON task_generic.task_generic_id = task_keyword_maps.task_generic_id_fk) "
    				+ "ON keyword.keyword_id = task_keyword_maps.task_keyword_id_fk "
    				+ "WHERE task_generic.task_generic_id =?";
        	
            ps = cn.prepareStatement(sql);
            
            ps.setInt(1, taskID);
            
            rs = ps.executeQuery();
   
            while (rs.next()){
            	if(rs.getString("keyword.keyword_id")!=null){
            		keywords.put(rs.getInt("keyword.keyword_id"), new Keyword(rs.getInt("keyword.keyword_id"),rs.getString("keyword.keyword"),rs.getInt("keyword.keyword_user_id_fk")));
            	}
            	
            	if(rs.isLast()){
            		Timestamp timestamp = rs.getTimestamp("task_generic.task_date_completed");
                	LocalDateTime dateCompleted = convertTimestampToLocalDateTime(timestamp);
                	
                	task = TaskGeneric.getInstanceFull(rs.getInt("task_generic.task_generic_id"), rs.getInt("task_generic.task_generic_stage_id_fk"), rs.getInt("task_generic.task_generic_user_id_fk"), 
                			rs.getInt("task_generic.task_generic_task_type_id_fk"), rs.getInt("task_generic.parent_task_id"), rs.getString("task_generic.task_title"), 
                			rs.getString("task_generic.instructions"), rs.getString("task_generic.resource_link"), rs.getBoolean("task_generic.task_completed"), 
                			dateCompleted, rs.getInt("client_task_order"), rs.getBoolean("is_extra_task"), rs.getBoolean("task_generic.task_is_template"), 
                			rs.getInt("task_generic.task_template_id"), rs.getInt("task_generic.client_repetition"), keywords);
            	
                	//task.setKeywords(keywords);
            	}
            	
            	
            	
            	
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        return task;
	}
	
	@Override
	public void taskTwoTextBoxesCreateAdditionalData(Connection cn, TaskTwoTextBoxes twoTextBoxesTask) throws SQLException{
		PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        
        try {
        	String sql = "INSERT INTO task_two_textboxes (task_generic_id, extra_text_label_1, extra_text_value_1, extra_text_label_2, extra_text_value_2) "
        			+ "VALUES (?, ?, ?, ?, ?)";
        	
            ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, twoTextBoxesTask.getTaskID());
            ps.setString(2, twoTextBoxesTask.getExtraTextLabel1());
            ps.setString(3, twoTextBoxesTask.getExtraTextValue1());
            ps.setString(4, twoTextBoxesTask.getExtraTextLabel2());
            ps.setString(5, twoTextBoxesTask.getExtraTextValue2());


            int success = ps.executeUpdate();
            
            generatedKeys = ps.getGeneratedKeys();
   
            while (generatedKeys.next()){
            	twoTextBoxesTask.setTaskID(generatedKeys.getInt(1));
            }

        } finally {
        	DbUtils.closeQuietly(generatedKeys);
			DbUtils.closeQuietly(ps);
        }
		

	}
	
	@Override
	public boolean taskTwoTextBoxesUpdateAdditionalData(Connection cn, TaskTwoTextBoxes twoTextBoxesTask) throws SQLException, ValidationException {
    	PreparedStatement ps = null;
        int success = 0;
        
        try {
        	
    		String sql = "UPDATE task_two_textboxes SET extra_text_label_1=?, extra_text_value_1=?, extra_text_label_2=?, extra_text_value_2=? WHERE task_generic_id=?";
        	
            ps = cn.prepareStatement(sql);
            
            ps.setString(1, twoTextBoxesTask.getExtraTextLabel1());
            ps.setString(2, twoTextBoxesTask.getExtraTextValue1());
            ps.setString(3, twoTextBoxesTask.getExtraTextLabel2());
            ps.setString(4, twoTextBoxesTask.getExtraTextValue2());
            ps.setInt(5, twoTextBoxesTask.getTaskID()); 

            success = ps.executeUpdate();   	

        } finally {
			DbUtils.closeQuietly(ps);
        }
        
        return success == 1;
	}
	
	@Override
	public Task taskTwoTextBoxesLoadAdditionalData(Connection cn, TaskGeneric genericTask) throws SQLException {
		PreparedStatement ps = null;
        ResultSet rs = null;
        TaskTwoTextBoxes task = null;
        
        try {
    		String sql = "SELECT * FROM task_two_textboxes WHERE task_two_textboxes.task_generic_id =?";
        	
            ps = cn.prepareStatement(sql);
            
            ps.setInt(1, genericTask.getTaskID());
            
            rs = ps.executeQuery();
   
            while (rs.next()){
            	task = TaskTwoTextBoxes.addDataToGenericTask(genericTask, rs.getString("extra_text_label_1"), rs.getString("extra_text_value_1"), rs.getString("extra_text_label_2"), rs.getString("extra_text_value_2"));
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        return task;
	}
	

	@Override
	public void taskTwoTextBoxesDeleteAdditionalData(Connection cn, int taskID) throws SQLException {
		PreparedStatement ps = null;

		try {
            ps = cn.prepareStatement("DELETE FROM task_two_textboxes WHERE task_generic_id=?");
            ps.setInt(1, taskID);

            ps.executeUpdate();

		} finally {
			DbUtils.closeQuietly(ps);
	    }

		
	}
	
	@Override
	public boolean taskGenericUpdate(Connection cn, Task taskToUpdate) throws SQLException {
    	PreparedStatement ps = null;
        int success = 0;
        
        if(taskToUpdate.isTemplate()){  	
        	//as a precaution - make sure the template's order is 0 since all order values for how task templates are a part of a stage template is kept in their mapping table
        	taskToUpdate.setClientTaskOrder(0);
        }
        
        try {

	    		String sql = "UPDATE task_generic SET task_generic_task_type_id_fk=?, task_generic_stage_id_fk=?, task_generic_user_id_fk=?, parent_task_id=?, task_title=?, instructions=?, resource_link=?, "
	    				+ "task_completed=?, task_date_completed=?, client_task_order=?, is_extra_task=?, task_is_template=?, task_template_id=?, client_repetition=? WHERE task_generic_id=?";
	        	
	            ps = cn.prepareStatement(sql);
	            
	            ps.setInt(1, taskToUpdate.getTaskTypeID());
	            ps.setInt(2, taskToUpdate.getStageID());
	            ps.setInt(3, taskToUpdate.getUserID());
	            ps.setInt(4, taskToUpdate.getParentTaskID());
	            ps.setString(5, taskToUpdate.getTitle().trim());
	            ps.setString(6,  taskToUpdate.getInstructions());
	            ps.setString(7, taskToUpdate.getResourceLink());
	            ps.setBoolean(8, taskToUpdate.isCompleted());
	            ps.setTimestamp(9, convertLocalTimeDateToTimstamp(taskToUpdate.getDateCompleted()));
	            ps.setInt(10, taskToUpdate.getClientTaskOrder());
	            ps.setBoolean(11, taskToUpdate.isExtraTask());
	            ps.setBoolean(12, taskToUpdate.isTemplate());
	            ps.setInt(13, taskToUpdate.getTemplateID());
	            ps.setInt(14, taskToUpdate.getClientRepetition());
	            ps.setInt(15, taskToUpdate.getTaskID());
	
	            success = ps.executeUpdate();

        } finally {
			DbUtils.closeQuietly(ps);
        }
        
        return success == 1;
	}
	
	//TODO - bug fix - either create different validate method for updates so doesn't throw TaskTitleExists exception when updating fields of a task without changing the title or add logic in method below to do this
	@Override
	public boolean taskValidate(Connection cn, Task newTask) throws ValidationException, SQLException{
		PreparedStatement ps = null;
        ResultSet rsStageCount = null;
        int comboExists = 0;
	        
        try {
			ps = cn.prepareStatement("SELECT COUNT(*) FROM task_generic WHERE (task_generic.task_title=? AND task_generic_stage_id_fk=? AND task_generic_id!=?)");
			ps.setString(1, newTask.getTitle().trim());
			ps.setInt(2, newTask.getStageID());
			ps.setInt(3, newTask.getTaskID());

			rsStageCount = ps.executeQuery();

			while (rsStageCount.next()){
			    comboExists = rsStageCount.getInt("COUNT(*)");
			}

        } finally {
			DbUtils.closeQuietly(rsStageCount);
			DbUtils.closeQuietly(ps);
		}
        
		if(comboExists > 0){
			throw new ValidationException(ErrorMessages.TASK_TITLE_EXISTS_FOR_STAGE);
		} else {
			return true;
		}
		
	}
	
	/**Inserts a Generic Task template into the database.  Since it is a template, it does not insert for the 
	 * fields: task_id, task_stage_id_fk, task_date_completed, or parent_task_id, and it sets is_extra_task = 0(false) and task_is_template = 1(true)
	 * @param cn
	 * @param newTask - Task object to be inserted.
	 * @return
	 * @throws DatabaseException
	 */
	@Override
	public Task taskGenericCreate(Connection cn, Task newTask) throws SQLException{
		PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        
        try {
        	String sql = "INSERT INTO task_generic (task_generic_task_type_id_fk, task_generic_stage_id_fk, task_generic_user_id_fk, parent_task_id, task_title, "
        			+ "instructions, resource_link, task_completed, task_date_completed, client_task_order, is_extra_task, task_is_template, task_template_id, client_repetition) "
    				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        	
            ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, newTask.getTaskTypeID());

            ps.setInt(2, newTask.getStageID());
            ps.setInt(3, newTask.getUserID());
            ps.setInt(4, newTask.getParentTaskID());
            ps.setString(5, newTask.getTitle().trim());
            ps.setString(6, newTask.getInstructions());
            ps.setString(7, newTask.getResourceLink());
            ps.setBoolean(8, newTask.isCompleted());
            ps.setTimestamp(9, convertLocalTimeDateToTimstamp(newTask.getDateCompleted()));
            ps.setInt(10, newTask.getClientTaskOrder());
            ps.setBoolean(11, newTask.isExtraTask());
            ps.setBoolean(12, newTask.isTemplate());
            ps.setInt(13, newTask.getTemplateID());
            ps.setInt(14, newTask.getClientRepetition());

            int success = ps.executeUpdate();
            
            generatedKeys = ps.getGeneratedKeys();
   
            while (generatedKeys.next()){
            	newTask.setTaskID(generatedKeys.getInt(1));
            }
        	
        } finally {
        	DbUtils.closeQuietly(generatedKeys);
			DbUtils.closeQuietly(ps);
        }
		
		return newTask;
	}
	
	@Override
	public void taskDelete(Connection cn, int taskID) throws SQLException {
		PreparedStatement ps = null;

		try {
            ps = cn.prepareStatement("DELETE FROM task_generic WHERE task_generic_id=?");
            ps.setInt(1, taskID);

            ps.executeUpdate();

		} finally {
			DbUtils.closeQuietly(ps);
	    }

	}
	
	public Map<Integer, String> taskTypesLoad() throws DatabaseException{
		Connection cn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        HashMap<Integer, String> taskTypeMap = new HashMap<>();
        
        try {
        	cn = getConnection();
        	String sql = "SELECT * FROM task_type;";
    		ps = cn.prepareStatement(sql);
            rs = ps.executeQuery();
   
            while (rs.next()){
            	taskTypeMap.put(rs.getInt("task_type_id"), rs.getString("task_type"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException(ErrorMessages.GENERAL_DB_ERROR);
        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(cn);
        }
		
		return taskTypeMap;
	}

	@Override
	public TreatmentIssue treatmentIssueCreate(Connection cn, TreatmentIssue treatmentIssue, int userID) throws ValidationException, SQLException{
    	PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        
        try {
        	
        	if(treatmentIssueValidateNewName(cn, treatmentIssue.getTreatmentIssueName(), userID)){
        		String sql = "INSERT INTO treatment_issue (issue, treatment_issue_user_id_fk) "
                		+ "VALUES (?, ?)";
            	
                ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                
                ps.setString(1, treatmentIssue.getTreatmentIssueName().trim());
                ps.setInt(2, treatmentIssue.getUserID());

                int success = ps.executeUpdate();
                
                generatedKeys = ps.getGeneratedKeys();
       
                while (generatedKeys.next()){
                	treatmentIssue.setTreatmentIssueID(generatedKeys.getInt(1));
                }
        	}

        } finally {
        	DbUtils.closeQuietly(generatedKeys);
			DbUtils.closeQuietly(ps);
        }
        
        return treatmentIssue;
	}
	
	@Override
	public boolean treatmentIssueUpdate(Connection cn, TreatmentIssue issue) throws ValidationException, SQLException{
    	PreparedStatement ps = null;
        int success = 0;
        
        try {
        		
    		String sql = "UPDATE treatment_issue SET treatment_issue_user_id_fk=?, issue=? WHERE treatment_issue_id=?";
        	
            ps = cn.prepareStatement(sql);

            ps.setInt(1, issue.getUserID());
            ps.setString(2, issue.getTreatmentIssueName());
            ps.setInt(3, issue.getTreatmentIssueID());
            

            success = ps.executeUpdate();
        	
        } finally {
			DbUtils.closeQuietly(ps);
        }
        
        return success == 1;
	}
	
	@Override
	public void treatmentIssueDelete(Connection cn, int treatmentIssueID) throws SQLException, ValidationException{
    	PreparedStatement ps = null;
        
    	try{
	        ps = cn.prepareStatement("DELETE FROM treatment_issue WHERE treatment_issue_id=?");
	        ps.setInt(1, treatmentIssueID);
	
	        ps.executeUpdate();
    	}finally{
    		DbUtils.closeQuietly(ps);
    	}
	
	}
	
	/**
	 * Checks if there is an existing combination of treatment issue name and userID in the database.
	 * @param cn Database connection
	 * @param issueName Treatment Issue title of new issue that user wants to create
	 * @param userID id of the user
	 * @return true if the the combination is valid and does not exist in the database. false if the combination exists and is therefore invalid.
	 * @throws SQLException
	 * @throws ValidationException 
	 * @throws DatabaseException
	 */
	@Override
	public boolean treatmentIssueValidateNewName(Connection cn, String issueName, int userID) throws ValidationException, SQLException{
    	PreparedStatement ps = null;
        ResultSet issueCount = null;
        int comboExists = 0;
        
        try {

            ps = cn.prepareStatement("SELECT COUNT(*)  FROM treatment_issue WHERE ((treatment_issue.issue=?) AND (treatment_issue.treatment_issue_user_id_fk=?))");
            ps.setString(1, issueName.trim());
            ps.setInt(2, userID);

            issueCount = ps.executeQuery();


            while (issueCount.next()){
                comboExists = issueCount.getInt("COUNT(*)");
            }

        } finally {
			DbUtils.closeQuietly(issueCount);
			DbUtils.closeQuietly(ps);
		}

        if(comboExists > 0){
        	throw new ValidationException(ErrorMessages.ISSUE_NAME_EXISTS);
        } else {
            return true;
        }
    }
	
	@Override
	public boolean treatmentIssueValidateUpdatedName(Connection cn, TreatmentIssue issue) throws ValidationException, SQLException{
    	PreparedStatement ps = null;
        ResultSet issueCount = null;
        int comboExists = 0;
        
        try {

            ps = cn.prepareStatement("SELECT COUNT(*)  FROM treatment_issue WHERE ((treatment_issue.issue=?) AND (treatment_issue.treatment_issue_user_id_fk=?)"
            		+ "AND (treatment_issue_id != ?))");
            ps.setString(1, issue.getTreatmentIssueName().trim());
            ps.setInt(2, issue.getUserID());
            ps.setInt(3, issue.getTreatmentIssueID());

            issueCount = ps.executeQuery();


            while (issueCount.next()){
                comboExists = issueCount.getInt("COUNT(*)");
            }

        } finally {
			DbUtils.closeQuietly(issueCount);
			DbUtils.closeQuietly(ps);
		}

        if(comboExists > 0){
        	throw new ValidationException(ErrorMessages.ISSUE_NAME_EXISTS);
        } else {
            return true;
        }
    }

	//XXX I need to move this logic into a model and from there call treatmentIssueGetListByUserID()
	@Override
	public ArrayList<TreatmentIssue> treatmentIssueGetCoreList() throws DatabaseException{
		Connection cn = null;
		ArrayList<TreatmentIssue> issues = new ArrayList<>();
		
		try{
			cn = getConnection();
			List<Integer> userIDs = userGetAdminIDs(cn);
			for(int adminUserID : userIDs){
				issues.addAll(treatmentIssueGetListByUserID(cn, adminUserID));
			}
		}catch (SQLException e){
			e.printStackTrace();
			throw new DatabaseException(ErrorMessages.GENERAL_DB_ERROR);
		} finally {
			DbUtils.closeQuietly(cn);
		}

		return issues;
	}


    @Override
	public ArrayList<TreatmentIssue> treatmentIssueGetListByUserID(Connection cn, int userID) throws SQLException{
    	
    	PreparedStatement ps = null;
        ResultSet rs = null;
        
        ArrayList<TreatmentIssue> issues = new ArrayList<>();
        
        try {
        	
        	String sql = "SELECT treatment_issue.treatment_issue_id, treatment_issue.issue, user.user_id "
            		+ "FROM user INNER JOIN treatment_issue ON user.user_id = treatment_issue.treatment_issue_user_id_fk "
            		+ "WHERE user.user_id=? ORDER BY issue";
        	
            ps = cn.prepareStatement(sql);
            ps.setInt(1, userID);

            rs = ps.executeQuery();
   
            while (rs.next()){
            	TreatmentIssue issue = new TreatmentIssue(rs.getInt("treatment_issue_id"), rs.getString("issue"));
            	issues.add(issue);
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        return issues;
    }
    
    @Override
	public List<MapStageTaskTemplate> mapStageTaskTemplateLoad(Connection cn, int stageID) throws SQLException, ValidationException{
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	List<MapStageTaskTemplate> stageTaskDetailMap = new ArrayList<>();
        
        throwValidationExceptionIfTemplateHolderID(stageID);
        
        try {
            ps = cn.prepareStatement("SELECT * FROM stage_template_task_template_maps WHERE stage_template_id_fk=? ORDER BY template_task_order");
            ps.setInt(1, stageID);

            rs = ps.executeQuery();

            while (rs.next()){
            	MapStageTaskTemplate detail = new MapStageTaskTemplate(rs.getInt("stage_template_id_fk"), rs.getInt("task_generic_template_id_fk"), rs.getInt("template_task_order"), rs.getInt("template_repetitions"));
            	stageTaskDetailMap.add(detail);
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
    	
    	return stageTaskDetailMap;
	
	}
    
    @Override
	public void mapStageTaskTemplateCreate(Connection cn, MapStageTaskTemplate map) throws SQLException{
		PreparedStatement ps = null;
        
        try {
        	String sql = "INSERT INTO stage_template_task_template_maps (task_generic_template_id_fk, stage_template_id_fk, template_task_order, template_repetitions) "
        			+ "VALUES (?, ?, ?, ?)";

        	
            ps = cn.prepareStatement(sql);
            
            ps.setInt(1, map.getTaskID());
            ps.setInt(2, map.getStageID());
            ps.setInt(3, map.getTemplateTaskOrder());
            ps.setInt(4, map.getTemplateTaskRepetitions());

            int success = ps.executeUpdate();
 	
        } finally {
			DbUtils.closeQuietly(ps);
        }

	}
    
    @Override
	public void mapStageTaskTemplateUpdate(Connection cn, MapStageTaskTemplate stageTaskTemplateMap) throws SQLException{
		PreparedStatement ps = null;
        
        try {
        	String sql = "UPDATE stage_template_task_template_maps SET template_task_order = ?, template_repetitions = ?"
        			+ " WHERE stage_template_id_fk = ? AND task_generic_template_id_fk = ?";

        	
            ps = cn.prepareStatement(sql);
            
            ps.setInt(1, stageTaskTemplateMap.getTemplateTaskOrder());
            ps.setInt(2, stageTaskTemplateMap.getTemplateTaskRepetitions());
            ps.setInt(3, stageTaskTemplateMap.getStageID());
            ps.setInt(4, stageTaskTemplateMap.getTaskID());

            int success = ps.executeUpdate();
 	
        } finally {
			DbUtils.closeQuietly(ps);
        }

	}
    
    @Override
	public boolean mapStageTaskTemplateValidate(Connection cn, int taskTemplateID, int stageTemplateID) throws ValidationException, SQLException{
		PreparedStatement ps = null;
        ResultSet rsStageCount = null;
        int comboExists = 0;
	        
        try {
			ps = cn.prepareStatement("SELECT COUNT(*) FROM stage_template_task_template_maps WHERE task_generic_template_id_fk=? AND stage_template_id_fk=?");
			ps.setInt(1, taskTemplateID);
			ps.setInt(2, stageTemplateID);

			rsStageCount = ps.executeQuery();

			while (rsStageCount.next()){
			    comboExists = rsStageCount.getInt("COUNT(*)");
			}

        } finally {
			DbUtils.closeQuietly(rsStageCount);
			DbUtils.closeQuietly(ps);
		}
        
		if(comboExists > 0){
			throw new ValidationException(ErrorMessages.STAGE_CONTAINS_TASK_TEMPLATE);
		} else {
			return true;
		}
		
	}
    
    @Override
	public void mapStageTaskTemplateDelete(Connection cn, int taskID, int stageID) throws SQLException {
		PreparedStatement ps = null;

		try {
            ps = cn.prepareStatement("DELETE FROM stage_template_task_template_maps WHERE task_generic_template_id_fk=? AND stage_template_id_fk=?");
            ps.setInt(1, taskID);
            ps.setInt(2, stageID);

            ps.executeUpdate();

		} finally {
			DbUtils.closeQuietly(ps);
	    }

	}
    
    //TODO add an updateMapsTaskStage method to update templateTaskOrder that takes List<Task> as arg and loops through updating order
    
    @Override
	public List<MapTreatmentPlanStageTemplate> mapTreatmentPlanStageTemplateLoad(Connection cn, int treatmentPlanID) throws SQLException, ValidationException{
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	List<MapTreatmentPlanStageTemplate> planStageDetailMap = new ArrayList<>();
        
        throwValidationExceptionIfTemplateHolderID(treatmentPlanID);
        
        try {
        	ps = cn.prepareStatement("SELECT * FROM treatment_plan_template_stage_template_maps WHERE treatment_plan_template_id_fk=? ORDER BY template_stage_order");
            ps.setInt(1, treatmentPlanID);
            

            rs = ps.executeQuery();
            
            while (rs.next()){
            	MapTreatmentPlanStageTemplate detail = new MapTreatmentPlanStageTemplate(rs.getInt("treatment_plan_template_id_fk"), rs.getInt("stage_template_id_fk"), rs.getInt("template_stage_order"));
            	planStageDetailMap.add(detail);
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
    	
    	return planStageDetailMap;

	}
    
    @Override
	public void mapTreatmentPlanStageTemplateCreate(Connection cn, MapTreatmentPlanStageTemplate mapPlanStageTemplate) throws SQLException{
		PreparedStatement ps = null;
        
        try {
        	String sql = "INSERT INTO treatment_plan_template_stage_template_maps (stage_template_id_fk, treatment_plan_template_id_fk, template_stage_order) "
        			+ "VALUES (?, ?, ?)";

        	
            ps = cn.prepareStatement(sql);
            
            ps.setInt(1, mapPlanStageTemplate.getStageID());
            ps.setInt(2, mapPlanStageTemplate.getTreatmentPlanID());
            ps.setInt(3, mapPlanStageTemplate.getTemplateStageOrder());

            int success = ps.executeUpdate();
 	
        } finally {
			DbUtils.closeQuietly(ps);
        }

	}
    
    @Override
	public boolean mapTreatmentPlanStageTemplateValidate(Connection cn, int stageTemplateID, int treatmentPlanTemplateID) throws ValidationException, SQLException{
		PreparedStatement ps = null;
        ResultSet rsStageCount = null;
        int comboExists = 0;
	        
        try {
			ps = cn.prepareStatement("SELECT COUNT(*) FROM treatment_plan_template_stage_template_maps WHERE stage_template_id_fk=? AND treatment_plan_template_id_fk=?");
			ps.setInt(1, stageTemplateID);
			ps.setInt(2, treatmentPlanTemplateID);

			rsStageCount = ps.executeQuery();

			while (rsStageCount.next()){
			    comboExists = rsStageCount.getInt("COUNT(*)");
			}

        } finally {
			DbUtils.closeQuietly(rsStageCount);
			DbUtils.closeQuietly(ps);
		}
        
		if(comboExists > 0){
			throw new ValidationException(ErrorMessages.PLAN_CONTAINS_STAGE_TEMPLATE);
		} else {
			return true;
		}
		
	}
    
    @Override
	public void mapTreatmentPlanStageTemplateUpdate(Connection cn, MapTreatmentPlanStageTemplate mapPlanStageTemplate) throws SQLException {
    	PreparedStatement ps = null;
        
        try {
        	String sql = "UPDATE treatment_plan_template_stage_template_maps SET template_stage_order=? WHERE stage_template_id_fk=? and treatment_plan_template_id_fk =?;";

        	
            ps = cn.prepareStatement(sql);
            
            ps.setInt(1, mapPlanStageTemplate.getTemplateStageOrder());
            ps.setInt(2, mapPlanStageTemplate.getStageID());
            ps.setInt(3, mapPlanStageTemplate.getTreatmentPlanID());
            

            int success = ps.executeUpdate();
 	
        } finally {
			DbUtils.closeQuietly(ps);
        }
		
	}
    
    @Override
	public void mapTreatmentPlanStageTemplateDelete(Connection cn, int stageID, int treatmentPlanID) throws SQLException {
		PreparedStatement ps = null;

		try {
            ps = cn.prepareStatement("DELETE FROM treatment_plan_template_stage_template_maps WHERE stage_template_id_fk=? AND treatment_plan_template_id_fk=?");
            ps.setInt(1, stageID);
            ps.setInt(2, treatmentPlanID);

            ps.executeUpdate();

		} finally {
			DbUtils.closeQuietly(ps);
	    }

	}
    
    @Override
    public Map<Integer, Keyword> keywordCoreMembersLoad(Connection cn) throws SQLException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        Map<Integer, Keyword> keywordMap = new LinkedHashMap<>();
        
        
        try {
        	List<Integer> adminIDList = userGetAdminIDs(cn);
        	
        	String baseStatement = "SELECT * FROM keyword WHERE keyword_user_id_fk in (";
        	
        	String orderByClause = "ORDER BY keyword";
        	
        	String sql = SqlBuilders.includeMultipleIntParams(baseStatement, adminIDList, orderByClause);
        	
    		ps = cn.prepareStatement(sql);
    		
    		for(int i = 0; i < adminIDList.size(); i++){
    			ps.setInt(i+1, adminIDList.get(i));
    		}
            
            rs = ps.executeQuery();
            
            while (rs.next()){
            	Keyword keyword = new Keyword(rs.getInt("keyword_id"), rs.getString("keyword"), rs.getInt("keyword_user_id_fk")); //here build object with constructor or static factory method 
            	keywordMap.put(rs.getInt("keyword_id"), keyword);
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        //throwValidationExceptionIfNull(stage);
        
        return keywordMap;
    	
    }
    
    @Override
    public Keyword keywordCreate(Connection cn, Keyword keyword) throws SQLException{
    	PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        
        try {
    		String sql = "INSERT INTO keyword (keyword, keyword_user_id_fk) "
            		+ "VALUES (?, ?)";
        	
            ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            /* set the prepared statement arguments*/
            ps.setString(1, keyword.getKeyword());
            ps.setInt(2, keyword.getUserID());

            int success = ps.executeUpdate();
            
            generatedKeys = ps.getGeneratedKeys();
   
            while (generatedKeys.next()){
            	keyword.setKeywordID(generatedKeys.getInt(1));
            }
        	
        } finally {
        	DbUtils.closeQuietly(generatedKeys);
			DbUtils.closeQuietly(ps);
        }
        
        return keyword;
    }
    
    @Override
    public boolean keywordUpdate(Connection cn, Keyword keyword) throws SQLException{
    	PreparedStatement ps = null;
        int success = 0;
        
        try {
        		
    		String sql = "UPDATE keyword SET keyword=?, keyword_user_id_fk=? WHERE keyword_id=?";
        	
            ps = cn.prepareStatement(sql);

            ps.setInt(1, keyword.getKeywordID());
            ps.setString(2, keyword.getKeyword());
            ps.setInt(3, keyword.getUserID());
            
            success = ps.executeUpdate();
        	
        } finally {
			DbUtils.closeQuietly(ps);
        }
        
        return success == 1;
    	
    }
    
    @Override
    public void keywordDelete(Connection cn, int keywordID) throws SQLException{
    	PreparedStatement ps = null;
        
    	try{
	        ps = cn.prepareStatement("DELETE FROM keyword WHERE keyword_id=?");
	        ps.setInt(1, keywordID);
	
	        ps.executeUpdate();
    	}finally{
    		DbUtils.closeQuietly(ps);
    	}
    }
        
    @Override
    public boolean keywordTaskMapCreate(Connection cn, int taskID, int keywordID) throws SQLException{
    	PreparedStatement ps = null;
        ResultSet rs = null;
        int success = 0;
        try {
    		String sql = "INSERT INTO task_keyword_maps (task_generic_id_fk, task_keyword_id_fk) "
            		+ "VALUES (?, ?)";
        	
            ps = cn.prepareStatement(sql);
            
            /* set the prepared statement arguments*/
            ps.setInt(1, taskID);
            ps.setInt(2, keywordID);

            success = ps.executeUpdate();
	
        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        return success==1;
    }
    
    @Override
    public void keywordTaskMapDelete(Connection cn, int taskID, int keywordID) throws SQLException{
    	PreparedStatement ps = null;
        
    	try{
	        ps = cn.prepareStatement("DELETE FROM task_keyword_maps WHERE task_generic_id_fk=? AND task_keyword_id_fk=?");
	        ps.setInt(1, taskID);
	        ps.setInt(2, keywordID);
	        
	        ps.executeUpdate();
    	}finally{
    		DbUtils.closeQuietly(ps);
    	}
    }
    
    @Override
	public List<Affirmation> getAllAffirmations(Connection cn) throws SQLException {
		PreparedStatement ps = null;
        ResultSet rs = null;
        
        List<Affirmation> affirmations = new ArrayList<>();
        
        try {
        	
        	String sql = "SELECT * from affirmations";
        	
            ps = cn.prepareStatement(sql);

            rs = ps.executeQuery();
   
            while (rs.next()){
            	affirmations.add(new Affirmation(rs.getInt("affirmation_id"), rs.getString("affirmation"), rs.getInt("affirmation_user_id_fk")));
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        return affirmations;

	}

	@Override
	public Affirmation affirmationCreate(Connection cn, Affirmation affirmation) throws SQLException {
		PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        
        try {
        	

    		String sql = "INSERT INTO affirmations (affirmation, affirmation_user_id_fk) "
            		+ "VALUES (?, ?)";
        	
            ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setString(1, affirmation.getAffirmation().trim());
            ps.setInt(2, affirmation.getUserID());

            int success = ps.executeUpdate();
            
            generatedKeys = ps.getGeneratedKeys();
   
            while (generatedKeys.next()){
            	affirmation.setAffirmationID(generatedKeys.getInt(1));
            }
        	

        } finally {
        	DbUtils.closeQuietly(generatedKeys);
			DbUtils.closeQuietly(ps);
        }
        
        return affirmation;
	}

	@Override
	public List<LoginHistory> getLoginHistory(Connection cn, int userID) throws SQLException {
		PreparedStatement ps = null;
        ResultSet rs = null;
        
        List<LoginHistory> loginHx = new ArrayList<>();
        
        try {
        	
        	String sql = "SELECT * from login_history";
        	
            ps = cn.prepareStatement(sql);

            rs = ps.executeQuery();
   
            while (rs.next()){
            	Timestamp timestamp = rs.getTimestamp("date_invited");
            	LocalDateTime dateLogin = convertTimestampToLocalDateTime(timestamp);
            	
            	loginHx.add(new LoginHistory(rs.getInt("login_history_id"), rs.getInt("login_history_user_id_fk"), dateLogin));
            }

        } finally {
        	DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
        }
        
        return loginHx;

	}

	@Override
	public void loginHistoryCreate(Connection cn, LoginHistory loginHx) throws SQLException {
		PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        
        try {
        	
        	Timestamp timestamp = convertLocalTimeDateToTimstamp(loginHx.getLoginDateTime());

    		String sql = "INSERT INTO login_history (login_history_user_id_fk, login_datetime) "
            		+ "VALUES (?, ?)";
        	
            ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setInt(1, loginHx.getUserID());
            ps.setTimestamp(2, timestamp);

            int success = ps.executeUpdate();
            
            generatedKeys = ps.getGeneratedKeys();
   
            while (generatedKeys.next()){
            	loginHx.setLoginHistoryID(generatedKeys.getInt(1));
            }
        	

        } finally {
        	DbUtils.closeQuietly(generatedKeys);
			DbUtils.closeQuietly(ps);
        }
        
	}
	
	@Override
    public void loginHistoryDeleteOldEntries(Connection cn, int userID, LocalDateTime deleteBeforeThisDate) throws SQLException{
    	PreparedStatement ps = null;
        
    	try{
    		
    		Timestamp timestamp = convertLocalTimeDateToTimstamp(deleteBeforeThisDate);

	        ps = cn.prepareStatement("DELETE FROM login_history WHERE login_history_user_id_fk=? AND login_datetime<?");
	        ps.setInt(1, userID);
	        ps.setTimestamp(2, timestamp);
	
	        ps.executeUpdate();
    	}finally{
    		DbUtils.closeQuietly(ps);
    	}
    }

	
	/*********************************************************************
	 * Class Utility Methods
	 ********************************************************************/
	 
	private Timestamp convertLocalTimeDateToTimstamp(LocalDateTime ldt){
    	Timestamp timestamp = null;
    	
        if(ldt != null){
        	timestamp = Timestamp.valueOf(ldt);
        }
        
        return timestamp; 
    }

    private LocalDateTime convertTimestampToLocalDateTime(Timestamp timestamp){
    	LocalDateTime ldt = null;
    	if(timestamp != null){
    		ldt = timestamp.toLocalDateTime();
    	}
    	return ldt;
    }



}
