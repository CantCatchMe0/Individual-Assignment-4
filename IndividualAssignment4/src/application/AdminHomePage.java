package application;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import databasePart1.DatabaseHelper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import application.User;


/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */

public class AdminHomePage {
	/**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
	private DatabaseHelper databaseHelper;
	private String userName;
	
	
    /**
     * Constructs the AdminHomePage with a reference to the DatabaseHelper.
     * 
     * @param databaseHelper the database helper instance
     */
	public AdminHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
	
    /**
     * Displays the AdminHomePage interface on the provided stage.
     * 
     * @param primaryStage the main JavaFX window
     * @param user the current admin user
     */
    public void show(Stage primaryStage, User user) {
    	userName = user.getUserName();
    	VBox layout = new VBox();
    	
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // label to display the welcome message for the admin
	    Label adminLabel = new Label("Hello, Admin!");
	    
	    adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    Button listUsersButton = new Button("List Users");
	    listUsersButton.setOnAction(a -> {
	    	showUserListWindow();
	    });
	    
	    Button generateOneTimePasswordButton = new Button("Generate One Time Password for User");
	    generateOneTimePasswordButton.setOnAction(a -> generateOneTimePasswordPopup());
	    
	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(a -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));
	    
	    Button goToQAButton = new Button("Go to Q&A Forum");
        goToQAButton.setOnAction(e -> new QuestionPostPage().show(databaseHelper, primaryStage, user));
        
        Button manageRequestsButton = new Button("Manage Reviewer Requests");
        manageRequestsButton.setOnAction(e -> {
			try {
				showReviewerRequestsPage();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		});
        
        Button messagesButton = new Button("Messages");
        messagesButton.setOnAction(e -> {
            PrivateMessagingPage messagingPage = new PrivateMessagingPage();
            messagingPage.show(databaseHelper, primaryStage, userName);
        });
        layout.getChildren().add(messagesButton);
	    
	    layout.getChildren().addAll(adminLabel, listUsersButton, generateOneTimePasswordButton, logoutButton, goToQAButton, manageRequestsButton);
	    Scene adminScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(adminScene);
	    primaryStage.setTitle("Admin Page");
    }
    
    /**
     * Displays a table listing all users along with options to edit or delete them.
     */
    private void showUserListWindow() {
    	Stage userListStage = new Stage();
    	userListStage.setTitle("User List");
    	
    	TableView<User> tableView = new TableView<>();
    	
    	TableColumn<User, String> firstNameColumn = new TableColumn<>("First Name");
    	firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
    	
    	TableColumn<User, String> lastNameColumn = new TableColumn<>("Last Name");
    	lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
    	
    	TableColumn<User, String> userNameColumn = new TableColumn<>("User Name");
    	userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
    	
    	TableColumn<User, String> emailColumn = new TableColumn<>("Email");
    	emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
    	
    	TableColumn<User, String> roleColumn = new TableColumn<>("Role");
    	roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
    	
    	TableColumn<User, Void> editRolesColumn = new TableColumn<>("Add/Remove Roles");
    	editRolesColumn.setCellFactory(column -> new TableCell<>() {
    		private final Button editRoleButton = new Button("Add/Remove Roles");
    		{
    			editRoleButton.setOnAction(a -> {
    				User user = getTableView().getItems().get(getIndex());
    				
    				if (user.getRole().contains("admin")) {
    	                showPopup("Action Not Allowed", "You cannot edit an admin's roles.", null);
    	                return;
    	            }
    				
    				showEditRolesCheckbox(user, getTableView());
    			});
    		}
    		
    		@Override
    		protected void updateItem(Void item, boolean empty) {
    			super.updateItem(item, empty);
    			
    			if(empty) {
    				setGraphic(null);
    			} else {
    				User user = getTableView().getItems().get(getIndex());
    				editRoleButton.setDisable("admin".equalsIgnoreCase(user.getRole()));
    				setGraphic(editRoleButton);
    			}
    		}
    	});
    	
    	TableColumn<User, Void> deleteColumn = new TableColumn<>("Delete");
    	deleteColumn.setCellFactory(column -> new TableCell<>() {
    		private final Button deleteButton = new Button("Delete");
    		{
    			deleteButton.setOnAction(a ->{
    				User user = getTableView().getItems().get(getIndex());
    				
    				if("admin".equalsIgnoreCase(user.getRole())) {
    					showPopup("Action Not Allowed", "Admins cannot be deleted.", null);
                        return;
    				}
    				
    				showPopup("Confirm Deletion", "Are you sure you want to delete the user " + user.getUserName() + "?",
    						() -> {
    							databaseHelper.deleteUser(user.getUserName());
    							tableView.getItems().remove(user);
    						});
    			});
    		}
    		
    		@Override
    		protected void updateItem(Void item, boolean empty) {
    			super.updateItem(item, empty);
    			
    			if(empty) {
    				setGraphic(null);
    			} else {
    				User user = getTableView().getItems().get(getIndex());
    				deleteButton.setDisable("admin".equalsIgnoreCase(user.getRole()));
    				setGraphic(deleteButton);
    			}
    		}
    	});
    	
    	tableView.getColumns().add(firstNameColumn);
    	tableView.getColumns().add(lastNameColumn);
    	tableView.getColumns().add(userNameColumn);
    	tableView.getColumns().add(emailColumn);
    	tableView.getColumns().add(roleColumn);
    	tableView.getColumns().add(editRolesColumn);
    	tableView.getColumns().add(deleteColumn);
    	
    	List<User> users = databaseHelper.getUsersFromDatabase();
    	tableView.getItems().addAll(users);
    	
    	VBox layout = new VBox(10);
    	layout.setStyle("-fx-padding: 15;");
    	layout.getChildren().add(tableView);
    	
    	Scene scene = new Scene(layout, 400, 300);
    	userListStage.setScene(scene);
    	userListStage.show();
    }
    	
    
    	/**
     	* Shows a confirmation dialog.
     	* 
     	* @param title the title of the popup
     	* @param message the confirmation message
     	* @param yesAction the action to perform if "Yes" is clicked
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
    	
    	
        /**
         * Opens a window to allow editing roles for a selected user.
         * 
         * @param user the user whose roles are being edited
         * @param tableView the TableView to refresh after changes
         */
    	private void showEditRolesCheckbox(User user, TableView<User> tableView) {
    		Stage checkboxStage = new Stage();
    		checkboxStage.setTitle("Edit Roles");
    		
    		Label messageLabel = new Label("Select roles for: " + user.getUserName());
    		
    		List<String> availableRoles = Arrays.asList("admin", "student", "instructor", "staff", "reviewer");
    	    ListView<CheckBox> roleListView = new ListView<>();
    	    
    	    List<String> userRoles = Arrays.asList(user.getRole().split(", "));
    	    
    	    for (String role : availableRoles) {
    	        CheckBox checkBox = new CheckBox(role);
    	        checkBox.setSelected(userRoles.contains(role));
    	        roleListView.getItems().add(checkBox);
    	    }
    	    
    	    Button saveButton = new Button("Save");
    	    Button cancelButton = new Button("Cancel");
    	    
    	    saveButton.setDisable(userRoles.isEmpty());
    	    
    	    roleListView.getItems().forEach(checkBox -> checkBox.setOnAction(a -> {
    	    	long selectedCount = roleListView.getItems().stream().filter(CheckBox::isSelected).count();
    	    	saveButton.setDisable(selectedCount == 0);
    	    }));
    	    
    	    saveButton.setOnAction(a -> {
    	    	 List<String> selectedRoles = roleListView.getItems().stream()
    	                 .filter(CheckBox::isSelected)
    	                 .map(CheckBox::getText)
    	                 .toList();
    	    	 
    	    	 if (selectedRoles.isEmpty()) {
    	    		 checkboxStage.close();
    	    		 return;
    	    	 }
    	    	 
    	    	 String newRoles = String.join(", ", selectedRoles);
    	    	 
    	    	if(!newRoles.equals(user.getRole())) {
    	    		databaseHelper.updateUserRole(user.getUserName(), newRoles);
    	    		user.setRole(newRoles);
    	    		tableView.refresh();
    	    	}
    	    	checkboxStage.close();
    	    });
    	    
    	    cancelButton.setOnAction(a -> checkboxStage.close());
    	    
    	    VBox buttonBox = new VBox(10, saveButton, cancelButton);
    	    buttonBox.setStyle("-fx-alignment: center;");

    	    VBox layout = new VBox(10, messageLabel, roleListView, buttonBox);
    	    layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

    	    Scene scene = new Scene(layout, 300, 250);
    	    checkboxStage.setScene(scene);
    	    checkboxStage.show();
    	}
    	
    	
    	/**
         * Displays a popup to generate a one-time password for a specific user.
         */
    	private void generateOneTimePasswordPopup() {
    		Stage popupStage = new Stage();
    		popupStage.setTitle("Generate One Time Password");
    		
    		Label usernameLabel = new Label("Enter username: ");
    		TextField userNameField = new TextField();
    		userNameField.setPromptText("Username");
    		
    		Button button = new Button("Generate One Time Password");
    		Button cancelButton = new Button("Cancel");
    		
    		Label resultLabel = new Label();
    		
    		button.setOnAction(a -> {
    			String userName = userNameField.getText().trim();
    			
    			if(userName.isEmpty()) {
    				resultLabel.setText("Please input User Name");
    				return;
    			}
    			
    			if(!databaseHelper.doesUserExist(userName)) {
    				resultLabel.setText("User not found");
    				return;
    			}
    			
    			if(databaseHelper.isUserAdmin(userName)) {
    				resultLabel.setText("You can't reset an admin's password");
    				return;
    			}
    			
    			String oneTimePassword = databaseHelper.generateOneTimePassword(userName);
    			resultLabel.setText("One Time Password for " + userName + ":\n" + oneTimePassword);
    		});
    		
    		cancelButton.setOnAction(a -> popupStage.close());
    		
    		VBox layout = new VBox(10, usernameLabel, userNameField, button, cancelButton, resultLabel);
    	    layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

    	    Scene scene = new Scene(layout, 350, 250);
    	    popupStage.setScene(scene);
    	    popupStage.show();
    	}
    	
    	
    	/**
         * Displays the pending reviewer requests and provides approve/deny options.
         * 
         * @throws SQLException if database error occurs
         */
    	private void showReviewerRequestsPage() throws SQLException {
    	    Stage requestStage = new Stage();
    	    requestStage.setTitle("Reviewer Requests");

    	    VBox layout = new VBox(10);
    	    layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

    	    List<String> pendingRequests = databaseHelper.getPendingReviewerRequests();
    	    if (pendingRequests.isEmpty()) {
    	        layout.getChildren().add(new Label("No pending reviewer requests."));
    	    } else {
    	        for (String student : pendingRequests) {
    	            HBox requestBox = new HBox(10);
    	            requestBox.setAlignment(Pos.CENTER_LEFT);

    	            Label nameLabel = new Label(student);
    	            Button approveButton = new Button("Approve");
    	            Button denyButton = new Button("Deny");
    	            Button viewButton = new Button("View Contributions");

    	            approveButton.setOnAction(e -> handleApproval(student, requestBox));
    	            denyButton.setOnAction(e -> handleDenial(student, requestBox));
    	            viewButton.setOnAction(e -> showStudentContributions(student));

    	            requestBox.getChildren().addAll(nameLabel, approveButton, denyButton, viewButton);
    	            layout.getChildren().add(requestBox);
    	        }
    	    }

    	    Scene scene = new Scene(layout, 400, 300);
    	    requestStage.setScene(scene);
    	    requestStage.show();
    	}
    	
    	/**
         * Handles approval of a review request.
         * 
         * @param student the student requesting reviewer access
         * @param requestBox the UI component representing the request
         */
    	private void handleApproval(String student, HBox requestBox) {
    	    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Approve " + student + " as a reviewer?", ButtonType.YES, ButtonType.NO);
    	    confirmation.showAndWait().ifPresent(response -> {
    	        if (response == ButtonType.YES) {
    	            try {
    	                databaseHelper.addUserRole(student, "reviewer");
    	                databaseHelper.deleteReviewerRequest(student);
    	                ((VBox) requestBox.getParent()).getChildren().remove(requestBox);
    	                showAlert("Approved", student + " has been granted reviewer privileges.");
    	            } catch (SQLException ex) {
    	                showAlert("Error", "Could not approve request.");
    	                ex.printStackTrace();
    	            }
    	        }
    	    });
    	}
    	
    	/**
         * Handles denial of a review request.
         * 
         * @param student the student requesting reviewer access
         * @param requestBox the UI component representing the request
         */
    	private void handleDenial(String student, HBox requestBox) {
    	    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Deny " + student + "'s reviewer request?", ButtonType.YES, ButtonType.NO);
    	    confirmation.showAndWait().ifPresent(response -> {
    	        if (response == ButtonType.YES) {
    	            try {
    	                databaseHelper.deleteReviewerRequest(student);
    	                ((VBox) requestBox.getParent()).getChildren().remove(requestBox);
    	                showAlert("Denied", student + "'s reviewer request has been denied.");
    	            } catch (SQLException ex) {
    	                showAlert("Error", "Could not deny request.");
    	                ex.printStackTrace();
    	            }
    	        }
    	    });
    	}
    	
    	/**
         * Displays a popup showing all questions and answers posted by the specified student.
         * 
         * @param studentUserName the username of the student
         */
    	private void showStudentContributions(String studentUserName) {
    	    Stage contributionsStage = new Stage();
    	    contributionsStage.setTitle("Contributions by " + studentUserName);

    	    VBox layout = new VBox(10);
    	    layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

    	    QuestionList questions = databaseHelper.showQuestionsToManage(studentUserName);
    	    if (questions != null && !questions.getQuestions().isEmpty()) {
    	        Label qLabel = new Label("Questions:");
    	        layout.getChildren().add(qLabel);
    	        for (String q : questions.getQuestions()) {
    	            Label qItem = new Label(q);
    	            qItem.setWrapText(true);
    	            layout.getChildren().add(qItem);
    	        }
    	    } else {
    	        layout.getChildren().add(new Label("No questions found."));
    	    }

    	    AnswerList answers = databaseHelper.showAnswersToManage(studentUserName);
    	    if (answers != null && !answers.getAnswers().isEmpty()) {
    	        Label aLabel = new Label("Answers:");
    	        layout.getChildren().add(aLabel);
    	        for (String a : answers.getAnswers()) {
    	            Label aItem = new Label(a);
    	            aItem.setWrapText(true);
    	            layout.getChildren().add(aItem);
    	        }
    	    } else {
    	        layout.getChildren().add(new Label("No answers found."));
    	    }

    	    Button closeButton = new Button("Close");
    	    closeButton.setOnAction(e -> contributionsStage.close());
    	    layout.getChildren().add(closeButton);

    	    Scene scene = new Scene(new ScrollPane(layout), 500, 600);
    	    contributionsStage.setScene(scene);
    	    contributionsStage.show();
    	}
    	
    	/**
         * Displays a simple alert dialog with a title and message.
         * 
         * @param title the title of the alert
         * @param message the content message
         */
        private void showAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        }
    	
}