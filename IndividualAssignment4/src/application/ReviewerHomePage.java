package application;

import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays a simple welcome message for the user.
 */

public class ReviewerHomePage {
	
	private final DatabaseHelper databaseHelper;
	
	/**
     * Constructs a ReviewerHomePage with the provided DatabaseHelper.
     *
     * @param databaseHelper An instance of DatabaseHelper for database interactions.
     */
	public ReviewerHomePage(DatabaseHelper databaseHelper) {
	    this.databaseHelper = databaseHelper;
	}
	
	/**
     * Displays the reviewer home page interface.
     *
     * @param primaryStage The primary JavaFX stage where this scene is displayed.
     * @param user         The currently logged-in user (reviewer).
     */
    public void show(Stage primaryStage, User user) {
    	String userName = user.getUserName();
    	
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, Reviewer!");
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
	    
	    layout.getChildren().addAll(userLabel, logoutButton, goToQAButton);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("Reviewer Page");
    	
    }
}