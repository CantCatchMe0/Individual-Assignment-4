package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import userNameRecognizerTestbed.UserNameRecognizer;
import passwordEvaluationTestbed.PasswordEvaluator;

import java.sql.SQLException;

import databasePart1.*;

/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;
    
    /**
     * Constructs the AdminSetupPage with a database helper.
     *
     * @param databaseHelper the database helper used to interact with the database
     */
    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    /**
     * Displays the admin setup form in the provided primary stage.
     * The form collects first name, last name, username, email, and password from the user.
     * If the inputs are valid, an admin account is registered in the database.
     *
     * @param primaryStage the JavaFX stage to display the setup form on
     */
    public void show(Stage primaryStage) {
    	// Input fields for first and last name
    	TextField firstNameField = new TextField();
    	firstNameField.setPromptText("Enter First Name");
        firstNameField.setMaxWidth(250);
    	
        TextField lastNameField = new TextField();
    	lastNameField.setPromptText("Enter Last Name");
        lastNameField.setMaxWidth(250);
    	
    	// Input field for userName
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);
        
        // Input field for Email Address
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email Address");
        emailField.setMaxWidth(250);
		
        // Input field for password
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
        	String firstName = firstNameField.getText();
        	String lastName = lastNameField.getText();
            String userName = userNameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            
            // Input has been provided, let's see if it is a valid user name or not
            String errMessageUserName = UserNameRecognizer.checkForValidUserName(userName);
			if (errMessageUserName != "") {
				// Display the error message
				System.out.println(errMessageUserName);
				
				// Fetch the index where the processing of the input stopped
				if (UserNameRecognizer.userNameRecognizerIndexofError <= -1) return;	// Should never happen
				// Display the input line so the user can see what was entered		
				System.out.println(userName);
				// Display the line up to the error and the display an up arrow
				System.out.println(userName.substring(0,UserNameRecognizer.userNameRecognizerIndexofError) + "\u21EB");
				// Prevent creating the account with invalid information by returning to the log in interface
				return;
			}
			else {
				// The returned String is empty, it, so there is no error in the input.
				System.out.println("Success! The UserName is valid.");
			}
			
			String  errMessagePassword = PasswordEvaluator.evaluatePassword(password);
			if (errMessagePassword != "") {
				// Display the error message
				System.out.println(errMessagePassword);
				System.out.println("***Failure*** The password <" + password + "> is invalid.");
				System.out.println("Error message: " + errMessagePassword);
				return;
			}
			else {
				// The returned String is empty, it, so there is no error in the input.
				System.out.println("Success! The UserName is valid.");
			}
			
            try {
            	// Create a new User object with admin role and register in the database
            	User user=new User(firstName, lastName, userName, email, password, "admin");
                databaseHelper.register(user);
                System.out.println("Administrator setup completed.");
                
                // Navigate to the Login Page again
                new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10, firstNameField, lastNameField, userNameField, emailField, passwordField, setupButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
