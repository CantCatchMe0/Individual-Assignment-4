package application;

import java.sql.SQLException;
import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays a simple welcome message for the user.
 */

public class StudentHomePage {

	private final DatabaseHelper databaseHelper;
	private String userName;
	
	/**
     * Constructs a StudentHomePage with the provided database helper.
     *
     * @param databaseHelper The database helper for interacting with stored data.
     */
	public StudentHomePage(DatabaseHelper databaseHelper) {
	    this.databaseHelper = databaseHelper;
	}
	
	/**
     * Displays the student home page in the given primary stage.
     *
     * @param primaryStage The main JavaFX stage.
     * @param user         The logged-in student user.
     */
    public void show(Stage primaryStage, User user) {
    	userName = user.getUserName();
    	
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, Student!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(a -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));
	    
	    Button goToQAButton = new Button("Go to Q&A Forum");
        goToQAButton.setOnAction(e -> new QuestionPostPage().show(databaseHelper, primaryStage, user));
        
        Button requestReviewerButton = new Button("Request Reviewer Role");
        requestReviewerButton.setOnAction(e -> handleReviewerRequest());
        
        Button messagesButton = new Button("Messages");
        messagesButton.setOnAction(e -> {
            PrivateMessagingPage messagingPage = new PrivateMessagingPage();
            messagingPage.show(databaseHelper, primaryStage, userName);
        });
        layout.getChildren().add(messagesButton);
	    
	    layout.getChildren().addAll(userLabel, logoutButton, goToQAButton, requestReviewerButton);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("Student Page");
    	
    }
    
    private void handleReviewerRequest() {
        try {
            if (databaseHelper.hasPendingRequest(userName)) {
                showAlert("Request Pending", "You have already requested the reviewer role. Please wait for admin approval.");
            } else if (databaseHelper.isReviewer(userName)) {
                showAlert("Already a Reviewer", "You already have reviewer privileges.");
            } else {
                databaseHelper.insertReviewerRequest(userName);
                showAlert("Request Submitted", "Your request to become a reviewer has been submitted.");
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Could not process your request.");
            ex.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}