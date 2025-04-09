package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import passwordEvaluationTestbed.PasswordEvaluator;
import userNameRecognizerTestbed.UserNameRecognizer;

import java.sql.SQLException;

import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 */
public class SetupAccountPage {
	
    private final DatabaseHelper databaseHelper;
    // DatabaseHelper to handle database operations.
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
    	// Input fields for userName, password, and invitation code
    	TextField firstNameField = new TextField();
    	firstNameField.setPromptText("Enter First Name");
        firstNameField.setMaxWidth(250);
    	
        TextField lastNameField = new TextField();
    	lastNameField.setPromptText("Enter Last Name");
        lastNameField.setMaxWidth(250);
    	
    	// Input field for userName
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);
        
        // Input field for Email Address
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email Address");
        emailField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        

        Button setupButton = new Button("Setup");
        Button backButton = new Button("Back to Home");
        
        backButton.setOnAction(e -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
        	String firstName = firstNameField.getText();
        	String lastName = lastNameField.getText();
            String userName = userNameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String code = inviteCodeField.getText();
            
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
            	// Check if the user already exists
            	if(!databaseHelper.doesUserExist(userName)) {
            		
            		// Validate the invitation code
            		if(databaseHelper.validateInvitationCode(code)) {
            			
            			// Create a new user and register them in the database
            			User user=new User(firstName, lastName, userName, email, password, "user");
		                databaseHelper.register(user);
		                System.out.println("Account setup completed.");
		                
		                // Navigate to the Login Page again
		                new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
            		}
            		else {
            			errorLabel.setText("Please enter a valid invitation code");
            		}
            	}
            	else {
            		errorLabel.setText("This userName is taken!!.. Please use another to setup an account");
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(firstNameField, lastNameField, userNameField, emailField, passwordField, inviteCodeField, setupButton, errorLabel, backButton);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
