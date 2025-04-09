package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * The PrivateMessagingPage class displays a user's private message threads
 * and allows them to initiate or continue chats with others.
 */
public class PrivateMessagingPage {

	/**
     * Displays the private messaging interface for the given user.
     *
     * @param db the database helper used to fetch user and message data
     * @param primaryStage the primary stage where the scene will be displayed
     * @param currentUser the username of the user accessing the private messaging page
     */
    public void show(DatabaseHelper db, Stage primaryStage, String currentUser) {
    	User user = db.getUserByUserName(currentUser);
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        Label title = new Label("Your Private Message Threads");

        layout.getChildren().add(title);

        List<String> users = db.getChatPartners(currentUser);
        if (users.isEmpty()) {
            layout.getChildren().add(new Label("No message threads yet."));
        }

        for (String otherUser : users) {
            Button chatButton = new Button("Chat with " + otherUser);
            chatButton.setOnAction(e -> {
                MessageThreadPage threadPage = new MessageThreadPage();
                threadPage.show(db, primaryStage, currentUser, otherUser);
            });
            layout.getChildren().add(chatButton);
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new WelcomeLoginPage(db).show(primaryStage, user));

        layout.getChildren().add(backButton);

        primaryStage.setScene(new Scene(layout, 400, 400));
        primaryStage.setTitle("Private Messaging");
        primaryStage.show();
    }
}