package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {
	
	private final DatabaseHelper databaseHelper;
	
	/**
     * Constructs a new UserHomePage with the specified database helper.
     *
     * @param databaseHelper The database helper used to manage database interactions.
     */
	public UserHomePage(DatabaseHelper databaseHelper) {
	    this.databaseHelper = databaseHelper;
	}
	
	/**
     * Displays the user home page in the given primary stage.
     *
     * @param primaryStage The main application window.
     * @param user         The current logged-in user.
     */
    public void show(Stage primaryStage, User user) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, User!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(a -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));
	    
	    layout.getChildren().addAll(userLabel, logoutButton);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("User Page");
	    
    	
    }
  
}