/**
 * Bain, Brandon - bdb7305@rit.edu
 * Benkleman, Adam - arb2941@rit.edu
 * Bhakta, Aisha - atb6376@rit.edu
 * Ismaili, Shpend - si4778@rit.edu
 * Nura, Leon - ln3855@rit.edu
 * 
 * Project - Faculty Research Database
 * March, 31, 2025
 */


// Load JDBC core library
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;

public class FacultyResearchDataLayer_Group1{
	// - Benkleman, Adam

	// Attributes
	private Connection conn;
	private PreparedStatement prepStmt;
	private Statement stmt;
	private String sql;
	private ResultSet rs;
	public String userType;
	public String username; // Added username field to store current username

	/* JDBC Type 4 Driver */
    // Set up device driver
	final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

	/* Define Data Source */
	static String url = "jdbc:mysql://localhost/";


	/**
	 * - Benkleman, Adam
	 * default constructor
	 */
	public FacultyResearchDataLayer_Group1(String database) {
		url += database;
	}


	/**
	 * - Benkleman, Adam
	 * use the default driver to connect
	 * and attempt to connect to the database
	 */
	public boolean connect(String user, String pass){
		// Load a driver
		try {
	 		Class.forName(DEFAULT_DRIVER);
		}
		catch (ClassNotFoundException cnfe) {
	    	return false;
		}

		String userName = user.isEmpty() ? "root" : user; // edited for conciseness
        String password = pass.isEmpty() ? "student" : pass;

		// String userName = user;
		// if(userName == "") { // no username entered
		// 	userName = "root"; // default username
		// }
		// String password = pass;
		// if(password == "") { // no password entered
		// 	password = "student"; // default password
		// }

		// Create a connection
		try {
         url = url + "?serverTimezone=UTC"; // compatibility with MacOS
         conn = DriverManager.getConnection(url, userName, password);
		}
		catch (SQLException se) {
			return false;
		}
		return true; // Driver loaded and Connection created
	}


	/**
	 * - Benkleman, Adam
	 * close the connection to the database
	 */
	public boolean close() {
		try {
			if(conn != null) conn.close();
			if(prepStmt != null) prepStmt.close();
			if(stmt != null) stmt.close();
		}
		catch(Exception e) {
			return false;
		}
		return true; // successfully closed connection and statement
	}


	/**
	 * Start Transaction
	 * - Benkleman, Adam
	 * @return boolean - success(true) or fail(false)
	 */
	public boolean startTransaction() {
		try {
			conn.setAutoCommit(false);
			return true;
		}
		catch (SQLException ex) {
			// Error starting transaction
			return false;
		}
	}

	/**
	 * Rollback Transaction
	 * - Benkleman, Adam
	 * @return boolean - success(true) or fail(false)
	 */
	public boolean rollbackTransaction() {
		try {
			conn.rollback();
			endTransaction();
			return true;
		}
		catch (SQLException ex) {
			// Error rolling back transaction
			return false;
		}
	}

	/**
	 * Commit Transaction
	 * - Benkleman, Adam
	 * @return boolean - success(true) or fail(false)
	 */
	public boolean commitTransaction() {
		try {
			conn.commit();
			endTransaction();
			return true;
		}
		catch (SQLException ex)
		{
			// Error commiting transaction, rolling back
			rollbackTransaction();
			return false;
		}
	}

	/**
	 * End Transaction
	 * - Benkleman, Adam
	 * @return boolean - success(true) or fail(false)
	 */
	public boolean endTransaction() {
		try {
			conn.setAutoCommit(true);
			return true;
		}
		catch (SQLException ex) {
			// Error ending transaction
			return false;
		}
	}


	/**
	 * Encrypt Password
	 * - Habermas, Jim (from example password encryption files)
	 * @param secret - Password (in plain text)
	 * @return String - password hash
	 */
    public static String encryptPassword(String secret) {
        String sha1 = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(secret.getBytes("utf8"));
            sha1 = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception e) { // Shouldn't ever error/crash
            e.printStackTrace();
        }
        return sha1;
    }


	/**
	 * - Benkleman, Adam
	 * - Improved by Ismaili, Shpend
	 * @param in_username
	 * @param in_password
	 * @return success(true) or failure(false) for adding the user login info to the database
	 */
	// Add Username and Password to login table
	// INSERT INTO login(username, password) VALUES(?,?)
	public boolean addUserLogin(String in_username, String in_password) {
		// Break case:
		if(userType.equals("Faculty") && in_password.equals("")) {
			// User is registering as a Faculty, but didn't provide a password
			return false;
		}

		// Store the username in the global variable 
		username = in_username;

		int i = 0;
		try {
			sql = "INSERT INTO login(username, password) VALUES(?,?)";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_username);
			if(!in_password.equals("")) { // if user entered a password
				// bind encrypted password to prepared statement
				prepStmt.setString(2, encryptPassword(in_password));
			}
			else { // user didn't enter a password (must be either Student or Outside)
				// bind un-encrypted empty-string as the user password (rather than the hash of an empty string)
				prepStmt.setString(2, in_password);
			}
			i = prepStmt.executeUpdate();
		}
		catch(Exception e) {
			System.out.println("Error adding user login: " + e.getMessage());
			i = 0;
		}
		return (i > 0);
	}

	/**
	 * - Benkleman, Adam
	 * - Improved by Ismaili, Shpend
	 * @param in_user_info
	 * @return success(true) or failure(false) for adding user info to the database
	 */
	// Add user info to specific user table
	// Faculty: INSERT INTO faculty(username, name, email, building_no, office_no) VALUES(?,?,?,?,?)
	// Student: INSERT INTO student(username, name, email) VALUES(?,?,?)
	// Outside: INSERT INTO outside(username, name, email) VALUES(?,?,?)
	public boolean addUserInfo(String[] in_user_info) {
		// check global variable to see which user type is being added (for which table to add the info to)
		int i = 0;
		try {
			switch (userType) {
				case "Faculty":
					try {
						sql = "INSERT INTO faculty(username, name, email, building_no, office_no) VALUES(?,?,?,?,?)";
						prepStmt = conn.prepareStatement(sql);
						prepStmt.setString(1, username);
						prepStmt.setString(2, in_user_info[0]);
						prepStmt.setString(3, in_user_info[1]);
						prepStmt.setString(4, in_user_info[2]);
						prepStmt.setString(5, in_user_info[3]);
						i = prepStmt.executeUpdate();
					}
					catch(Exception e) {
						System.out.println("Error adding faculty info: " + e.getMessage());
						i = 0;
					}
					break;

				case "Student":
					try {
						sql = "INSERT INTO student(username, name, email) VALUES(?,?,?)";
						prepStmt = conn.prepareStatement(sql);
						prepStmt.setString(1, username);
						prepStmt.setString(2, in_user_info[0]);
						prepStmt.setString(3, in_user_info[1]);
						i = prepStmt.executeUpdate();
					}
					catch(Exception e) {
						System.out.println("Error adding student info: " + e.getMessage());
						i = 0;
					}
					break;

				case "Outside":
					try {
						sql = "INSERT INTO outside(username, name, email) VALUES(?,?,?)";
						prepStmt = conn.prepareStatement(sql);
						prepStmt.setString(1, username);
						prepStmt.setString(2, in_user_info[0]);
						prepStmt.setString(3, in_user_info[1]);
						i = prepStmt.executeUpdate();
					}
					catch(Exception e) {
						System.out.println("Error adding outside info: " + e.getMessage());
						i = 0;
					}
					break;
			
				default:
					// Never runs, but keeps (i = 0) so it errors and rolls back
					System.out.println("Invalid user type: " + userType);
					break;
			}
		} catch (Exception e) {
			System.out.println("General error in addUserInfo: " + e.getMessage());
			i = 0;
		}
		
		return (i > 0);
	}

	
	/**
	 * - Benkleman, Adam
	 * @param in_username
	 * @return if username exists(true) or not(false)
	 */
	// Is username already in login table?
	// SELECT count(username) FROM login WHERE username = ?
	public boolean usernameExists(String in_username) {
		int i = 0;
		try {
			// NOT using LIKE because usernames can be precisely unique
			sql = "SELECT count(username) FROM login WHERE username = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_username);
			rs = prepStmt.executeQuery();
			rs.next();
			i = rs.getInt(1);
		}
		catch (Exception e) {
			i = 1; // Error: return fake-true for username existing
		}
		return (i == 0) ? false : true;
	}


	/**
	 * - Benkleman, Adam
	 * @param in_username
	 * @return list of abstracts associated with a faculty username
	 */
	// Get abstracts for username
	// SELECT content FROM abstracts JOIN facultyabstracts USING(abstract_id) WHERE username = ?
	public List<String> getFacultyAbstracts(String in_username) {
		List<String> abstracts = new ArrayList<>();
		try {
			// Get content from abstracts for username
			sql = "SELECT content FROM abstracts JOIN facultyabstracts USING(abstract_id) WHERE username = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_username);
			rs = prepStmt.executeQuery();
			boolean exists = rs.next();
			if(exists) { // query didn't return an empty ResultSet
				do {
					abstracts.add(rs.getString(1));
				} while(rs.next());
			}
			else { // query doesn't find any abstracts for username
				abstracts = null;
			}
		}
		catch (Exception e) {
			abstracts = null;
		}
		return (abstracts != null) ? abstracts : new ArrayList<>();
		// abstracts for username if query returns a ResultSet
		// empty array if Error or no ResultSet
	}

	/**
	 * - Benkleman, Adam
	 * @param in_abstract
	 * @return 
	 * abstract_id if abstract already exists
	 * zero if abstract doesn't exist
	 * negative if error
	 */
	// Is abstract already in abstract table?
	// SELECT abstract_id FROM abstracts WHERE content = ?
	public int abstractExists(String in_abstract) {
		int i = 0;
		try {
			sql = "SELECT abstract_id FROM abstracts WHERE content LIKE ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, "%"+in_abstract+"%");
			rs = prepStmt.executeQuery();
			boolean exists = rs.next();
			if(exists) { // query didn't return an empty ResultSet
				i = rs.getInt(1); // return abstract_id for the abstract searched
			} // else returns 0 for no results found
		}
		catch (Exception e) {
			i = -1; // Error: return negative
		}
		return i;
	}

	/**
	 * - Benkleman, Adam
	 * - Improved by Ismaili, Shpend
	 * @param in_abstract
	 * @param in_type
	 * @return abstract_id for added abstract
	 */
	// Add abstract to abstract table
	// INSERT INTO abstracts(content, type) VALUES(?,?)
	// call abstractExists() to get the abstract_id created
	public int addAbstract(String in_abstract, String in_type) {
		// Check if abstract already exists
		int i = abstractExists(in_abstract);
		if(i == 0) { // abstract doesn't already exist
			try {
				sql = "INSERT INTO abstracts(content, type) VALUES(?,?)";
				prepStmt = conn.prepareStatement(sql);
				prepStmt.setString(1, in_abstract);
				prepStmt.setString(2, in_type);
				i = prepStmt.executeUpdate();
				if(i > 0) { // abstract added successfully
					// gets abstract_id
					i = abstractExists(in_abstract);
				}
			}
			catch(Exception e) {
				i = -1;
			}
		}
		// else if i > 0 then abstract exists and returns abstract_id
		// else if i < 0 then error occurred and returns negative
		return i;
	}

	/**
	 * - Ismaili, Shpend
	 * @param in_abstract
	 * @return abstract_id for added abstract (using default type 'book')
	 */
	// For backward compatibility
	public int addAbstract(String in_abstract) {
		return addAbstract(in_abstract, "book");
	}

	/**
	 * - Benkleman, Adam
	 * @param in_username
	 * @param in_abstract_id
	 * @return 
	 * faculty_abstract_id if faculty-abstract association already exists
	 * zero if faculty-abstract association doesn't exist
	 * negative if error
	 */
	// Abstract already assigned to user?
	// SELECT faculty_abstract_id FROM facultyabstracts WHERE username = ? AND abstract_id = ?
	public int facultyAbstractExists(String in_username, int in_abstract_id) {
		int i = 0;
		try {
			sql = "SELECT faculty_abstract_id FROM facultyabstracts WHERE username = ? AND abstract_id = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_username);
			prepStmt.setInt(2, in_abstract_id);
			rs = prepStmt.executeQuery();
			boolean exists = rs.next();
			if(exists) { // query didn't return an empty ResultSet
				i = rs.getInt(1);
			} // else returns 0 for no results found
		}
		catch (Exception e) {
			i = -1; // Error: return negative
		}
		return i;
	}

	/**
	 * - Benkleman, Adam
	 * @param in_username
	 * @param in_abstract_id
	 * @return faculty_abstract_id
	 */
	// Add faculty-abstract association to table
	// INSERT INTO facultyabstracts(username, abstract_id) VALUES(?,?)
	// call facultyAbstractExists() to get the faculty_abstract_id created
	public int addFacultyAbstract(String in_username, int in_abstract_id) {
		// Check if faculty-abstract association already exists
		int i = facultyAbstractExists(in_username, in_abstract_id);
		if(i == 0) { // faculty-abstract association doesn't already exist
			try {
				sql = "INSERT INTO facultyabstracts(username, abstract_id) VALUES(?,?)";
				prepStmt = conn.prepareStatement(sql);
				prepStmt.setString(1, in_username);
				prepStmt.setInt(2, in_abstract_id);
				i = prepStmt.executeUpdate();
				if(i > 0) { // faculty-abstract association added successfully
					// gets faculty_abstract_id
					i = facultyAbstractExists(in_username, in_abstract_id);
				}
			}
			catch(Exception e) {
				i = -1;
			}
		}
		// else if i > 0 then faculty-abstract association exists and returns faculty_abstract_id
		// else if i < 0 then error occurred and returns negative
		return i;
	}

	/**
	 * - Benkleman, Adam
	 * - Improved by Ismaili, Shpend
	 * @param in_username
	 * @param in_index
	 * @return
	 * i > 0: faculty-abstract association successfully deleted
	 * i = 0: faculty-abstract association didn't exist
	 * i = -1: error occurred
	 * i = -2: invalid index
	 */
	// Delete faculty-abstract association from table
	// DELETE FROM facultyabstracts WHERE username = ? AND abstract_id = ?
	public int deleteFacultyAbstract(String in_username, int in_index) {
		int i = 0;
		// Get faculty-abstract associations
		List<String> abstracts = getFacultyAbstracts(in_username);
		if(!abstracts.isEmpty()) { // non-empty list of abstracts
			if(in_index >= 0 && in_index < abstracts.size()) {
				String abstract_content = abstracts.get(in_index);
				// Check if faculty-abstract association exists by finding the abstract_id
				int abstract_id = abstractExists(abstract_content);
				if(abstract_id > 0) {
					i = facultyAbstractExists(in_username, abstract_id);
					if(i > 0) { // faculty-abstract association does exist
						try {
							sql = "DELETE FROM facultyabstracts WHERE username = ? AND abstract_id = ?";
							prepStmt = conn.prepareStatement(sql);
							prepStmt.setString(1, in_username);
							prepStmt.setInt(2, abstract_id);
							i = prepStmt.executeUpdate();
						}
						catch(Exception e) {
							i = -1;
						}
					}
				}
			} else {
				i = -2; // Invalid index
			}
		}
		else { // empty list of abstracts
			i = 0; // no faculty-abstract associations
		}
		return i;
	}


	/**
	 * - Benkleman, Adam
	 * - Fixed by Ismaili, Shpend
	 * @param in_username
	 * @return list of interests associated with a user
	 */
	// Get interests for username
	// SELECT content FROM interests JOIN userinterests USING(interest_id) WHERE username = ?
	public List<String> getUserInterests(String in_username) {
		List<String> interests = new ArrayList<>();
		try {
			// Get content from interests from username
			sql = "SELECT i.content FROM interests i " +
				  "JOIN userinterests ui ON i.interest_id = ui.interest_id " +
				  "WHERE ui.username = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_username);
			rs = prepStmt.executeQuery();
			
			while (rs.next()) {
				interests.add(rs.getString("content"));
			}
			
		}
		catch (Exception e) {
			System.out.println("Error in getUserInterests: " + e.getMessage());
			interests = new ArrayList<>();
		}
		return interests;
	}

	/**
	 * - Benkleman, Adam
	 * - Fixed by Ismaili, Shpend
	 * @param in_interest
	 * @return
	 * interest_id if interest already exists
	 * zero if interest doesn't exist
	 * negative if error
	 */
	// Is interest already in interest table?
	// SELECT interest_id FROM interests WHERE content = ?
	public int interestExists(String in_interest) {
		int i = 0;
		try {
			// Always use exact matching to prevent confusion with similar interests
			// (e.g., "C" vs "C++")
			sql = "SELECT interest_id FROM interests WHERE content = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_interest);
			
			rs = prepStmt.executeQuery();
			if (rs.next()) { // query didn't return an empty ResultSet
				i = rs.getInt(1); // return interest_id for the interest searched
			} // else returns 0 for no results found
		}
		catch (Exception e) {
			System.out.println("Error in interestExists: " + e.getMessage());
			i = -1; // Error: return negative
		}
		return i;
	}

	/**
	 * - Benkleman, Adam
	 * - Modified by Ismaili, Shpend to add validation for student interests
	 * @param in_interest
	 * @return 
	 * - interest_id for added interest if successful
	 * - -2 if interest is invalid (for students only)
	 * - -1 if error
	 * - 0 if not found
	 */
	// Add interest to interest table
	// INSERT INTO interests(content) VALUES(?)
	// call interestExists() to get the interest_id created
	public int addInterest(String in_interest) {
		// For students, validate that interests are 1-3 words
		if (userType.equals("Student") && !validateInterest(in_interest)) {
			System.out.println("Interest validation failed for student interest: '" + in_interest + "'");
			return -2; // Invalid interest for student (not 1-3 words)
		}
		
		// Check if interest already exists
		int i = interestExists(in_interest);
		
		if(i == 0) { // interest doesn't already exist
			try {
				sql = "INSERT INTO interests(content) VALUES(?)";
				prepStmt = conn.prepareStatement(sql);
				prepStmt.setString(1, in_interest);
				int result = prepStmt.executeUpdate();

				if(result > 0) { // interest added successfully
					// gets interest_id
					i = interestExists(in_interest);
				} else {
					i = -1;
				}
			}
			catch(Exception e) {
				System.out.println("Error in addInterest: " + e.getMessage());
				i = -1;
			}
		}
		return i;
	}

	/**
	 * - Benkleman, Adam
	 * - Fixed by Ismaili, Shpend
	 * @param in_username
	 * @param in_interest_id
	 * @return 
	 * - user_interest_id if user-interest association already exists
	 * - zero if user-interest association doesn't exist
	 * - negative if error
	 */
	// Interest already assigned to user?
	// SELECT user_interest_id FROM userinterests WHERE username = ? AND interest_id = ?
	public int userInterestExists(String in_username, int in_interest_id) {
		int i = 0;
		try {
			sql = "SELECT user_interest_id FROM userinterests WHERE username = ? AND interest_id = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_username);
			prepStmt.setInt(2, in_interest_id);
			rs = prepStmt.executeQuery();
			if (rs.next()) { // query didn't return an empty ResultSet
				i = rs.getInt(1);
			} // else returns 0 for no results found
		}
		catch (Exception e) {
			System.out.println("Error in userInterestExists: " + e.getMessage());
			i = -1; // Error: return negative
		}
		return i;
	}

	/**
	 * - Benkleman, Adam
	 * - Fixed by Ismaili, Shpend
	 * @param in_username
	 * @param in_interest_id
	 * @return 
	 * - positive id if added successfully
	 * - 0 if already exists (not an error)
	 * - negative if error
	 */
	// Add user-interest association to table
	// INSERT INTO userinterests(username, interest_id) VALUES(?,?)
	// call userInterestExists() to get the user_interest_id created
	public int addUserInterest(String in_username, int in_interest_id) {
		// Check if user-interest association already exists
		int i = userInterestExists(in_username, in_interest_id);
		
		if(i == 0) { // user-interest association doesn't already exist
			try {
				sql = "INSERT INTO userinterests(username, interest_id) VALUES(?,?)";
				prepStmt = conn.prepareStatement(sql);
				prepStmt.setString(1, in_username);
				prepStmt.setInt(2, in_interest_id);
				int result = prepStmt.executeUpdate();
				
				if(result > 0) { // user_interest association added successfully
					// gets user_interest_id
					i = userInterestExists(in_username, in_interest_id);
				} else {
					System.out.println("Failed to insert user interest");
					i = -1; // Failed to insert
				}
			}
			catch(Exception e) {
				System.out.println("Error in addUserInterest: " + e.getMessage());
				i = -1; // Error
			}
		}
		// Returns:
		// - positive id if added successfully
		// - 0 if already exists (not an error)
		// - negative if error
		return i;
	}
	
	/**
	 * - Benkleman, Adam
	 * - Improved by Ismaili, Shpend
	 * @param in_username
	 * @param in_index
	 * @return
	 * i > 0: user-interest association successfully deleted
	 * i = 0: user-interest association didn't exist
	 * i = -1: error occurred
	 * i = -2: invalid index
	 */
	// Delete user-interest association from table
	// DELETE FROM userinterests WHERE username = ? AND interest_id = ?
	public int deleteUserInterest(String in_username, int in_index) {
		int i = 0;
		// Get user-interest associations
		List<String> interests = getUserInterests(in_username);
		if(!interests.isEmpty()) { // non-empty list of interests
			if(in_index >= 0 && in_index < interests.size()) {
				String interest = interests.get(in_index);
				// Check if user-interest association exists by finding the interest_id
				int interest_id = interestExists(interest);
				if(interest_id > 0) {
					i = userInterestExists(in_username, interest_id);
					if(i > 0) { // user-interest association does exist
						try {
							sql = "DELETE FROM userinterests WHERE username = ? AND interest_id = ?";
							prepStmt = conn.prepareStatement(sql);
							prepStmt.setString(1, in_username);
							prepStmt.setInt(2, interest_id);
							i = prepStmt.executeUpdate();
						}
						catch(Exception e) {
							i = -1;
						}
					}
				}
			} else {
				i = -2; // Invalid index
			}
		}
		else { // empty list of interests
			i = 0; // no user-interest associations
		}
		return i;
	}

	
	/**
	 * - Benkleman, Adam
	 * @return list of all abstracts
	 */
	// Get all abstracts
	// SELECT content FROM abstracts
	public List<String> getAbstracts() {
		List<String> abstracts = new ArrayList<>();
		try {
			// Get content from abstracts
			stmt = conn.createStatement();
			sql = "SELECT content FROM abstracts ";
			rs = stmt.executeQuery(sql);
			boolean exists = rs.next();
			if(exists) { // query didn't return an empty ResultSet
				do {
					abstracts.add(rs.getString(1));
				} while(rs.next());
			}
			else { // query doesn't find any abstracts
				abstracts = null;
			}
		}
		catch (Exception e) {
			abstracts = null;
		}
		return (abstracts != null) ? abstracts : new ArrayList<>();
		// abstracts if query returns a ResultSet
		// empty array if Error or no ResultSet
	}


	/**
	 * - Benkleman, Adam
	 * @return list of all interests
	 */
	// Get all interests
	// SELECT content FROM interests
	public List<String> getInterests() {
		List<String> interests = new ArrayList<>();
		try {
			// Get content from interests
			stmt = conn.createStatement();
			sql = "SELECT content FROM interests ";
			rs = stmt.executeQuery(sql);
			boolean exists = rs.next();
			if(exists) { // query didn't return an empty ResultSet
				do {
					interests.add(rs.getString(1));
				} while(rs.next());
			}
			else { // query doesn't find any interests
				interests = null;
			}
		}
		catch (Exception e) {
			interests = null;
		}
		return (interests != null) ? interests : new ArrayList<>();
		// interests if query returns a ResultSet
		// empty array if Error or no ResultSet
	}


	/**
	 * - Benkleman, Adam
	 * @param in_interest
	 * @return list of users associated with interest
	 */
	// Search for users with interest
	// SELECT username FROM userinterests JOIN interests USING(interest_id) WHERE content = ?
	public List<String> getUsersWithInterest(String in_interest) {
		List<String> usernames = new ArrayList<>();
		try {
			// Get usernames from interests for interest
			sql = "SELECT username FROM userinterests JOIN interests USING(interest_id) WHERE content = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_interest);
			rs = prepStmt.executeQuery();
			boolean exists = rs.next();
			if(exists) { // query didn't return an empty ResultSet
				do {
					usernames.add(rs.getString(1));
				} while(rs.next());
			}
			else { // query doesn't find any usernames for interest
				usernames = null;
			}
		}
		catch (Exception e) {
			usernames = null;
		}
		return (usernames != null) ? usernames : new ArrayList<>();
		// usernames for interest if query returns a ResultSet
		// empty array if Error or no ResultSet
	}

	/**
	 * - Benkleman, Adam
	 * - Fixed by Ismaili, Shpend
	 * @param in_abstract
	 * @return list of faculty associated with abstract
	 */
	// Search for faculty with abstract
	// SELECT username FROM facultyabstracts JOIN abstract USING(abstract_id) WHERE content = ?
	public List<String> getFacultyWithAbstract(String in_abstract) {
		List<String> usernames = new ArrayList<>();
		try {
			// Get usernames from abstracts for abstract
			sql = "SELECT fa.username FROM facultyabstracts fa " +
				  "JOIN abstracts a ON fa.abstract_id = a.abstract_id " +
				  "WHERE a.content = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_abstract);
			rs = prepStmt.executeQuery();
			
			while (rs.next()) {
				usernames.add(rs.getString("username"));
			}
		}
		catch (Exception e) {
			usernames = new ArrayList<>();
		}
		return usernames;
	}


	/**
	 * - Benkleman, Adam
	 * @param in_username
	 * @return true or false if user is in faculty table
	 */
	// Is username in faculty table?
	// SELECT count(username) FROM faculty WHERE username = ?
	public boolean usernameInFaculty(String in_username) {
		int i = 0;
		try {
			sql = "SELECT count(username) FROM faculty WHERE username = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_username);
			rs = prepStmt.executeQuery();
			rs.next();
			i = rs.getInt(1);
		}
		catch (Exception e) {
			i = 0; // Error: return fake-false for username existing
		}
		return (i == 0) ? false : true;
	}

	/**
	 * - Benkleman, Adam
	 * @param in_username
	 * @return true or false if user is in student table
	 */
	// is username in student table?
	// SELECT count(username) FROM student WHERE username = ?
	public boolean usernameInStudent(String in_username) {
		int i = 0;
		try {
			sql = "SELECT count(username) FROM student WHERE username = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_username);
			rs = prepStmt.executeQuery();
			rs.next();
			i = rs.getInt(1);
		}
		catch (Exception e) {
			i = 0; // Error: return fake-false for username existing
		}
		return (i == 0) ? false : true;
	}

	/**
	 * - Benkleman, Adam
	 * @param in_username
	 * @return true or false if user is in outside table
	 */
	// Is username in outsider table?
	// SELECT count(username) FROM outside WHERE username = ?
	public boolean usernameInOutside(String in_username) {
		int i = 0;
		try {
			sql = "SELECT count(username) FROM outside WHERE username = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_username);
			rs = prepStmt.executeQuery();
			rs.next();
			i = rs.getInt(1);
		}
		catch (Exception e) {
			i = 0; // Error: return fake-false for username existing
		}
		return (i == 0) ? false : true;
	}


	/**
	 * - Benkleman, Adam
	 * @param in_username
	 * @param in_password
	 * @return Success(true) or Failure(false) for if user logged in
	 */
	// Check if username is in the db
	// Check if the username belongs to Faculty, Student, or Outside
	// - Faculty: encrypt password and check against hashed password in login table
	public boolean performLogin(String in_username, String in_password) {
		// check if the provided username is in the login table
		if(usernameExists(in_username)) { // username does exist
			// check if username belongs to Faculty, Student, or Outside
			if(usernameInFaculty(in_username)) {
				userType = "Faculty";
			}
			else if(usernameInStudent(in_username)) {
				userType = "Student";
			}
			else if(usernameInOutside(in_username)) {
				userType = "Outside";
			}
			else { // Error: username found in login table, but not in any user table
				// should not run
				return false;
			}

			// Validate Password
			if(userType.equals("Faculty") && in_password.equals("")) { // password cannot be blank for a Faculty user
				return false;
			}

			// get password hash from database to compare against user-provided password
			String dbPassword = new String();
			try {
				sql = "SELECT password FROM login WHERE username = ?";
				prepStmt = conn.prepareStatement(sql);
				prepStmt.setString(1, in_username);
				rs = prepStmt.executeQuery();
				rs.next();
				dbPassword = rs.getString(1);
			}
			catch (Exception e) {
				return false; // Error: return Not Logged in
			}

			if(!in_password.equals("")) { // only check password if it's not blank (empty string has a hash value that would fail password verification)
				// compare provided password with password from database
				return (dbPassword.equals(encryptPassword(in_password))) ? true : false;
			}
			return true;
		}
		// else username doesn't exist in the login table
		return false;
	}


	// Check global userType for who is doing the searching
	// Check in_userType for who they are searching for
	// - Faculty for Student:
	//   - get all abstracts for the faculty username
	//   - get all interests
	//   - search for interests similar to the abstracts
	//   - get usernames for each user with each faculty interest and interest similar to the abstracts
	//   - get name and email for each of the usernames in the students table
	//     - map student username to value of ^^ with 'shared interests' appended at the end
	//   - return string array of map values
	// - Student for Faculty:
	//   - get all abstracts
	//   - search for abstracts similar to the student's interests
	//   - get usernames for each faculty with their abstract similar to the student's interests
	//   - get usernames for each user with each student interest
	//   - get name, email, building_no, and office_no for each of the faculty in the faculty table
	//     - map faculty username to value of ^^ with 'shared interests' appended at the end
	//   - return string array of map values
	// - Student for Outside:
	//   - get usernames for each user with each student interest
	//   - get name and email for each of the usernames in the outside table
	//     - map outside username to value of ^^ with 'shared interests' appended at the end
	//   - return string array of map values
	// - Outside for Student:
	//   - get usernames for each user with each outside interest
	//   - get name and email for each of the usernames in the students table
	//     - map student username to value of ^^ with 'shared interests' appended at the end
	//   - return string array of map values

	/**
	 * Original Search
	 * - Benkleman, Adam
	 * 
	 * Refactored Search
	 * - Ismaili, Shpend
	 * - Nura, Leon 
	 * @param username of the user doing the searching (logged-in user)
	 * @param targetType - Type of user the current user is searching for (Faculty, Student, Outside)
	 * @return - multi-line String of user info, for users with similar interests to current user
	 */
    public String searchForSimilarInterests(String username, String targetType) {
        List<String> rawMatches;
        switch (userType) {
            case "Faculty":
                rawMatches = findStudentsByFaculty(username);
                break;
            case "Student":
                rawMatches = "Faculty".equals(targetType)
                        ? findFacultyByStudent(username)
                        : findOutsideByStudent(username);
                break;
            case "Outside":
                rawMatches = findStudentsByOutside(username);
                break;
            default:
                return "";
        }
        return formatContactInfo(rawMatches, targetType);
    }

	/**
	 * Faculty searching for Students
	 * - Ismaili, Shpend
	 * - Nura, Leon 
	 */
    private List<String> findStudentsByFaculty(String facultyUser) {
        // Get faculty abstracts and interests
        List<String> interests = getUserInterests(facultyUser);
        List<String> abstracts = getFacultyAbstracts(facultyUser);
        
        // Find students who have interests that match the faculty's abstracts
        List<String> matchingStudents = new ArrayList<>();
        
        // Store matching data for formatting output later
        setMatchingData(new ArrayList<>()); // Clear previous match data
        
        // First try to match faculty abstracts to student interests
        for (String abs : abstracts) {
            // Find all interests that match this abstract
            List<String> matchingInterests = new ArrayList<>();
            List<String> allInterests = getInterests();
            String absLower = abs.toLowerCase();
            
            // Find interests that match this abstract
            for (String interest : allInterests) {
                if (interest == null || interest.trim().isEmpty()) continue;
                
                String interestLower = interest.toLowerCase().trim();
                boolean isMatch = false;
                
                // Use the same matching logic as matchInterestsToAbstract
                // Special handling for single-character interests (like "C")
                if (interestLower.length() == 1) {
                    char c = interestLower.charAt(0);
                    
                    // Check for word boundaries for single characters
                    String[] words = absLower.split("\\s+");
                    for (String word : words) {
                        if (word.equals(String.valueOf(c)) || 
                            word.equals(c + ".") || 
                            word.equals(c + ",") ||
                            word.equals(c + ";") ||
                            word.equals(c + ":") ||
                            word.startsWith(c + " ")) {
                            isMatch = true;
                            break;
                        }
                    }
                    
                    if (absLower.contains(" " + c + "++") || 
                        absLower.contains(" " + c + "#") ||
                        absLower.contains(" " + c + " ")) {
                        isMatch = true;
                    }
                } else {
                    // Try regex word boundary matching first
                    try {
                        if (absLower.matches(".*\\b" + interestLower + "\\b.*") ||
                            absLower.matches("^" + interestLower + "\\b.*") ||
                            absLower.matches(".*\\b" + interestLower + "$") ||
                            absLower.matches(".*[\\s.,;:!?]" + interestLower + "[\\s.,;:!?].*")) {
                            isMatch = true;
                        }
                    } catch (Exception e) {
                        // If regex fails, fall back to simpler checks
                    }
                    
                    // Fallback checks if regex didn't match
                    if (!isMatch) {
                        // Split the abstract into words and check for exact matching
                        String[] words = absLower.split("\\s+|[.,;:!?()]");
                        for (String word : words) {
                            if (word.equals(interestLower)) {
                                isMatch = true;
                                break;
                            }
                        }
                        
                        // Check for substring patterns
                        if (!isMatch) {
                            String pattern = " " + interestLower + " ";
                            String pattern2 = " " + interestLower + ".";
                            String pattern3 = " " + interestLower + ",";
                            String pattern4 = " " + interestLower + ";";
                            
                            if (absLower.contains(pattern) || 
                                absLower.contains(pattern2) || 
                                absLower.contains(pattern3) || 
                                absLower.contains(pattern4) ||
                                absLower.startsWith(interestLower + " ") ||
                                absLower.endsWith(" " + interestLower)) {
                                isMatch = true;
                            }
                        }
                        
                        // Multi-word interest check
                        if (!isMatch && interestLower.contains(" ")) {
                            String[] interestWords = interestLower.split("\\s+");
                            boolean allWordsFound = true;
                            for (String word : interestWords) {
                                if (word.length() < 3) continue; // Skip very short words
                                
                                if (!absLower.contains(word)) {
                                    allWordsFound = false;
                                    break;
                                }
                            }
                            
                            if (allWordsFound) {
                                isMatch = true;
                            }
                        }
                    }
                }
                
                if (isMatch) {
                    matchingInterests.add(interest);
                    
                    // Find students with this interest
                    List<String> studentsWithInterest = getUsersWithInterest(interest);
                    for (String student : studentsWithInterest) {
                        if (usernameInStudent(student)) {
                            matchingStudents.add(student);
                            
                            // Store match information
                            String[] matchInfo = new String[3];
                            matchInfo[0] = student;          // student username
                            matchInfo[1] = interest;         // matching interest
                            matchInfo[2] = abs;              // faculty abstract
                            addMatchingData(matchInfo);
                        }
                    }
                }
            }
        }
        
        // Also find students with direct interest matches
        List<String> fromInterests = findUsersByInterests(interests, "student");
        
        // Store interest match data
        for (String student : fromInterests) {
            List<String> studentInterests = getUserInterests(student);
            for (String facultyInterest : interests) {
                if (studentInterests.contains(facultyInterest)) {
                    // This student has this faculty interest
                    String[] matchInfo = new String[3];
                    matchInfo[0] = student;            // student username
                    matchInfo[1] = facultyInterest;    // shared interest
                    matchInfo[2] = null;               // no abstract (null means interest match)
                    addMatchingData(matchInfo);
                }
            }
        }
        
        // Combine all results and filter to only include students
        return filterByUserType(mergeUnique(matchingStudents, fromInterests), "student");
    }

    /** 
	 * Student searching for Faculty
	 * - Ismaili, Shpend
	 * - Nura, Leon 
	 */
    private List<String> findFacultyByStudent(String studentUser) {
        // Get student interests
        List<String> interests = getUserInterests(studentUser);
        
        // Find abstracts that match student interests and what interest matched
        List<String[]> matchedAbstractsWithInterests = matchAbstractsToInterests(interests);
        
        // Find faculty users from matched abstracts 
        List<String> facultyFromAbstracts = new ArrayList<>();
        
        // Store matching data for formatting output later
        // Map faculty username to list of [interest, abstract] matches
        setMatchingData(new ArrayList<>()); // Clear previous match data
        
        // Get faculty for each matched abstract
        for (String[] match : matchedAbstractsWithInterests) {
            String abstract_content = match[0];
            String matched_interest = match[1];
            
            List<String> faculty = getFacultyWithAbstract(abstract_content);
            for (String facultyUser : faculty) {
                facultyFromAbstracts.add(facultyUser);
                
                // Store match information for this faculty member
                String[] matchInfo = new String[3];
                matchInfo[0] = facultyUser;        // faculty username
                matchInfo[1] = matched_interest;   // student interest that matched
                matchInfo[2] = abstract_content;   // matched abstract
                addMatchingData(matchInfo);
            }
        }
        
        // Also find faculty with matching interests
        List<String> facultyFromInterests = findUsersByInterests(interests, "faculty");
        
        // Store interest match data
        for (String facultyUser : facultyFromInterests) {
            List<String> facultyInterests = getUserInterests(facultyUser);
            for (String studentInterest : interests) {
                if (facultyInterests.contains(studentInterest)) {
                    // This faculty has this student interest
                    String[] matchInfo = new String[3];
                    matchInfo[0] = facultyUser;        // faculty username
                    matchInfo[1] = studentInterest;    // shared interest
                    matchInfo[2] = null;               // no abstract (null means interest match)
                    addMatchingData(matchInfo);
                }
            }
        }
        
        // Combine all results and filter to only include faculty
        return mergeUnique(facultyFromAbstracts, facultyFromInterests);
    }

    /** 
	 * Student searching for Outside users
	 * - Ismaili, Shpend
	 * - Nura, Leon 
	 */
    private List<String> findOutsideByStudent(String studentUser) {
        return findUsersByInterests(getUserInterests(studentUser), "outside");
    }

    /** 
	 * Outside searching for Students
	 * - Ismaili, Shpend
	 * - Nura, Leon 
	 */
    private List<String> findStudentsByOutside(String outsideUser) {
        return findUsersByInterests(getUserInterests(outsideUser), "student");
    }

    /** 
	 * -- Helper methods for refactored search -- 
	 * - Ismaili, Shpend
	 * - Nura, Leon 
	 */

    /** 
	 * Enhanced matching between abstracts and interests 
	 * - Ismaili, Shpend
	 * - Nura, Leon 
	 */
    private List<String[]> matchAbstractsToInterests(List<String> interests) {
        List<String[]> matches = new ArrayList<>();
        List<String> all = getAbstracts();
        
        if (!all.isEmpty() && !interests.isEmpty()) {
            for (String abs : all) {
                String absLower = abs.toLowerCase();
                
                // For each abstract, find all matching interests (not just the first one)
                for (String interest : interests) {
                    // Skip empty interests
                    if (interest == null || interest.trim().isEmpty()) {
                        continue;
                    }
                    
                    String interestLower = interest.toLowerCase().trim();
                    boolean isMatch = false;
                    
                    // Special handling for single-character interests (like "C")
                    if (interestLower.length() == 1) {
                        // Use word boundary matching for single characters
                        char c = interestLower.charAt(0);
                        
                        // Check for word boundaries - the character surrounded by spaces,
                        // punctuation, or at the beginning/end of the abstract
                        String[] words = absLower.split("\\s+");
                        for (String word : words) {
                            // Check if the word is exactly this single character
                            if (word.equals(String.valueOf(c)) || 
                                word.equals(c + ".") || 
                                word.equals(c + ",") ||
                                word.equals(c + ";") ||
                                word.equals(c + ":") ||
                                word.startsWith(c + " ")) {
                                isMatch = true;
                                break;
                            }
                        }
                        
                        // Also check for special programming cases like C++, C#
                        if (absLower.contains(" " + c + "++") || 
                            absLower.contains(" " + c + "#") ||
                            absLower.contains(" " + c + " ")) {
                            isMatch = true;
                        }
                    }
                    // Special case for programming languages
                    else if (interestLower.matches("c\\+\\+|c#|java|sql")) {
                        // Use stricter matching for common programming terms to avoid false positives
                        if (absLower.contains(" " + interestLower + " ") || 
                            absLower.contains(" " + interestLower + ".") ||
                            absLower.contains(" " + interestLower + ",") ||
                            absLower.contains(" " + interestLower + ";") ||
                            absLower.contains(" " + interestLower + ":") ||
                            absLower.startsWith(interestLower + " ") ||
                            absLower.endsWith(" " + interestLower)) {
                            isMatch = true;
                        }
                    }
                    // Regular words (like "book", "wealth")
                    else {
                        // First try word boundary matching with regex
                        try {
                            if (absLower.matches(".*\\b" + interestLower + "\\b.*")) {
                                isMatch = true;
                            }
                            // Check if it's at the start of the abstract with a space after it
                            else if (absLower.matches("^" + interestLower + "\\b.*")) {
                                isMatch = true;
                            } 
                            // Check if it's at the end of the abstract with a space before it
                            else if (absLower.matches(".*\\b" + interestLower + "$")) {
                                isMatch = true;
                            }
                            // Check for word surrounded by punctuation
                            else if (absLower.matches(".*[\\s.,;:!?]" + interestLower + "[\\s.,;:!?].*")) {
                                isMatch = true;
                            }
                        } catch (Exception e) {
                            // If regex fails (possibly due to special characters in the interest),
                            // fall back to simpler checks
                            System.out.println("Regex error for interest '" + interest + "': " + e.getMessage());
                        }
                        
                        // If regex matching didn't work, try fallback approach
                        if (!isMatch) {
                            // Split the abstract into words and check for exact matching
                            String[] words = absLower.split("\\s+|[.,;:!?()]");
                            for (String word : words) {
                                if (word.equals(interestLower)) {
                                    isMatch = true;
                                    break;
                                }
                            }
                            
                            // Also check for the interest as a substring surrounded by spaces or punctuation
                            if (!isMatch) {
                                String pattern = " " + interestLower + " ";
                                String pattern2 = " " + interestLower + ".";
                                String pattern3 = " " + interestLower + ",";
                                String pattern4 = " " + interestLower + ";";
                                
                                if (absLower.contains(pattern) || 
                                    absLower.contains(pattern2) || 
                                    absLower.contains(pattern3) || 
                                    absLower.contains(pattern4) ||
                                    absLower.startsWith(interestLower + " ") ||
                                    absLower.endsWith(" " + interestLower)) {
                                    isMatch = true;
                                }
                            }
                            
                            // As a fallback for multi-word interests, check each word
                            if (!isMatch && interestLower.contains(" ")) {
                                String[] interestWords = interestLower.split("\\s+");
                                boolean allWordsFound = true;
                                for (String word : interestWords) {
                                    if (word.length() < 3) continue; // Skip very short words
                                    
                                    // For each word, check if it appears in the abstract
                                    if (!absLower.contains(word)) {
                                        allWordsFound = false;
                                        break;
                                    }
                                }
                                
                                if (allWordsFound) {
                                    isMatch = true;
                                }
                            }
                        }
                    }
                    
                    if (isMatch) {
                        // Store both the abstract and matching interest
                        String[] match = new String[2];
                        match[0] = abs;       // the abstract
                        match[1] = interest;  // the matching interest
                        matches.add(match);
                    }
                }
            }
        }
        return matches;
    }

	/*
    private List<String> matchInterestsToAbstract(String abs) {
        List<String> matches = new ArrayList<>();
        List<String> all = getInterests();
        String absLower = abs.toLowerCase();
        
        if (!all.isEmpty()) {
            for (String interest : all) {
                if (interest == null || interest.trim().isEmpty()) {
                    continue;
                }
                
                String interestLower = interest.toLowerCase().trim();
                boolean isMatch = false;
                
                // Special handling for single-character interests (like "C")
                if (interestLower.length() == 1) {
                    // Use word boundary matching for single characters
                    char c = interestLower.charAt(0);
                    
                    // Check for word boundaries for single characters
                    String[] words = absLower.split("\\s+");
                    for (String word : words) {
                        // Check if the word is exactly this single character
                        if (word.equals(String.valueOf(c)) || 
                            word.equals(c + ".") || 
                            word.equals(c + ",") ||
                            word.equals(c + ";") ||
                            word.equals(c + ":") ||
                            word.startsWith(c + " ")) {
                            isMatch = true;
                            break;
                        }
                    }
                    
                    // Also check for special programming cases like C++, C#
                    if (absLower.contains(" " + c + "++") || 
                        absLower.contains(" " + c + "#") ||
                        absLower.contains(" " + c + " ")) {
                        isMatch = true;
                    }
                }
                // Regular words (like "book", "wealth")
                else {
                    // First try word boundary matching
                    try {
                        if (absLower.matches(".*\\b" + interestLower + "\\b.*")) {
                            isMatch = true;
                        }
                        // Check if it's at the start of the abstract with a space after it
                        else if (absLower.matches("^" + interestLower + "\\b.*")) {
                            isMatch = true;
                        } 
                        // Check if it's at the end of the abstract with a space before it
                        else if (absLower.matches(".*\\b" + interestLower + "$")) {
                            isMatch = true;
                        }
                        // Check for word surrounded by punctuation
                        else if (absLower.matches(".*[\\s.,;:!?]" + interestLower + "[\\s.,;:!?].*")) {
                            isMatch = true;
                        }
                    } catch (Exception e) {
                        // If regex fails (possibly due to special characters in the interest),
                        // fall back to simpler checks
                    }
                    
                    // If regex matching didn't work, try fallback approach
                    if (!isMatch) {
                        // Split the abstract into words and check for exact matching
                        String[] words = absLower.split("\\s+|[.,;:!?()]");
                        for (String word : words) {
                            if (word.equals(interestLower)) {
                                isMatch = true;
                                break;
                            }
                        }
                        
                        // Also check for the interest as a substring surrounded by spaces or punctuation
                        if (!isMatch) {
                            String pattern = " " + interestLower + " ";
                            String pattern2 = " " + interestLower + ".";
                            String pattern3 = " " + interestLower + ",";
                            String pattern4 = " " + interestLower + ";";
                            
                            if (absLower.contains(pattern) || 
                                absLower.contains(pattern2) || 
                                absLower.contains(pattern3) || 
                                absLower.contains(pattern4) ||
                                absLower.startsWith(interestLower + " ") ||
                                absLower.endsWith(" " + interestLower)) {
                                isMatch = true;
                            }
                        }
                        
                        // As a fallback for multi-word interests, check each word
                        if (!isMatch && interestLower.contains(" ")) {
                            String[] interestWords = interestLower.split("\\s+");
                            boolean allWordsFound = true;
                            for (String word : interestWords) {
                                if (word.length() < 3) continue; // Skip very short words
                                
                                // For each word, check if it appears in the abstract
                                if (!absLower.contains(word)) {
                                    allWordsFound = false;
                                    break;
                                }
                            }
                            
                            if (allWordsFound) {
                                isMatch = true;
                            }
                        }
                    }
                }
                
                if (isMatch) {
                    // If the interest matches, get all users with that interest
                    matches.addAll(getUsersWithInterest(interest));
                }
            }
        }
        return matches;
    }
	*/

	/*
    private List<String> findUsersByAbstracts(List<String> absList, String type) {
        List<String> users = new ArrayList<>();
        if (!absList.isEmpty()) {
            for (String abs : absList) {
                users.addAll("faculty".equals(type)
                        ? getFacultyWithAbstract(abs)
                        : matchInterestsToAbstract(abs));
            }
        }
        return filterByUserType(users, type);
    }
	*/

    private List<String> findUsersByInterests(List<String> interests, String type) {
        List<String> users = new ArrayList<>();
        if (!interests.isEmpty()) {
            for (String interest : interests) {
                users.addAll(getUsersWithInterest(interest));
            }
        }
        return filterByUserType(users, type);
    }

    private List<String> filterByUserType(List<String> candidates, String type) {
        return candidates.stream()
                .filter(u -> {
                    switch (type) {
                        case "faculty":
                            return usernameInFaculty(u);
                        case "student":
                            return usernameInStudent(u);
                        case "outside":
                            return usernameInOutside(u);
                        default:
                            return false;
                    }
                })
                .distinct()
                .collect(Collectors.toList());
    }

    private String formatContactInfo(List<String> users, String searchType) {
        if (users.isEmpty()) {
            return "No matching users found.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Found %d matching %s:\n", users.size(), 
                  searchType.equals("Faculty") ? "faculty members" : 
                  searchType.equals("Student") ? "students" : "outsiders"));
        sb.append("-------------------------------------------------------------\n");
        
        for (int i = 0; i < users.size(); i++) {
            String user = users.get(i);
            sb.append(String.format("%d. %s\n", i+1, getContactInfo(user, searchType)));
            
            // Add shared interests information
            List<String> userInterests = getUserInterests(user);
            if (!userInterests.isEmpty()) {
                sb.append("   Interests: ");
                for (int j = 0; j < userInterests.size(); j++) {
                    sb.append(userInterests.get(j));
                    if (j < userInterests.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("\n");
            }
            
            // For faculty, also show abstracts
            if (searchType.equals("Faculty")) {
                List<String> abstracts = getFacultyAbstracts(user);
                if (!abstracts.isEmpty()) {
                    sb.append("   Abstract count: ").append(abstracts.size()).append("\n");
                }
            }
                
            // Add match information if available - works for any user type
            if (matchingData != null && !matchingData.isEmpty()) {
                // Group matches by user
                List<String[]> userMatches = new ArrayList<>();
                for (String[] match : matchingData) {
                    if (match[0].equals(user)) {
                        userMatches.add(match);
                    }
                }
                
                if (!userMatches.isEmpty()) {
                    sb.append("   Matches with your interests:\n");
                    
                    // First show interest matches
                    boolean hasInterestMatches = false;
                    Set<String> sharedInterests = new LinkedHashSet<>(); // Use set to prevent duplicates
                    for (String[] match : userMatches) {
                        if (match[2] == null) { // This is an interest match
                            hasInterestMatches = true;
                            sharedInterests.add(match[1]);
                        }
                    }
                    if (hasInterestMatches) {
                        sb.append("     * Shared interests: ");
                        boolean first = true;
                        for (String interest : sharedInterests) {
                            if (!first) sb.append(", ");
                            sb.append(interest);
                            first = false;
                        }
                        sb.append("\n");
                    }
                    
                    // Then group abstract matches by abstract
                    Map<String, Set<String>> abstractMatches = new LinkedHashMap<>();
                    for (String[] match : userMatches) {
                        if (match[2] != null) { // This is an abstract match
                            if (!abstractMatches.containsKey(match[2])) {
                                abstractMatches.put(match[2], new LinkedHashSet<>());
                            }
                            abstractMatches.get(match[2]).add(match[1]);
                        }
                    }
                    
                    // Show abstract matches
                    for (Map.Entry<String, Set<String>> entry : abstractMatches.entrySet()) {
                        String abstract_content = entry.getKey();
                        Set<String> matchingInterests = entry.getValue();
                        
                        // Create a short preview of the abstract
                        String abstract_preview = abstract_content;
                        if (abstract_preview.length() > 50) {
                            abstract_preview = abstract_preview.substring(0, 47) + "...";
                        }
                        
                        // Show all interests that matched this abstract
                        if (searchType.equals("Faculty")) {
                            // When student is searching for faculty
                            sb.append("     * Your interests ");
                        } else {
                            // When faculty is searching for students
                            sb.append("     * Student interests ");
                        }
                            
                        boolean first = true;
                        for (String interest : matchingInterests) {
                            if (!first) sb.append(", ");
                            sb.append("'").append(interest).append("'");
                            first = false;
                        }
                        sb.append(" matched in abstract: \"")
                          .append(abstract_preview).append("\"\n");
                    }
                }
            }
            
            sb.append("-------------------------------------------------------------\n");
        }
        
        return sb.toString();
    }

    private List<String> mergeUnique(List<String> a, List<String> b) {
        Set<String> set = new LinkedHashSet<>();
        if (!a.isEmpty())
            set.addAll(a);
        if (!b.isEmpty())
            set.addAll(b);
        return new ArrayList<>(set);
    }

    // Store match data for enhanced output
    private List<String[]> matchingData = new ArrayList<>();
    
    private void setMatchingData(List<String[]> data) {
        this.matchingData = data;
    }
    
    private void addMatchingData(String[] match) {
        this.matchingData.add(match);
    }
    
    public List<String[]> getMatchingData() {
        return this.matchingData;
    }

	/**
	 * Get abstract types for a faculty member
	 * - Ismaili, Shpend
	 * @param username - faculty username
	 * @return Map of abstract types and counts
	 */
	public String getAbstractTypeInfo(String username) {
		StringBuilder result = new StringBuilder();
		int bookCount = 0;
		int speakingCount = 0;
		
		try {
			sql = "SELECT a.type FROM abstracts a " +
				  "JOIN facultyabstracts fa ON a.abstract_id = fa.abstract_id " +
				  "WHERE fa.username = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, username);
			rs = prepStmt.executeQuery();
			
			while (rs.next()) {
				String type = rs.getString("type");
				if ("book".equals(type)) {
					bookCount++;
				} else if ("speaking_engagement".equals(type)) {
					speakingCount++;
				}
			}
			
			if (bookCount > 0 || speakingCount > 0) {
				result.append("Books: ").append(bookCount);
				result.append(", Speaking: ").append(speakingCount);
			}
		} catch (Exception e) {
			return "";
		}
		
		return result.toString();
	}

	/**
	 * Original - getFacultyContact - gets contact info for faculty
	 * - Bhakta, Aisha
	 * 
	 * Refactor to handle getting contact info for all Faculty, Student, or Other
	 * - Benkleman Adam
	 * 
	 * Updated to show abstract type information for faculty
	 * - Ismaili, Shpend
	 * 
	 * Get contact info for user (for display)
	 * Faculty: SELECT name, email, building_no, office_no FROM faculty WHERE username = ?
	 * Student: SELECT name, email FROM student WHERE username = ?
	 * Outside: SELECT name, email FROM outside WHERE username = ?
	 * 
	 * @param in_username
	 * @param in_search_type
	 * @return single-line string of contact info for a user
	 */
	public String getContactInfo(String in_username, String in_search_type) {
		String result = "";
		// Searching for Faculty
		if(in_search_type.equals("Faculty")) {
			try {
				sql = "SELECT name, email, building_no, office_no FROM faculty WHERE username = ?";
				prepStmt = conn.prepareStatement(sql);
				prepStmt.setString(1, in_username);
				rs = prepStmt.executeQuery();
				if (rs.next()) {
					result = rs.getString("name") + " | " +
							 rs.getString("email") + " | " +
							 "Building: " + rs.getString("building_no") + " | " +
							 "Office: " + rs.getString("office_no");
					
					// Add abstract type information
					String abstractInfo = getAbstractTypeInfo(in_username);
					if (!abstractInfo.isEmpty()) {
						result += " | " + abstractInfo;
					}
				}
			} catch (Exception e) {
				result = "Error retrieving contact info";
			}
			return result;
		}
		// Searching for Student
		else if(in_search_type.equals("Student")) {
			sql = "SELECT name, email FROM student WHERE username = ?";
		}
		// Searching for Outside
		else if(in_search_type.equals("Outside")) {
			sql = "SELECT name, email FROM outside WHERE username = ?";
		}

		// Only runs for Student and Outside
		try {
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_username);
			rs = prepStmt.executeQuery();
			if (rs.next()) {
				result = rs.getString("name") + " | " +
						 rs.getString("email");
			}
		} catch (Exception e) {
			result = "Error retrieving contact info";
		}
		return result;
	}

	/**
	 * Validates if an interest meets the project requirements (1-3 words)
	 * - Ismaili, Shpend
	 * @param interest - The interest to validate
	 * @return true if valid (1-3 words), false otherwise
	 */
	public boolean validateInterest(String interest) {
		if (interest == null || interest.trim().isEmpty()) {
			return false;
		}
		
		// Remove extra whitespace and count words
		String trimmed = interest.trim().replaceAll("\\s+", " ");
		String[] words = trimmed.split("\\s+");
		

		
		// Project requirement: Student interests must be 1-3 words
		return words.length >= 1 && words.length <= 3;
	}

	/**
	 * Get abstracts with types for username
	 * - Ismaili, Shpend
	 * @param in_username
	 * @return list of abstract with their type
	 */
	public List<String[]> getFacultyAbstractsWithTypes(String in_username) {
		List<String[]> results = new ArrayList<>();
		try {
			// Get content and type from abstracts for username
			sql = "SELECT a.content, a.type FROM abstracts a " +
				  "JOIN facultyabstracts fa ON a.abstract_id = fa.abstract_id " +
				  "WHERE fa.username = ?";
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, in_username);
			rs = prepStmt.executeQuery();
			boolean exists = rs.next();
			if(exists) { // query didn't return an empty ResultSet
				do {
					String[] entry = new String[2];
					entry[0] = rs.getString(1); // content
					entry[1] = rs.getString(2); // type
					results.add(entry);
				} while(rs.next());
			}
		}
		catch (Exception e) {
			results = new ArrayList<>();
		}
		return results;
	}

	public static void main(String[] args) {
		new FacultyResearchDataLayer_Group1("faculty_research_group1"); // call constructor
	}// end of main method
}// end of class