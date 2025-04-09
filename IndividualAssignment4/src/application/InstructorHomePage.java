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

public class InstructorHomePage {

	private final DatabaseHelper databaseHelper;

	/**
     * Constructs the InstructorHomePage with the specified DatabaseHelper.
     *
     * @param databaseHelper the database helper used for database interactions
     */
	public InstructorHomePage(DatabaseHelper databaseHelper) {
	    this.databaseHelper = databaseHelper;
	}
	
	/**
     * Displays the instructor home page interface in the provided primary stage.
     * Shows a welcome message and a logout button to return to the login selection screen.
     *
     * @param primaryStage the primary JavaFX stage
     * @param user the currently logged-in user
     */
    public void show(Stage primaryStage, User user) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, Instructor!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(a -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));
	    
	    layout.getChildren().addAll(userLabel, logoutButton);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("Instructor Page");
    	
    }
}