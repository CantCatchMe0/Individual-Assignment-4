package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * The MessageThreadPage class displays a conversation between two users.
 * It shows the chat history and provides an interface to send new messages.
 */
public class MessageThreadPage {

	/**
     * Displays the message thread UI for the current user to chat with another user.
     *
     * @param db the database helper used to retrieve and send messages
     * @param primaryStage the primary JavaFX stage to display the UI
     * @param currentUser the username of the user viewing the thread
     * @param otherUser the username of the person they are chatting with
     */
    public void show(DatabaseHelper db, Stage primaryStage, String currentUser, String otherUser) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20;");

        Label header = new Label("Chat with " + otherUser);
        layout.getChildren().add(header);

        List<Message> messages = db.getMessages(currentUser, otherUser);
        for (Message msg : messages) {
            Label msgLabel = new Label(msg.getSender() + ": " + msg.getContent());
            msgLabel.setWrapText(true);
            layout.getChildren().add(msgLabel);
        }

        TextField input = new TextField();
        input.setPromptText("Type your message...");

        Button send = new Button("Send");
        send.setOnAction(e -> {
            String content = input.getText().trim();
            if (!content.isEmpty()) {
                db.sendMessage(currentUser, otherUser, content);
                show(db, primaryStage, currentUser, otherUser); 
            }
        });

        layout.getChildren().addAll(input, send);
        primaryStage.setScene(new Scene(new ScrollPane(layout), 500, 500));
        primaryStage.setTitle("Chat with " + otherUser);
        primaryStage.show();
    }
}
