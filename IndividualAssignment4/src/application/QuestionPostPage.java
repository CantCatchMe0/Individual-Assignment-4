package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.User;

/**
 * The QuestionPostPage class provides the main interface for users to interact with the Q&A Forum.
 * Users can post questions, view and answer questions, and interact with reviews based on their role.
 */
public class QuestionPostPage{
	private DatabaseHelper databaseHelper;
	private String userName;
	private User user;
	private Question question;
	
	/**
     * Displays the Q&A Forum page.
     *
     * @param databaseHelper The database helper instance for DB interactions.
     * @param primaryStage The primary stage of the application.
     * @param user The current logged-in user.
     */
    public void show(DatabaseHelper databaseHelper, Stage primaryStage, User user) {
        this.databaseHelper = databaseHelper;
        this.userName = user.getUserName();
        this.user = user;

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label headerLabel = new Label("Q&A Forum");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField titleField = new TextField();
        TextArea contentField = new TextArea();

        titleField.setPromptText("Enter your question title");
        contentField.setPromptText("Enter your complete question here");

        titleField.setMinWidth(300);
        contentField.setMinWidth(300);
        contentField.setMaxHeight(100);

        Button submitButton = new Button("Post Question");
        submitButton.setOnAction(e -> postQuestion(titleField, contentField));
        
        Button reportButton = new Button("View Flagged Report");
        reportButton.setOnAction(e -> showFlaggedReportPopup());

        Button viewQuestionsButton = new Button("View Questions");
        viewQuestionsButton.setOnAction(e -> showQuestions(primaryStage));

        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> navigateBack(primaryStage));

        layout.getChildren().addAll(headerLabel, titleField, contentField, submitButton, viewQuestionsButton, reportButton, backButton);
        primaryStage.setScene(new Scene(layout, 800, 500));
        primaryStage.setTitle("Q&A Forum");
        primaryStage.show();
    }
    
    /**
     * Posts a question submitted by the user.
     *
     * @param titleField TextField containing the question title.
     * @param contentField TextArea containing the question content.
     */
    private void postQuestion(TextField titleField, TextArea contentField) {
        String title = titleField.getText().trim();
        String content = contentField.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert("Error", "Title or content cannot be empty.");
            return;
        }

        try {
            Question question = new Question(title, content, userName);
            databaseHelper.postQuestion(question);
            showAlert("Success", "Your question has been posted.");
            titleField.clear();
            contentField.clear();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not post your question.");
            e.printStackTrace();
        }
    }
    
    private void showFlaggedReportPopup() {
        Stage popup = new Stage();
        popup.setTitle("Flagged Report");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        List<Question> flaggedQs = databaseHelper.getFlaggedQuestions();
        List<Answer> flaggedAs = databaseHelper.getFlaggedAnswers();

        layout.getChildren().add(new Label("📌 Flagged Questions:"));
        for (Question q : flaggedQs) {
            layout.getChildren().add(new Label("Title: " + q.getTitle() + "\nPosted by: " + q.getPostedBy()));
        }

        layout.getChildren().add(new Label("\n📌 Flagged Answers:"));
        for (Answer a : flaggedAs) {
            layout.getChildren().add(new Label("Text: " + a.getText() + "\nPosted by: " + a.getPostedBy()));
        }

        popup.setScene(new Scene(new ScrollPane(layout), 500, 400));
        popup.show();
    }

    
    /**
     * Displays all questions available in the forum.
     *
     * @param primaryStage The stage to display the questions in.
     */
    private void showQuestions(Stage primaryStage) {
        QuestionList questionList = databaseHelper.listAllQuestions();
        if (questionList == null || questionList.getQuestions().isEmpty()) {
            showAlert("No Questions", "No questions have been posted yet.");
            return;
        }
        
        ArrayList<Integer> questionIDs = questionList.getIDs();
        if (questionIDs == null || questionIDs.isEmpty()) {
            showAlert("Error", "No valid question IDs found.");
            return;
        }

        Stage questionStage = new Stage();
        questionStage.setTitle("View Questions");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        for (int i = 0; i < questionList.getQuestions().size(); i++) {
            String questionText = questionList.getQuestions().get(i);
            
            if (i >= questionIDs.size()) {
                continue;
            }
            
            int questionID = questionIDs.get(i);
            String title = databaseHelper.getQuestionTitle(questionID); 
            String fullText = databaseHelper.getQuestionText(questionID); 

            String postedBy = databaseHelper.getQuestionPoster(questionID);

            Label questionLabel = new Label(title);
            questionLabel.setWrapText(true);
            
            Label questionTextLabel = new Label(fullText);
            questionTextLabel.setWrapText(true);
            
            Button editButton = new Button("Edit");
            TextArea editField = new TextArea(title);
            editField.setVisible(false);
            Button saveButton = new Button("Save");
            saveButton.setVisible(false);

            if (!userName.equals(postedBy)) {
                editButton.setDisable(true);
            }
            editButton.setOnAction(e -> {
                editField.setVisible(true);
                saveButton.setVisible(true);
                editButton.setVisible(false);
            });
            saveButton.setOnAction(e -> {
                String updatedTitle = editField.getText().trim();
                if (!updatedTitle.isEmpty()) {
                    databaseHelper.updateQuestionTitle(questionID, updatedTitle);
                    questionLabel.setText(updatedTitle);
                    editField.setVisible(false);
                    saveButton.setVisible(false);
                    editButton.setVisible(true);
                } else {
                    showAlert("Error", "Question text cannot be empty.");
                }
            });
            
            Button answerButton = new Button("Answer");
            answerButton.setOnAction(e -> showAnswerPopup(questionID));

            Button viewAnswersButton = new Button("View Answers");
            viewAnswersButton.setOnAction(e -> showAnswers(primaryStage, questionID));
            
            Button viewReviewsButton = new Button("View Reviews");
            viewReviewsButton.setOnAction(e -> showReviewsPopup(questionID, null));

            VBox questionBox = new VBox(5, questionLabel, questionTextLabel, answerButton, viewAnswersButton, editButton, editField, saveButton, viewReviewsButton);
            layout.getChildren().add(questionBox);
            
            if (user.getRole().contains("reviewer") || user.getRole().contains("admin")) {
                Button reviewQuestionButton = new Button("Review Question");
                reviewQuestionButton.setOnAction(e -> showReviewPopup(questionID, "question"));

                questionBox.getChildren().add(reviewQuestionButton);
            }
            
            if (user.getRole().contains("reviewer") || user.getRole().contains("admin")) {
                Button myReviewsButton = new Button("View My Reviews");
                myReviewsButton.setOnAction(e -> showMyReviewsPopup());
                layout.getChildren().add(myReviewsButton);
            }
            
            if (user.getRole().contains("staff")) {
            	Button flagButton = new Button(databaseHelper.isQuestionFlagged(questionID) ? "Unflag" : "Flag");
                flagButton.setOnAction(ev -> {
                    try {
                        databaseHelper.flagQuestion(questionID); 
                        showAlert("Flagged", "Question has been flagged.");
                    } catch (SQLException ex) {
                        showAlert("Error", "Failed to flag the question.");
                    }
                });
            	layout.getChildren().add(flagButton);
            	if (databaseHelper.isQuestionFlagged(questionID)) {
                    Label flaggedLabel = new Label("[FLAGGED]");
                    flaggedLabel.setStyle("-fx-text-fill: red;");
                    questionBox.getChildren().add(flaggedLabel);
                }
            }
            
            questionBox.setSpacing(2);
        }
        
        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true); 
        scrollPane.setPadding(new Insets(10));

        Scene scene = new Scene(scrollPane, 600, 400);
        questionStage.setScene(scene);
        questionStage.show();
    }
    
    /**
     * Shows a popup to let the user answer a specific question.
     *
     * @param questionID The ID of the question to answer.
     */
    private void showAnswerPopup(int questionID) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Post Answer");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label instructionLabel = new Label("Enter your answer:");
        TextArea answerField = new TextArea();
        answerField.setPromptText("Type your answer here...");
        answerField.setMaxWidth(300);
        answerField.setMaxHeight(100);

        Button submitButton = new Button("Submit Answer");
        submitButton.setOnAction(e -> {
            String answerText = answerField.getText().trim();

            if (answerText.isEmpty()) {
                showAlert("Error", "Answer cannot be empty.");
                return;
            }

            try {
                Answer answer = new Answer(answerText, userName, questionID);
                databaseHelper.postAnswer(answer);
                showAlert("Success", "Your answer has been posted!");
                popupStage.close();
            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to post answer.");
                ex.printStackTrace();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> popupStage.close());

        layout.getChildren().addAll(instructionLabel, answerField, submitButton, cancelButton);
        Scene scene = new Scene(layout, 400, 200);
        popupStage.setScene(scene);
        popupStage.show();
    }
    
    /**
     * Displays the list of answers to a specific question.
     *
     * @param primaryStage The stage to use for rendering the view.
     * @param questionID The ID of the question.
     */
    private void showAnswers(Stage primaryStage, int questionID) {
        AnswerList answerList = databaseHelper.viewAnswersToQuestion(questionID);
        
        if (answerList == null || answerList.getAnswers().isEmpty()) {
            showAlert("No Answers", "No answers have been posted for this question.");
            return;
        }
        
        ArrayList<Integer> answerIDs = answerList.getIDs();
        if (answerIDs == null || answerIDs.isEmpty()) {
            showAlert("Error", "No valid answer IDs found.");
            return;
        }

        Stage answerStage = new Stage();
        answerStage.setTitle("View Answers");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        for (int i = 0; i < answerList.getAnswers().size(); i++) {
            String answerText = answerList.getAnswers().get(i);
            
            if (i >= answerIDs.size()) {
                continue;
            }
            
            int answerID = answerList.getIDs().get(i);
            
            String postedBy = databaseHelper.getAnswerPoster(answerID);

            Label answerLabel = new Label(answerText);
            answerLabel.setWrapText(true);
            
            Button editButton = new Button("Edit");
            TextArea editField = new TextArea(answerText);
            editField.setVisible(false);
            Button saveButton = new Button("Save");
            saveButton.setVisible(false);
            
            if (!userName.equals(postedBy)) {
                editButton.setDisable(true);
            }

            editButton.setOnAction(e -> {
                editField.setVisible(true);
                saveButton.setVisible(true);
                editButton.setVisible(false);
            });

            saveButton.setOnAction(e -> {
                String updatedText = editField.getText().trim();
                if (!updatedText.isEmpty()) {
                    databaseHelper.updateAnswer(answerID, updatedText);
                    answerLabel.setText(updatedText);
                    editField.setVisible(false);
                    saveButton.setVisible(false);
                    editButton.setVisible(true);
                } else {
                    showAlert("Error", "Answer text cannot be empty.");
                }
            });
            
            int[] likesDislikes = databaseHelper.getLikesDislikes(answerID);
            int currentLikes = likesDislikes[0];
            int currentDislikes = likesDislikes[1];

            Label likeCountLabel = new Label("Likes: " + currentLikes);
            Label dislikeCountLabel = new Label("Dislikes: " + currentDislikes);

            Button likeButton = new Button("Like");
            Button dislikeButton = new Button("Dislike");

            final boolean[] liked = {false};
            final boolean[] disliked = {false};
            
            
            likeButton.setOnAction(e -> {
                if (liked[0]) {
                    databaseHelper.decrementLike(answerID);
                    liked[0] = false;
                    likeCountLabel.setText("Likes: " + (Integer.parseInt(likeCountLabel.getText().split(": ")[1]) - 1));
                } else {
                    databaseHelper.incrementLike(answerID);
                    liked[0] = true;
                    likeCountLabel.setText("Likes: " + (Integer.parseInt(likeCountLabel.getText().split(": ")[1]) + 1));
                    if (disliked[0]) {
                        databaseHelper.decrementDislike(answerID);
                        disliked[0] = false;
                        dislikeCountLabel.setText("Dislikes: " + (Integer.parseInt(dislikeCountLabel.getText().split(": ")[1]) - 1));
                    }
                }
            });

            dislikeButton.setOnAction(e -> {
                if (disliked[0]) {
                    databaseHelper.decrementDislike(answerID);
                    disliked[0] = false;
                    dislikeCountLabel.setText("Dislikes: " + (Integer.parseInt(dislikeCountLabel.getText().split(": ")[1]) - 1));
                } else {
                    databaseHelper.incrementDislike(answerID);
                    disliked[0] = true;
                    dislikeCountLabel.setText("Dislikes: " + (Integer.parseInt(dislikeCountLabel.getText().split(": ")[1]) + 1));
                    if (liked[0]) {
                        databaseHelper.decrementLike(answerID);
                        liked[0] = false;
                        likeCountLabel.setText("Likes: " + (Integer.parseInt(likeCountLabel.getText().split(": ")[1]) - 1));
                    }
                }
            });
            Button replyButton = new Button("Reply");
            replyButton.setOnAction(e -> showReplyPopup(answerID));
            
            HBox buttonBox = new HBox(10, likeButton, likeCountLabel, dislikeButton, dislikeCountLabel);
            HBox answerBox = new HBox(10, answerLabel, replyButton, editButton, editField, saveButton);
            
            if (user.getRole().contains("reviewer") || user.getRole().contains("admin")) {
                Button reviewAnswerButton = new Button("Review Answer");
                reviewAnswerButton.setOnAction(e -> showReviewPopup(answerID, "answer"));

                layout.getChildren().add(reviewAnswerButton);
            }
            
            Button viewReviewsButton = new Button("View Reviews");
            viewReviewsButton.setOnAction(e -> showReviewsPopup(null, answerID));
            
            layout.getChildren().add(viewReviewsButton);
            layout.getChildren().add(answerBox);
            layout.getChildren().add(buttonBox);
            if (user.getRole().contains("staff")) {
                Button flagButton = new Button(
                    databaseHelper.isAnswerFlagged(answerID) ? "Unflag" : "Flag"
                );
                
                flagButton.setOnAction(ev -> {
                    try {
                        databaseHelper.flagAnswer(answerID);
                        showAlert("Flagged", "Answer has been flagged.");
                    } catch (SQLException ex) {
                        showAlert("Error", "Failed to flag the answer.");
                        ex.printStackTrace();
                    }
                });

                Label flaggedLabel = null;
                if (databaseHelper.isAnswerFlagged(answerID)) {
                    flaggedLabel = new Label("[FLAGGED]");
                    flaggedLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }

                layout.getChildren().addAll(flagButton);
                if (flaggedLabel != null) {
                    layout.getChildren().add(flaggedLabel);
                }
            }

        }

        Scene scene = new Scene(layout, 600, 500);
        answerStage.setScene(scene);
        answerStage.show();
    }
    
    /**
     * Shows a reply popup to respond to an answer.
     *
     * @param answerID The ID of the answer being replied to.
     */
    private void showReplyPopup(int answerID) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Reply to Answer");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label instructionLabel = new Label("Enter your reply:");
        TextArea replyField = new TextArea();
        replyField.setPromptText("Type your reply here...");
        replyField.setMaxWidth(300);
        replyField.setMaxHeight(100);

        Button submitButton = new Button("Submit Reply");
        submitButton.setOnAction(e -> {
            String replyText = replyField.getText().trim();

            if (replyText.isEmpty()) {
                showAlert("Error", "Reply cannot be empty.");
                return;
            }

            try {
                databaseHelper.postReply(answerID, userName, replyText);
                showAlert("Success", "Your reply has been posted!");
                popupStage.close();
            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to post reply.");
                ex.printStackTrace();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> popupStage.close());

        layout.getChildren().addAll(instructionLabel, replyField, submitButton, cancelButton);
        Scene scene = new Scene(layout, 400, 200);
        popupStage.setScene(scene);
        popupStage.show();
    }
    
    /**
     * Navigates back to the home page based on the user's role.
     *
     * @param primaryStage The primary application stage.
     */
    private void navigateBack(Stage primaryStage) {
        new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
    }
    
    /**
     * Displays an alert with a specific title and message.
     *
     * @param title The title of the alert.
     * @param message The content of the alert.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows a popup for leaving a review on a question or answer.
     *
     * @param targetID The ID of the question or answer.
     * @param type "question" or "answer" to determine the context.
     */
    private void showReviewPopup(int targetID, String type) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Leave a Review");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label instructionLabel = new Label("Write your review:");
        TextArea reviewField = new TextArea();
        reviewField.setPromptText("Type your review here...");
        reviewField.setMaxWidth(300);
        reviewField.setMaxHeight(100);

        Button submitButton = new Button("Submit Review");
        submitButton.setOnAction(e -> {
            String reviewText = reviewField.getText().trim();

            if (reviewText.isEmpty()) {
                showAlert("Error", "Review cannot be empty.");
                return;
            }

            try {
                Review review;
                if (type.equals("question")) {
                    review = new Review(reviewText, user.getUserName(), targetID, null);
                } else {
                    review = new Review(reviewText, user.getUserName(), null, targetID);
                }

                databaseHelper.addReview(review);
                showAlert("Success", "Review submitted successfully.");
                popupStage.close();
            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to submit review.");
                ex.printStackTrace();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> popupStage.close());

        layout.getChildren().addAll(instructionLabel, reviewField, submitButton, cancelButton);
        Scene scene = new Scene(layout, 400, 200);
        popupStage.setScene(scene);
        popupStage.show();
    }
    
    /**
     * Displays the reviews for a given question or answer.
     *
     * @param questionID The ID of the question (nullable).
     * @param answerID The ID of the answer (nullable).
     */
    private void showReviewsPopup(Integer questionID, Integer answerID) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Reviews");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        CheckBox trustedOnlyToggle = new CheckBox("Show only trusted reviewers");

        VBox reviewsContainer = new VBox(10);

        final Runnable[] refreshReviews = new Runnable[1];
        refreshReviews[0] = () -> {
            reviewsContainer.getChildren().clear();
            List<Review> reviews = trustedOnlyToggle.isSelected()
                    ? databaseHelper.getTrustedReviews(userName, questionID, answerID)
                    : databaseHelper.getReviews(questionID, answerID);
            
            if (user.getRole().contains("student")) {
                reviews.sort((r1, r2) -> Integer.compare(
                        databaseHelper.getReviewerWeight(userName, r2.getReviewer()),
                        databaseHelper.getReviewerWeight(userName, r1.getReviewer())
                ));
            }

            if (reviews.isEmpty()) {
                reviewsContainer.getChildren().add(new Label("There are no reviews for this post yet."));
            } else {
                for (Review r : reviews) {
                    Label reviewLabel = new Label("\"" + r.getText() + "\" - by " + r.getReviewer());
                    reviewLabel.setWrapText(true);
                    HBox reviewBox = new HBox(10, reviewLabel);
                    reviewBox.setStyle("-fx-padding: 10;");

                    if (r.getReviewer().equals(userName)) {
                        Button editBtn = new Button("Edit");
                        Button deleteBtn = new Button("Delete");

                        editBtn.setOnAction(e -> {
                            popupStage.close();
                            showEditReviewPopup(r);
                        });

                        deleteBtn.setOnAction(e -> {
                            try {
                                databaseHelper.deleteReview(r.getId());
                                showAlert("Deleted", "Review deleted.");
                                refreshReviews[0].run(); 
                            } catch (SQLException ex) {
                                showAlert("Error", "Could not delete review.");
                                ex.printStackTrace();
                            }
                        });

                        reviewBox.getChildren().addAll(editBtn, deleteBtn);
                    }

                    if (user.getRole().equals("student") || user.getRole().equals("admin")) {
                        Button messageButton = new Button("Message Reviewer");
                        messageButton.setOnAction(e -> showMessagePopup(r.getReviewer()));
                        reviewBox.getChildren().add(messageButton);

                        boolean isTrusted = databaseHelper.isTrustedReviewer(userName, r.getReviewer());
                        Button trustToggleButton = new Button(isTrusted ? "Untrust Reviewer" : "Trust Reviewer");

                        trustToggleButton.setOnAction(e -> {
                            try {
                                if (isTrusted) {
                                    databaseHelper.removeTrustedReviewer(userName, r.getReviewer());
                                    showAlert("Success", "Reviewer untrusted.");
                                } else {
                                    databaseHelper.addTrustedReviewer(userName, r.getReviewer());
                                    showAlert("Success", "Reviewer trusted.");
                                }
                                refreshReviews[0].run();  
                            } catch (SQLException ex) {
                                showAlert("Error", "Could not update trust status.");
                                ex.printStackTrace();
                            }
                        });

                        reviewBox.getChildren().add(trustToggleButton);
                        
                        int currentWeight = databaseHelper.getReviewerWeight(userName, r.getReviewer());
                        Spinner<Integer> weightSpinner = new Spinner<>(0, 10, currentWeight);
                        Button setWeightButton = new Button("Set Weight");

                        setWeightButton.setOnAction(e -> {
                            try {
                                databaseHelper.setReviewerWeight(userName, r.getReviewer(), weightSpinner.getValue());
                                showAlert("Weight Set", "Weight saved.");
                                refreshReviews[0].run();
                            } catch (SQLException ex) {
                                showAlert("Error", "Could not update weight.");
                                ex.printStackTrace();
                            }
                        });

                        reviewBox.getChildren().addAll(new Label("Weight:"), weightSpinner, setWeightButton);
                    }

                    reviewsContainer.getChildren().add(reviewBox);
                }
            }
        };

        trustedOnlyToggle.setOnAction(e -> refreshReviews[0].run());
        refreshReviews[0].run();

        layout.getChildren().addAll(trustedOnlyToggle, reviewsContainer);
        popupStage.setScene(new Scene(new ScrollPane(layout), 400, 300));
        popupStage.show();
    }
    
    /**
     * Opens the review editing popup for a user's own review.
     *
     * @param review The review to be edited.
     */
    private void showEditReviewPopup(Review review) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Edit Review");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        TextArea textArea = new TextArea(review.getText());
        Button saveButton = new Button("Save");

        saveButton.setOnAction(e -> {
            String newText = textArea.getText().trim();
            if (!newText.isEmpty()) {
                review.setText(newText);
                try {
                    databaseHelper.updateReview(review);
                    showAlert("Updated", "Review updated successfully!");
                    popupStage.close();
                } catch (SQLException ex) {
                    showAlert("Database Error", "Could not update review.");
                    ex.printStackTrace();
                }
            } else {
                showAlert("Error", "Review cannot be empty.");
            }
        });

        layout.getChildren().addAll(new Label("Edit your review:"), textArea, saveButton);
        popupStage.setScene(new Scene(layout, 400, 200));
        popupStage.show();
    }
    
    /**
     * Shows a message popup to send a private message to a reviewer.
     *
     * @param recipient The username of the reviewer to message.
     */
    private void showMessagePopup(String recipient) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Send Message to " + recipient);

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        TextArea messageField = new TextArea();
        messageField.setPromptText("Write your private message here...");
        messageField.setWrapText(true);

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            String message = messageField.getText().trim();
            if (message.isEmpty()) {
                showAlert("Error", "Message cannot be empty.");
                return;
            }

            databaseHelper.sendMessage(userName, recipient, message);
			showAlert("Success", "Message sent to " + recipient);
			popupStage.close();
        });

        layout.getChildren().addAll(new Label("Message to " + recipient + ":"), messageField, sendButton);
        popupStage.setScene(new Scene(layout, 400, 200));
        popupStage.show();
    }
    
    /**
     * Displays all reviews written by the current user.
     */
    private void showMyReviewsPopup() {
        Stage myReviewsStage = new Stage();
        myReviewsStage.setTitle("My Reviews");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        List<Review> myReviews = databaseHelper.getReviewsByReviewer(userName);
        if (myReviews.isEmpty()) {
            layout.getChildren().add(new Label("You haven't written any reviews yet."));
        } else {
            for (Review r : myReviews) {
                String label = "\"" + r.getText() + "\" - on ";
                if (r.getQuestionID() != null) label += "Question ID #" + r.getQuestionID();
                if (r.getAnswerID() != null) label += "Answer ID #" + r.getAnswerID();
                layout.getChildren().add(new Label(label));
            }
        }

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> myReviewsStage.close());
        layout.getChildren().add(closeBtn);

        myReviewsStage.setScene(new Scene(new ScrollPane(layout), 400, 300));
        myReviewsStage.show();
    }
    
    private void flagProblem(int targetID, String type ) {
    	
    }
    
}