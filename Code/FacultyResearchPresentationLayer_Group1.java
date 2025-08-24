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

import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;

public class FacultyResearchPresentationLayer_Group1 extends JFrame {
    
	// Attributes
    public static Font myFontForOutput = new Font("Courier", Font.PLAIN, 24);
    public static Font myFontForAstric = new Font("Times Roman", Font.BOLD, 45); 
    public static JPanel loginBox = new JPanel(new GridLayout(2,2));
    public static JLabel lblUser = new JLabel("Username -> ");
	public static JLabel lblPassword = new JLabel("Password -> ");
	public static JLabel lblConfirmPassword = new JLabel("Confirm Password -> ");
    public static JTextField tfUser = new JTextField("");    
    public static JPasswordField tfPassword = new JPasswordField("");
    public static JPasswordField tfConfirmPassword = new JPasswordField("");

	static FacultyResearchDataLayer_Group1 db;
	// static Scanner kb = new Scanner(System.in);
	static String username = new String();
	static String response = new String();
	static int selection = 0;
	static boolean success = false;
	static boolean registered = true;

	/**
	 * Default Constructor
	 */
	public FacultyResearchPresentationLayer_Group1() {
		// empty constructor
	}


	public static void loginMenu() {
		// Reset the loginBox
		JPanel loginBox = new JPanel(new GridLayout(2,2));
		// Reset the text of the fields
		tfUser.setText("");
		tfPassword.setText("");
		// Add the labels and fields
		loginBox.add(lblUser);
		loginBox.add(tfUser);
		loginBox.add(lblPassword);
		loginBox.add(tfPassword);

		System.out.println("Displaying User Login Menu" + "\n");
		JOptionPane.showMessageDialog(null, loginBox, "User Login", JOptionPane.INFORMATION_MESSAGE);

		username = tfUser.getText();
		String password = new String(tfPassword.getPassword());

		// try to log in with the user credentials
		success = db.performLogin(username, password);

		// determine which login page to show
		// NOT a while loop since it's just figuring out which user menu to show
		if(success) { // if login is successful
			switch (db.userType) {
				case "Faculty":
					facultyMenu();
					break;
			
				case "Student":
					studentMenu();
					break;

				case "Outside":
					outsideMenu();
					break;

				default: // shouldn't run
					break;
			}
		}
		else { // Error logging in
			errorAlert("Login Failed");
		}
		// reset for parent while loop
		success = true;
	}


	public static void registerMenu() {
		// Don't loop through this menu
		System.out.print(
			"Register User" + "\n" +
			"What kind of user are you registering as?" + "\n" +
			"1. Faculty User" + "\n" +
			"2. Student User" + "\n" +
			"3. Outside User" + "\n" +
			"4. Go Back" + "\n" + 
			" >> "
		);
		selection = GetInput.readLineInt();
		System.out.println();
		if(selection == 1) { // Faculty User
			db.userType = "Faculty";
			registerUser();
		}
		else if(selection == 2) { // Student User
			db.userType = "Student";
			registerUser();
		}
		else if(selection == 3) { // Outside User
			db.userType = "Outside";
			registerUser();
		}
		else { // 4 or other goes back to main Login/Register Menu
			// just exit back to the Login/Register Menu
		}
	}


	public static void registerUser() {
		// Reset the loginBox
		JPanel loginBox = new JPanel(new GridLayout(3,2));
		// Reset the text of the fields
		tfUser.setText("");
		tfPassword.setText("");
		tfConfirmPassword.setText("");
		// Add the labels and fields
		loginBox.add(lblUser);
		loginBox.add(tfUser);
		loginBox.add(lblPassword);
		loginBox.add(tfPassword);
		loginBox.add(lblConfirmPassword);
		loginBox.add(tfConfirmPassword);

		System.out.println("Displaying User Registration Menu" + "\n");
		JOptionPane.showMessageDialog(null, loginBox, "User Registration", JOptionPane.INFORMATION_MESSAGE);

		username = tfUser.getText();
		String password = new String(tfPassword.getPassword());
		String confirmPassword = new String(tfConfirmPassword.getPassword());

		// Break Case: Username Validation
		success = false;
		if(username.equals("")) { // username cannot be blank
			errorAlert("You must provide a username.");
		}
		else if(username.contains(" ")) { // username cannot have a space
			errorAlert("Your username cannot contain a space.");
		}
		else if(db.usernameExists(username)) { // username cannot already exist
			errorAlert("Username already exists.");
		}
		else if(!password.equals(confirmPassword)) { // passwords have to match
			errorAlert("Your passwords do not match.");
		}
		else if(db.userType.equals("Faculty") && password.equals("")) { // password cannot be blank for a Faculty user
			errorAlert("You must provide a password.");
		}
		else { // No Validation Errors
			success = true;
		}

		// Proceed to login
		if(success) {
			// Save username to database object
			db.username = username;
			System.out.println("Registering user: " + username);
			
			// Start a transaction for the entire registration process
            if (db.startTransaction()) {
				if(db.addUserLogin(username, password)) {
					// Successfully added the user login to the database
					registerUserInfo();
				}
				else { // error adding the user login to the database
					// Shouldn't run due to above validation
					// Already rolled-back and ended the transaction
					db.rollbackTransaction();
					errorAlert("Issue adding user login.");
					success = false;
				}
			} else {
                errorAlert("Could not start database transaction.");
                success = false;
            }
		}
		// Errors for Not Success are handled in the Validation area
		
		// success variable reset happens in registerMenu() method
	}

	public static void registerUserInfo() {
		// 4 values for Faculty users, 2 values for Student and Outside users
		String[] info = new String[db.userType.equals("Faculty") ? 4 : 2];
		// Initialize all array elements to empty strings
		for(int i = 0; i < info.length; i++) {
			info[i] = "";
		}

		// Get user information
		System.out.println("Enter your user information!");
		
		try {
			while(info[0].equals("")) { // loop while name is blank
				System.out.print("Name: ");
			info[0] = GetInput.readLine().trim();
			}
			
			while(info[1].equals("")) { // loop while email is blank
				System.out.print("Email: ");
			info[1] = GetInput.readLine().trim();
			}
			
			// For Faculty users, get Building No and Office No
			if(db.userType.equals("Faculty")) {
				while(info[2].equals("")) { // loop while building no is blank
					System.out.print("Building No: ");
				info[2] = GetInput.readLine().trim();
				}
				while(info[3].equals("")) { // loop while office no is blank
					System.out.print("Office No: ");
				info[3] = GetInput.readLine().trim();
				}
			}
			
			System.out.println("User info collected: Name=" + info[0] + ", Email=" + info[1] + 
				(db.userType.equals("Faculty") ? ", Building=" + info[2] + ", Office=" + info[3] : ""));

			// Add user info to the database
			if(db.addUserInfo(info)) {
				// Successfully added the user info to the database
				System.out.println("User info added successfully!");
				
				// Get Abstract or Interest for the Faculty user
				if(db.userType.equals("Faculty")) {
					// Don't loop through this menu
					System.out.print(
						"Abstract/Interest" + "\n" +
						"1. Add Abstract" + "\n" +
						"2. Add Interest" + "\n" +
						"3. Add Both" + "\n" + 
						" >> "
					);
					selection = GetInput.readLineInt();
					System.out.println();
					if(selection == 1) { // Add Abstract
						addAbstractMenu();
					}
					else if(selection == 2) { // Add Interest
						addInterestMenu();
					}
					else { // 3 or other chooses to add both
						addAbstractMenu();
						addInterestMenu();
					}
				}
				// Get Interest for the Student or Outside user
				else {
					addInterestMenu();
				}

				// Check if Abstract and/or Interest was successful
				if(success) { // true = commit
					db.commitTransaction();
					System.out.println("Registration completed successfully!");
				}
				else { // false = rollback
					db.rollbackTransaction();
					System.out.println("Registration had issues with abstracts/interests. Rolling back changes.");
				}
			}
			else { // error adding the user login information to the database
				errorAlert("Issue adding user information.");
				db.rollbackTransaction();
				success = false;
			}
		} catch (Exception e) {
			System.out.println("Error during user registration: " + e.getMessage());
			e.printStackTrace();
			errorAlert("Error during registration: " + e.getMessage());
			db.rollbackTransaction();
			success = false;
		}
	}

	public static void showAbstracts() {
		List<String[]> abstractsWithTypes = db.getFacultyAbstractsWithTypes(username);
		if (!abstractsWithTypes.isEmpty()) {
			System.out.println("\nYour Abstracts:");
			System.out.println("-------------------------------------------------------------");
			for (int i = 0; i < abstractsWithTypes.size(); i++) {
				String[] entry = abstractsWithTypes.get(i);
				String content = entry[0];
				String type = entry[1];
				
				// Convert type for display
				String displayType = "book".equals(type) ? "Book" : "Speaking Engagement";
				
				System.out.println((i+1) + ". Type: " + displayType);
				System.out.println("   " + content);
				System.out.println("-------------------------------------------------------------");
			}
		}
		else {
			System.out.println("You are not associated with any Abstracts.");
			success = false; // printed out no abstracts
		}
	}

	public static void addAbstractMenu() {
		// reset response variable
		response = "";
		// loop until you get an abstract to add
		while(response.equals("")) {
			System.out.print(
				"Enter the entire abstract you want to add." + "\n" +
				" >> "
			);
			response = GetInput.readLine();
		}

		// Get abstract type
		String abstractType = "";
		while (!abstractType.equals("1") && !abstractType.equals("2")) {
			System.out.print(
				"Select abstract type:" + "\n" +
				"1. Book" + "\n" +
				"2. Speaking Engagement" + "\n" +
				" >> "
			);
			abstractType = GetInput.readLine();
		}

		// Convert selection to type string
		String type = abstractType.equals("1") ? "book" : "speaking_engagement";

		// Insert the new Abstract into the table
		// Add the faculty-abstract association
		int n = db.addAbstract(response, type); // positive for abstract_id, negative for error
		if(n > 0) { // successfully added the abstract
			// Add the faculty-abstract association
			n = db.addFacultyAbstract(username, n);
		}
		if(n <= 0) { // did not add the abstract or the faculty-abstract correctly
			// should not occurr, and therefore rolls back the registration of the user
			// mark the user registration as incomplete
			errorAlert("Issue adding the abstract to the database.");
			success = false;
		}
	}

	public static void deleteAbstractMenu() {
		// reset selection variable
		selection = 0;
		// show all abstracts for faculty
		showAbstracts();
		if(success) {
			// loop until you get an abstract to delete
			while(selection < 1) {
				System.out.print(
					"Which abstract do you want to delete?" + "\n" +
					" >> "
				);
				selection = GetInput.readLineInt();
			}

			// Delete the faculty-abstract association from the table
			// Convert from 1-based display index to 0-based array index
			int n = db.deleteFacultyAbstract(username, selection - 1); // positive for success, negative for error
			if(n <= 0) { // did not remove the faculty-abstract association correctly
				if (n == -2) {
					errorAlert("Invalid abstract selection. Please try again.");
				} else {
					errorAlert("Issue deleting the abstract from the database.");
				}
				success = false;
			} else {
				System.out.println("Abstract successfully deleted.");
			}
		}
	}

	public static void showInterests() {
		List<String> interests = db.getUserInterests(username);
		if(!interests.isEmpty()) { // non-empty list of interests
			System.out.println("\nYour Interests:");
			System.out.println("-------------------------------------------------------------");
			for (int i = 0; i < interests.size(); i++) {
				System.out.println((i+1) + ". " + interests.get(i));
			}
			System.out.println("-------------------------------------------------------------");
		}
		else { // empty list of interests
			System.out.println("You are not associated with any Interests.");
			success = false;
		}
	}

	public static void addInterestMenu() {
		// reset response variable
		response = "";
		// loop until you get an interest to add
		while(response.equals("")) {
			if (db.userType.equals("Student")) {
				System.out.print(
					"Enter the interest(s) you want to add." + "\n" +
					"Student interests must be 1-3 words each." + "\n" +
					"(use commas to add more than one)" + "\n" +
					" >> "
				);
			} else {
				System.out.print(
					"Enter the interest(s) you want to add." + "\n" +
					"(use commas to add more than one)" + "\n" +
					" >> "
				);
			}
			response = GetInput.readLine();
		}
		// got one or more interests
		String[] interests = response.split(",");
        
        // Start transaction for adding interests
        db.startTransaction();
		
		// Insert the new Interests into the table
		// Add the user-interest associations
		boolean allValid = true;
		boolean anyAdded = false;
		
		for(String i : interests) {
			i = i.trim(); // Gets rid of excess whitespace (in case they use a comma-space delimitered input)
			if (i.isEmpty()) continue;
			
			int n = db.addInterest(i); // positive for interest_id, negative for error
			
			if (n == -2) { // Invalid interest format for student
				errorAlert("Interest '" + i + "' is invalid. Student interests must be 1-3 words.");
				allValid = false;
				continue;
			}
			
			if(n > 0) { // successfully added or found the interest
				// Add the user-interest association
				n = db.addUserInterest(username, n);
				if (n > 0) {
				    anyAdded = true;
				    System.out.println("Added interest: " + i);
				} else if (n == 0) {
				    System.out.println("You already have interest: " + i);
				} else {
				    System.out.println("Failed to add interest: " + i);
				    allValid = false;
				}
			} else {
				// Issue adding interest
				errorAlert("Issue adding interest '"+i+"' to the database.");
				allValid = false;
			}
		}
		
		// Commit or rollback transaction based on results
		if (allValid && anyAdded) {
		    db.commitTransaction();
		    System.out.println("Interests successfully added.");
		} else if (!anyAdded) {
		    db.rollbackTransaction();
		    System.out.println("No new interests were added.");
		    success = false;
		} else {
		    // Some were valid but some failed
		    db.commitTransaction(); // Commit the successful ones
		    System.out.println("Some interests were added, others failed.");
		}
	}

	public static void deleteInterestMenu() {
		// reset selection variable
		selection = 0;
		// show all interests for user
		showInterests();
		if(success) {
			// loop until you get an interest to delete
			while(selection < 1) {
				System.out.print(
					"Which interest do you want to delete?" + "\n" +
					" >> "
				);
				selection = GetInput.readLineInt();
			}

			// Delete the user-interest association from the table
			// Convert from 1-based display index to 0-based array index
			int n = db.deleteUserInterest(username, selection - 1); // positive for success, negative for error
			if(n <= 0) { // did not remove the user-interest association correctly
				if (n == -2) {
					errorAlert("Invalid interest selection. Please try again.");
				} else {
					errorAlert("Issue deleting the interest from the database.");
				}
				success = false;
			} else {
				System.out.println("Interest successfully deleted.");
			}
		}
	}

	public static void updateInterestMenu() {
		// reset selection variable
		selection = 0;
		// show all interests for user
		showInterests();
		if(success) {
			// loop until you get an interest to update
			while(selection < 1) {
				System.out.print(
					"Which interest do you want to update?" + "\n" +
					" >> "
				);
				selection = GetInput.readLineInt();
			}
			
			// Get the interest to be updated
			List<String> interests = db.getUserInterests(username);
			if (selection > interests.size()) {
				errorAlert("Invalid selection. Please try again.");
				success = false;
				return;
			}
			
			// Get the replacement interest
			String newInterest = "";
			while(newInterest.equals("")) {
				System.out.print(
					"Enter the new interest to replace '" + interests.get(selection-1) + "':" + "\n" +
					(db.userType.equals("Student") ? "Student interests must be 1-3 words.\n" : "") +
					" >> "
				);
				newInterest = GetInput.readLine().trim();
			}
			
			// Start transaction for updating
			db.startTransaction();
			
			// Delete the existing interest
			int deleted = db.deleteUserInterest(username, selection - 1);
			if (deleted <= 0) {
				errorAlert("Failed to update interest. Could not remove the original interest.");
				db.rollbackTransaction();
				success = false;
				return;
			}
			
			// Add the new interest
			int n = db.addInterest(newInterest);
			if (n == -2) {
				errorAlert("Interest '" + newInterest + "' is invalid. Student interests must be 1-3 words.");
				db.rollbackTransaction();
				success = false;
				return;
			} else if (n > 0) {
				// Add user-interest association
				n = db.addUserInterest(username, n);
				if (n > 0) {
					db.commitTransaction();
					System.out.println("Interest successfully updated to: " + newInterest);
				} else {
					errorAlert("Failed to update interest. Could not add the new interest.");
					db.rollbackTransaction();
					success = false;
				}
			} else {
				errorAlert("Failed to update interest. Database error.");
				db.rollbackTransaction();
				success = false;
			}
		}
	}

	public static void facultyMenu() {
		while(success) {
			System.out.print(
				"Faculty Menu" + "\n" +
				"1. Abstracts Menu" + "\n" +
				"2. Interests Menu" + "\n" +
				"3. Search for Students with similar interests" + "\n" +
				"4. Log Out" + "\n" + 
				" >> "
			);
			// selection = GetInput.readLineInt();
			selection = GetInput.readLineInt();
			if(selection == 1) { // Abstracts Menu
				abstractsMenu();
			}
			else if(selection == 2) { // Interests Menu
				interestsMenu();
			}
			else if(selection == 3) { // Search for Students with similar interests
				System.out.println(db.searchForSimilarInterests(username, "Student"));
			}
			else { // 4 or other logs out the user
				success = false;
			}
		}
		// success variable reset happens in loginMenu() method
	}

	public static void studentMenu() {
		while(success) {
			System.out.print(
				"Student Menu" + "\n" +
				"1. Interests Menu" + "\n" +
				"2. Search for Faculty with similar interests" + "\n" +
				"3. Search for Outsiders with similar interests" + "\n" +
				"4. Log Out" + "\n" + 
				" >> "
			);
			selection = GetInput.readLineInt();
			if(selection == 1) { // Interests Menu
				interestsMenu();
			}
			else if(selection == 2) { // Search for Faculty with similar interests
				System.out.println(db.searchForSimilarInterests(username, "Faculty"));
			}
			else if(selection == 3) { // Search for Outsiders with similar interests
				System.out.println(db.searchForSimilarInterests(username, "Outside"));
			}
			else { // 4 or other logs out the user
				success = false;
			}
		}
		// success variable reset happens in loginMenu() method
	}

	public static void outsideMenu() {
		while(success) {
			System.out.print(
				"Outsider Menu" + "\n" +
				"1. Interests Menu" + "\n" +
				"2. Search for Students with similar interests" + "\n" +
				"3. Log Out" + "\n" + 
				" >> "
			);
			selection = GetInput.readLineInt();
			if(selection == 1) { // Interests Menu
				interestsMenu();
			}
			else if(selection == 2) { // Search for Faculty with similar interests
				System.out.println(db.searchForSimilarInterests(username, "Student"));
			}
			else { // 3 or other logs out the user
				success = false;
			}
		}
		// success variable reset happens in loginMenu() method
	}


	public static void abstractsMenu() {
		while(success) {
			System.out.print(
				"Abstracts Menu" + "\n" +
				"1. Show Abstracts" + "\n" +
				"2. Add Abstract" + "\n" +
				"3. Update Abstract" + "\n" +
				"4. Remove Abstract" + "\n" +
				"5. Back to Faculty Menu" + "\n" + // will only be faculty accessing abstracts
				" >> "
			);
			selection = GetInput.readLineInt();
			
			if(selection == 1) { // Show Abstracts
				showAbstracts();
			}
			else if(selection == 2) { // Add Abstract
				addAbstractMenu();
				// resets for current loop
				success = true;
			}
			else if(selection == 3) { // Update Abstract
				updateAbstractMenu();
				// resets for current loop
				success = true;
			}
			else if(selection == 4) { // Remove Abstract
				deleteAbstractMenu();
				// resets for current loop
				success = true;
			}
			else { // 5 or other returns to the Faculty Menu
				success = false;
			}
		}
		// reset for parent while loop
		success = true;
	}


	public static void interestsMenu() {
		while(success) {
			System.out.print(
				"Interests Menu" + "\n" +
				"1. Show Interests" + "\n" +
				"2. Add Interest" + "\n" +
				"3. Update Interest" + "\n" +
				"4. Remove Interest" + "\n" +
				"5. Back to " + db.userType + " Menu" + "\n" + // dynamic for any user accessing this menu
				" >> "
			);
			selection = GetInput.readLineInt();
			
			if(selection == 1) { // Show Interests
				showInterests();
			}
			else if(selection == 2) { // Add Interests
				addInterestMenu();
				// resets for current loop
				success = true;
			}
			else if(selection == 3) { // Update Interest
				updateInterestMenu();
				// resets for current loop
				success = true;
			}
			else if(selection == 4) { // Remove Interest
				deleteInterestMenu();
				// resets for current loop
				success = true;
			}
			else { // 5 or other returns to the User Menu
				success = false;
			}
		}
		// reset for parent while loop
		success = true;
	}


	public static void errorAlert(String message) {
		JPanel errorBox = new JPanel(new GridLayout(1,1));
		JLabel lblError = new JLabel(message);
		lblError.setFont(myFontForOutput);
		errorBox.add(lblError);
		JOptionPane.showMessageDialog(null, errorBox, "Error Message:", JOptionPane.QUESTION_MESSAGE);
	}

	public static void updateAbstractMenu() {
		// reset selection variable
		selection = 0;
		// show all abstracts for faculty
		showAbstracts();
		if(success) {
			// loop until you get an abstract to update
			while(selection < 1) {
				System.out.print(
					"Which abstract do you want to update?" + "\n" +
					" >> "
				);
				selection = GetInput.readLineInt();
			}
			
			// Get the abstracts
			List<String[]> abstractsWithTypes = db.getFacultyAbstractsWithTypes(username);
			if (selection > abstractsWithTypes.size()) {
				errorAlert("Invalid selection. Please try again.");
				success = false;
				return;
			}
			
			// Get the old abstract and its type
			String[] oldAbstract = abstractsWithTypes.get(selection-1);
			String oldContent = oldAbstract[0];
			String oldType = oldAbstract[1];
			
			// Get the new abstract content
			String newContent = "";
			while(newContent.equals("")) {
				System.out.print(
					"Enter the new abstract content to replace the existing abstract:" + "\n" +
					" >> "
				);
				newContent = GetInput.readLine().trim();
			}
			
			// Get new abstract type or keep the old one
			String abstractType = "";
			while (!abstractType.equals("1") && !abstractType.equals("2") && !abstractType.equals("3")) {
				System.out.print(
					"Select abstract type:" + "\n" +
					"1. Book" + "\n" +
					"2. Speaking Engagement" + "\n" +
					"3. Keep current type (" + (oldType.equals("book") ? "Book" : "Speaking Engagement") + ")" + "\n" +
					" >> "
				);
				abstractType = GetInput.readLine();
			}
			
			// Determine the type to use
			String newType;
			if (abstractType.equals("3")) {
				newType = oldType; // Keep existing type
			} else {
				newType = abstractType.equals("1") ? "book" : "speaking_engagement";
			}
			
			// Start transaction for updating
			db.startTransaction();
			
			// Delete the existing abstract
			int deleted = db.deleteFacultyAbstract(username, selection - 1);
			if (deleted <= 0) {
				errorAlert("Failed to update abstract. Could not remove the original abstract.");
				db.rollbackTransaction();
				success = false;
				return;
			}
			
			// Add the new abstract
			int n = db.addAbstract(newContent, newType);
			if (n > 0) {
				// Add faculty-abstract association
				n = db.addFacultyAbstract(username, n);
				if (n > 0) {
					db.commitTransaction();
					System.out.println("Abstract successfully updated.");
				} else {
					errorAlert("Failed to update abstract. Could not add the new abstract.");
					db.rollbackTransaction();
					success = false;
				}
			} else {
				errorAlert("Failed to update abstract. Database error.");
				db.rollbackTransaction();
				success = false;
			}
		}
	}

	/**
	 * Main method
	 * - Benkleman, Adam
	 */
	public static void main(String args[]) {
		// Sets the default colors for the GUI fields
		lblUser.setFont(myFontForOutput);
        tfUser.setFont(myFontForOutput);
        tfUser.setForeground(Color.BLUE);
        lblPassword.setFont(myFontForOutput);
        tfPassword.setFont(myFontForOutput);
        tfPassword.setForeground(Color.BLUE);
		lblConfirmPassword.setFont(myFontForOutput);
        tfConfirmPassword.setFont(myFontForOutput);
        tfConfirmPassword.setForeground(Color.BLUE);

		// Get database info (auto-filled)
		JPanel databaseBox = new JPanel(new GridLayout(2,1));
		JLabel lblDatabase = new JLabel("Database name?");
		lblDatabase.setFont(myFontForOutput);
		databaseBox.add(lblDatabase);
		JTextField textfieldDatabaseName = new JTextField("faculty_research_group1");
		textfieldDatabaseName.setFont(myFontForOutput);
		textfieldDatabaseName.setForeground(Color.BLUE);
		databaseBox.add(textfieldDatabaseName);
		JOptionPane.showMessageDialog(null, databaseBox, "Database name Input Prompt", JOptionPane.QUESTION_MESSAGE);

		String databaseName = textfieldDatabaseName.getText();
		// initialize the database
		db = new FacultyResearchDataLayer_Group1(databaseName);
		System.out.println("Database initialized: " + databaseName);

		// get database login info (auto-filled)
		JPanel dbLoginBox = new JPanel(new GridLayout(2,2));
        JLabel dbLblUser = new JLabel("Database Username -> ");
        JLabel dbLblPassword = new JLabel("Database Password -> ");
        dbLblUser.setFont(myFontForOutput);
        dbLblPassword.setFont(myFontForOutput);
        
        JTextField dbTfUser = new JTextField("root");
        JPasswordField dbTfPassword = new JPasswordField("student");
        dbTfUser.setFont(myFontForOutput);
        dbTfUser.setForeground(Color.BLUE);
        dbTfPassword.setFont(myFontForOutput);
        dbTfPassword.setForeground(Color.BLUE);

        dbLoginBox.add(dbLblUser);
        dbLoginBox.add(dbTfUser);
        dbLoginBox.add(dbLblPassword);
        dbLoginBox.add(dbTfPassword);

		JOptionPane.showMessageDialog(null, dbLoginBox, "Database Login", JOptionPane.INFORMATION_MESSAGE);

        String dbUserName = dbTfUser.getText();
        String dbPassword = new String(dbTfPassword.getPassword());
		
		System.out.println("Connecting to database with username: " + dbUserName);
		// connect to the database
		success = db.connect(dbUserName, dbPassword);

		if(!success) { // exit if error connecting
			errorAlert("Error connecting to database!");
			System.exit(0);
		}
		
		System.out.println("Successfully connected to database");

		// Login/Register
		while(success) { // if database connection is successful
			System.out.print(
				"Login/Register" + "\n" +
				"1. Login" + "\n" +
				"2. Register" + "\n" +
				"3. Exit" + "\n" + 
				" >> "
			);
			selection = GetInput.readLineInt();
			System.out.println();
			if(selection == 1) { // Login
				loginMenu();
			}
			else if(selection == 2) { // Register
				registerMenu();
				if(success) { // user successfully registered
					// Redirect to the correct login menu
					switch (db.userType) {
						case "Faculty":
							facultyMenu();
							break;
					
						case "Student":
							studentMenu();
							break;
		
						case "Outside":
							outsideMenu();
							break;
		
						default: // shouldn't run
							break;
					}
				}
				// reset for while loop
				success = true;
			}
			else { // 3 or other exits
				success = false;
				db.close();
			}
		}

		System.exit(0); // exits program
	}// end of main method
}// end of class