package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays a simple welcome message for the user.
 */

public class StaffHomePage {
	
	private final DatabaseHelper databaseHelper;
	
	/**
     * Constructs a StaffHomePage with the given DatabaseHelper instance.
     *
     * @param databaseHelper The database helper used for interacting with the database.
     */
	public StaffHomePage(DatabaseHelper databaseHelper) {
	    this.databaseHelper = databaseHelper;
	}
	
	/**
     * Displays the staff home page interface on the provided primary stage.
     *
     * @param primaryStage The main JavaFX stage to display the scene.
     * @param user         The logged-in staff user.
     */
    public void show(Stage primaryStage, User user) {
    	String userName = user.getUserName();
    	
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, Staff!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(a -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));
	    
	    Button goToQAButton = new Button("Go to Q&A Forum");
        goToQAButton.setOnAction(e -> new QuestionPostPage().show(databaseHelper, primaryStage, user));
        
        Button messagesButton = new Button("Messages");
        messagesButton.setOnAction(e -> {
            PrivateMessagingPage messagingPage = new PrivateMessagingPage();
            messagingPage.show(databaseHelper, primaryStage, userName);
        });
        layout.getChildren().add(messagesButton);
	    
	    layout.getChildren().addAll(userLabel, logoutButton,goToQAButton);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("Staff Page");
    	
    }
}