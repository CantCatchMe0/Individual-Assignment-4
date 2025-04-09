package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {
	
    private final DatabaseHelper databaseHelper;
    
    /**
     * Constructs a UserLoginPage with the given database helper.
     *
     * @param databaseHelper The database helper used for querying user and login data.
     */
    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    /**
     * Displays the login interface to the user.
     *
     * @param primaryStage The primary application stage.
     */
    public void show(Stage primaryStage) {
    	// Input field for the user's userName, password
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
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));

        Button loginButton = new Button("Login");
        
        loginButton.setOnAction(a -> {
        	// Retrieve user inputs
        	String firstName = firstNameField.getText();
        	String lastName = lastNameField.getText();
            String userName = userNameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            try {
            	User user=new User(firstName, lastName, userName, email, password, "");
            	WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);
            
            	String role = databaseHelper.getUserRole(userName);
            	
                if (role != null) {
                    user.setRole(role);
                    if (databaseHelper.login(user)) {
                    	List<String> userRoles = databaseHelper.getUserRoles(userName);
                    	if(userRoles.size() > 1) {
                    	    Stage popupStage = new Stage();
                    	    popupStage.setTitle("Select Role");

                    	    Label messageLabel = new Label("Choose a role to continue:");

                    	    ComboBox<String> roleComboBox = new ComboBox<>();
                    	    roleComboBox.getItems().addAll(userRoles);
                    	    roleComboBox.setValue(userRoles.get(0));

                    	    Button selectButton = new Button("Continue");
                    	    selectButton.setOnAction(e -> {
                    	        String selectedRole = roleComboBox.getValue();
                    	        if (selectedRole.equals("user")) {
                    	        	user.setRole("user");
                    	        	welcomeLoginPage.show(primaryStage, user); 
                    	        }
                    	        else if (selectedRole.equals("student")) {
                    	        	user.setRole("student");
                    	        	welcomeLoginPage.show(primaryStage, user); 
                    	        }
                    	        else if (selectedRole.equals("instructor")) {
                    	        	user.setRole("instructor");
                    	        	welcomeLoginPage.show(primaryStage, user); 
                    	        }
                    	        else if (selectedRole.equals("staff")) {
                    	        	user.setRole("staff");
                    	        	welcomeLoginPage.show(primaryStage, user); 
                    	        }
                    	        else if (selectedRole.equals("reviewer")) {
                    	        	user.setRole("reviewer");
                    	        	welcomeLoginPage.show(primaryStage, user); 
                    	        }
                    	        popupStage.close();
                    	    });

                    	    VBox layout = new VBox(10, messageLabel, roleComboBox, selectButton);
                    	    layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

                    	    Scene scene = new Scene(layout, 300, 150);
                    	    popupStage.setScene(scene);
                    	    popupStage.show();
                    	}
                    	else {
                    		welcomeLoginPage.show(primaryStage, user); 
                    	}
                    } 
                    else if (password.equals(databaseHelper.getOneTimePassword(userName))) {
                        databaseHelper.clearOneTimePassword(userName); 
                        resetPassword(userName);
                    } 
                    else {
                        errorLabel.setText("Error logging in");
                    }
                } else {
                    errorLabel.setText("User account doesn't exist.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, loginButton, errorLabel, backButton);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
    
    /**
     * Prompts the user to reset their password if they logged in using a one-time password.
     *
     * @param userName The username of the user resetting their password.
     */
    private void resetPassword(String userName) {
        Stage resetPasswordStage = new Stage();
        resetPasswordStage.setTitle("Reset Password");

        Label messageLabel = new Label("Enter a new password:");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");

        Button resetButton = new Button("Reset Password");

        resetButton.setOnAction(a -> {
            String newPassword = newPasswordField.getText();
            if (!newPassword.isEmpty()) {
                databaseHelper.updatePassword(userName, newPassword); 
                showPopup("Password Updated", "Your password has been changed. Please log in again.", 
                          () -> resetPasswordStage.close());
            }
        });

        VBox layout = new VBox(10, messageLabel, newPasswordField, resetButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 150);
        resetPasswordStage.setScene(scene);
        resetPasswordStage.show();
    }
    
    /**
     * Shows a confirmation popup with a Yes/No option and handles the user's choice.
     *
     * @param title     The title of the popup window.
     * @param message   The message to display.
     * @param yesAction The action to execute if the user clicks "Yes".
     */
	private void showPopup(String title, String message, Runnable yesAction) {
		Stage popupStage = new Stage();
		popupStage.setTitle(title);
		
		Label messageLabel = new Label(message);
		
		Button yesButton = new Button("Yes");
		Button noButton = new Button("No");
		
		yesButton.setOnAction(e -> {
			if (yesAction != null) {
				yesAction.run();
			}
			popupStage.close();
		});
		
		noButton.setOnAction(e -> popupStage.close());
		
		VBox buttonBox = new VBox(10, yesButton, noButton);
	    buttonBox.setStyle("-fx-alignment: center;");

	    VBox layout = new VBox(10, messageLabel, buttonBox);
	    layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

	    Scene scene = new Scene(layout, 300, 150);
	    popupStage.setScene(scene);
	    popupStage.show();
	}
}
