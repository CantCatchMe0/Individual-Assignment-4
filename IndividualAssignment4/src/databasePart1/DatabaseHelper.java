package databasePart1;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import application.Answer;
import application.AnswerList;
import application.Message;
import application.Question;
import application.QuestionList;
import application.User;
import application.Review;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			
			statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
			createQuestionTables();
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "firstName VARCHAR(255), "
				+ "lastName VARCHAR(255), "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "email VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "role VARCHAR(20), "
				+ "oneTimePassword VARCHAR(255))";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);
	    
	    String reviewsTable = "CREATE TABLE IF NOT EXISTS reviews ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "text VARCHAR(1000), "
	            + "reviewer VARCHAR(255), "
	            + "questionID INT, "
	            + "answerID INT)";
	    statement.execute(reviewsTable);
	    
	    String messagesTable = "CREATE TABLE IF NOT EXISTS messages ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "sender VARCHAR(255), "
	            + "recipient VARCHAR(255), "
	            + "content VARCHAR(1000), "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
	        statement.execute(messagesTable);

	    String trustedTable = "CREATE TABLE IF NOT EXISTS trustedReviewers ("
                + "student VARCHAR(255), "
                + "reviewer VARCHAR(255), "
                + "PRIMARY KEY (student, reviewer))";
            statement.execute(trustedTable);
            
        String reviewerWeights = "CREATE TABLE IF NOT EXISTS reviewerWeights ("
        		+ "student VARCHAR(255), "
                + "reviewer VARCHAR(255), "
        		+ "weight INT DEFAULT 0, "
        		+ "PRIMARY KEY (student, reviewer))";
        	statement.execute(reviewerWeights);
        	
        String reviewerRequests = "CREATE TABLE IF NOT EXISTS reviewerRequests ("
            	+ "studentUsername VARCHAR(255) PRIMARY KEY, "
                + "reviewer VARCHAR(255))";
            statement.execute(reviewerRequests);
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (firstName, lastName, userName, email, password, role) VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getFirstName());
			pstmt.setString(2, user.getLastName());
			pstmt.setString(3, user.getUserName());
			pstmt.setString(4, user.getEmail());
			pstmt.setString(5, user.getPassword());
			pstmt.setString(6, user.getRole());
			pstmt.executeUpdate();
		}
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	public String generateOneTimePassword(String userName) {
	    String oneTimePassword = UUID.randomUUID().toString().substring(0, 8); // Generate an 8 character password
	    String query = "UPDATE cse360users SET oneTimePassword = ? WHERE userName = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, oneTimePassword);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return oneTimePassword;
	}
	
	public String getOneTimePassword(String userName) {
	    String query = "SELECT oneTimePassword FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("oneTimePassword");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public void clearOneTimePassword(String userName) {
	    String query = "UPDATE cse360users SET oneTimePassword = NULL WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void updatePassword(String userName, String newPassword) {
	    String query = "UPDATE cse360users SET password = ?, oneTimePassword = NULL WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newPassword);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code) VALUES (?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}
	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	private void createQuestionTables() throws SQLException {
	    // Create questions table
		String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "title VARCHAR(255), "
				+ "text VARCHAR(500), "
				+ "postedBy VARCHAR(255), "
				+ "resolved INT DEFAULT 0,"
				+ "flagged BOOLEAN DEFAULT FALSE)";
		statement.execute(questionsTable);
		
	    // Create answers table
		String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "text VARCHAR(500), "
				+ "postedBy VARCHAR(255), "
				+ "underQuestion INT, "
				+ "isSolution INT DEFAULT 0, "
				+ "likes INT DEFAULT 0, "
				+ "dislikes INT DEFAULT 0,"
				+ "flagged BOOLEAN DEFAULT FALSE)";
				
		statement.execute(answersTable);
		
		String repliesTable = "CREATE TABLE IF NOT EXISTS replies ("
		        + "id INT AUTO_INCREMENT PRIMARY KEY, "
		        + "text VARCHAR(500), "
		        + "postedBy VARCHAR(255), "
		        + "underAnswer INT, "
		        + "FOREIGN KEY (underAnswer) REFERENCES answers(id) ON DELETE CASCADE)";
		statement.execute(repliesTable);
	}
	
	public void postReply(int answerID, String postedBy, String text) throws SQLException {
	    String query = "INSERT INTO replies (text, postedBy, underAnswer) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, text);
	        pstmt.setString(2, postedBy);
	        pstmt.setInt(3, answerID);
	        pstmt.executeUpdate();
	    }
	}

	// Create a new question
	public void postQuestion(Question question) throws SQLException {
		String insertQuestion = "INSERT INTO questions (title, text, postedBy, resolved) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertQuestion)) {
			pstmt.setString(1, question.getTitle());
			pstmt.setString(2, question.getText());
			pstmt.setString(3, question.getPostedBy());
			pstmt.setInt(4, question.getResolved() ? 1 : 0);
			pstmt.executeUpdate();
		}
	}
	
	// Create a new answer
	public boolean postAnswer(Answer answer) throws SQLException {
		if(answer.getText().isEmpty()) {
			return false;
		}
		String insertQuestion = "INSERT INTO answers (text, postedBy, underQuestion, isSolution) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertQuestion)) {
			pstmt.setString(1, answer.getText());
			pstmt.setString(2, answer.getPostedBy());
			pstmt.setInt(3, answer.getUnderQuestion());
			pstmt.setInt(4, answer.getIsSolution() ? 1 : 0);
			pstmt.executeUpdate();
		}
		return true;
	}
	// flag question 
	public void flagQuestion(int questionID) throws SQLException {
	    String query = "UPDATE questions SET flagged = TRUE WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionID);
	        pstmt.executeUpdate();
	    }
	}
	// flag answer 
	public void flagAnswer(int answerID) throws SQLException {
	    String query = "UPDATE answers SET flagged = TRUE WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, answerID);
	        pstmt.executeUpdate();
	    }
	}
	// check if the question is flagged
	public boolean isQuestionFlagged(int questionID) {
	    String query = "SELECT flagged FROM questions WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionID);
	        ResultSet rs = pstmt.executeQuery();
	        return rs.next() && rs.getBoolean("flagged");
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	// check if the answer is flagged
	public boolean isAnswerFlagged(int answerID) {
	    String query = "SELECT flagged FROM answers WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, answerID);
	        ResultSet rs = pstmt.executeQuery();
	        return rs.next() && rs.getBoolean("flagged");
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public List<Question> getFlaggedQuestions() {
	    List<Question> flagged = new ArrayList<>();
	    String query = "SELECT * FROM questions WHERE flagged = TRUE";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            flagged.add(new Question(
	                rs.getString("title"),
	                rs.getString("text"),
	                rs.getString("postedBy")
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return flagged;
	}

	public List<Answer> getFlaggedAnswers() {
	    List<Answer> flagged = new ArrayList<>();
	    String query = "SELECT * FROM answers WHERE flagged = TRUE";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            flagged.add(new Answer(
	                rs.getString("text"),
	                rs.getString("postedBy"),
	                rs.getInt("underQuestion")
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return flagged;
	}

	
	public void incrementLike(int answerID) {
	    String query = "UPDATE answers SET likes = likes + 1 WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, answerID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void decrementLike(int answerID) {
	    String query = "UPDATE answers SET likes = likes - 1 WHERE id = ? AND likes > 0";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, answerID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public void incrementDislike(int answerID) {
	    String query = "UPDATE answers SET dislikes = dislikes + 1 WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, answerID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void decrementDislike(int answerID) {
	    String query = "UPDATE answers SET dislikes = dislikes - 1 WHERE id = ? AND dislikes > 0";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, answerID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public int[] getLikesDislikes(int answerID) {
	    String query = "SELECT likes, dislikes FROM answers WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, answerID);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return new int[]{rs.getInt("likes"), rs.getInt("dislikes")};
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return new int[]{0, 0};
	}
	
	public String getQuestionPoster(int questionID) {
	    String query = "SELECT postedBy FROM questions WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionID);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("postedBy");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	public String getAnswerPoster(int answerID) {
	    String query = "SELECT postedBy FROM answers WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, answerID);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("postedBy");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	public void updateQuestion(int qid, String newText) {
	    String query = "UPDATE questions SET text = ? WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newText);
	        pstmt.setInt(2, qid);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public String getQuestionTitle(int questionID) {
	    String query = "SELECT title FROM questions WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionID);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("title");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	public String getQuestionText(int questionID) {
	    String query = "SELECT text FROM questions WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionID);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("text");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; 
	}
	
	public void updateQuestionTitle(int qid, String newTitle) {
	    String query = "UPDATE questions SET title = ? WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newTitle);
	        pstmt.setInt(2, qid);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public void updateAnswer(int aid, String newText) {
	    String query = "UPDATE answers SET text = ? WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newText);
	        pstmt.setInt(2, aid);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public QuestionList listAllQuestions() {
	    String query = "SELECT * FROM questions";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        ResultSet rs = pstmt.executeQuery();
	        
	        String data = "";
	        ArrayList<String> questions = new ArrayList<String>();
	        ArrayList<Integer> db_ids = new ArrayList<>();
	        
	        while (rs.next()) {
	        	data += rs.getInt("id") + ". ";
	            data += rs.getString("title");
	            if (rs.getInt("resolved") == 1) {
	            	data += " (Resolved)" + "\n";
	            } else {
	            	data += " (Unresolved)" + "\n";
	            }
	            data += "Posted by: " + rs.getString("postedBy") + "\n";
	            data += rs.getString("text");
	            data += "\n------------------------------------------\n";
	            questions.add(data);
	            db_ids.add(rs.getInt("id"));
	            data = "";
	        }
	        if (questions.isEmpty()) {
	            return new QuestionList(new ArrayList<>(), new ArrayList<>()); 
	        }
	        return new QuestionList(questions, db_ids);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return new QuestionList(new ArrayList<>(), new ArrayList<>()); // If no user exists or an error occurs
	}
	
	// View the answers to a question
	public AnswerList viewAnswersToQuestion(int qid) {
		String query = "SELECT * FROM answers WHERE underQuestion = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	    	pstmt.setInt(1, qid);
	    	
	        ResultSet rs = pstmt.executeQuery();
	        
	        String data = "";
	        ArrayList<String> answers = new ArrayList<String>();
	        ArrayList<Integer> db_ids = new ArrayList<>();
	        
	        while (rs.next()) {
	        	if (rs.getInt("isSolution") == 1) {
	        		data += "(Solution) ";
	        	}
	            data += rs.getString("text") + "\n";
	            data += "Posted by: " + rs.getString("postedBy");
	            data += "\n------------------------------------------\n";
	            answers.add(data);
	            db_ids.add(rs.getInt("id"));
	            data = "";
	        }
	        
	        if (answers.isEmpty()) {
	            return new AnswerList(new ArrayList<>(), new ArrayList<>());
	        }
	        
	        return new AnswerList(answers, db_ids);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return new AnswerList(new ArrayList<>(), new ArrayList<>()); // If no user exists or an error occurs
	}
	
	// Show the user's answers, which they are permitted to manage
	public AnswerList showAnswersToManage(String currentUser) {
		String query = "SELECT * FROM answers WHERE postedBy = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	    	pstmt.setString(1, currentUser);
	    	
	        ResultSet rs = pstmt.executeQuery();
	        
	        String data = "";
	        int index = 1;
	        
	        ArrayList<String> answers = new ArrayList<String>();
	        ArrayList<Integer> db_ids = new ArrayList<Integer>();
	        db_ids.add(null); // Fill index 0 of ArrayList with null
	        
	        while (rs.next()) {
	        	data += index + ". ";
	        	if (rs.getInt("isSolution") == 1) {
	        		data += "(Solution) ";
	        	}
	            data += rs.getString("text") + "\n";
	            data += "Posted by: " + rs.getString("postedBy");
	            data += "\n------------------------------------------\n";
	            answers.add(data);
	            db_ids.add(rs.getInt("id"));
	            index++;
	            data = "";
	        }
	        
	        AnswerList al = new AnswerList(answers, db_ids);
	        return al;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Show the answers under a user's question, which they are permitted to mark as solution
	public AnswerList showAnswersToManage(int qid) {
		String query = "SELECT * FROM answers WHERE underQuestion = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	    	pstmt.setInt(1, qid);
	    	
	        ResultSet rs = pstmt.executeQuery();
	        
	        String data = "";
	        int index = 1;
	        
	        ArrayList<String> answers = new ArrayList<String>();
	        ArrayList<Integer> db_ids = new ArrayList<Integer>();
	        db_ids.add(null); // Fill index 0 of ArrayList with null
	        
	        while (rs.next()) {
	        	data += index + ". ";
	        	if (rs.getInt("isSolution") == 1) {
	        		data += "(Solution) ";
	        	}
	            data += rs.getString("text") + "\n";
	            data += "Posted by: " + rs.getString("postedBy");
	            data += "\n------------------------------------------\n";
	            answers.add(data);
	            db_ids.add(rs.getInt("id"));
	            index++;
	            data = "";
	        }
	        
	        AnswerList al = new AnswerList(answers, db_ids);
	        return al;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Show the user's questions, which they are permitted to manage
	public QuestionList showQuestionsToManage(String currentUser) {
		String query = "SELECT * FROM questions WHERE postedBy = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	    	pstmt.setString(1, currentUser);
	    	
	        ResultSet rs = pstmt.executeQuery();
	        
	        String data = "";
	        int index = 1;
	        
	        ArrayList<String> questions = new ArrayList<String>();
	        ArrayList<Integer> db_ids = new ArrayList<Integer>();
	        db_ids.add(null); // Fill index 0 of ArrayList with null
	        
	        while (rs.next()) {
	        	data += index + ". ";
	            data += rs.getString("title");
	            if (rs.getInt("resolved") == 1) {
	            	data += " (Resolved)" + "\n";
	            } else {
	            	data += " (Unresolved)" + "\n";
	            }
	            data += "Posted by: " + rs.getString("postedBy") + "\n";
	            data += rs.getString("text");
	            data += "\n------------------------------------------\n";
	            questions.add(data);
	            db_ids.add(rs.getInt("id"));
	            index++;
	            data = "";
	        }
	        
	        QuestionList ql = new QuestionList(questions, db_ids);
	        return ql;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Delete an answer
	public void deleteAnswer(int aid) {
		String query = "DELETE answers WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, aid);
			pstmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Mark an answer as solution
	public void markAnswerAsSolution(int aid) {
		String query = "UPDATE answers SET isSolution = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, 1);
			pstmt.setInt(2, aid);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Update a question
	public void updateQuestion(int qid, String title, String text) {
		String query = "UPDATE questions SET title = ?, text = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, title);
			pstmt.setString(2, text);
			pstmt.setInt(3, qid);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Mark a question as resolved
	public void markQuestionAsResolved(int qid) {
		String query = "UPDATE questions SET resolved = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, 1);
			pstmt.setInt(2, qid);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Delete a question
	public void deleteQuestion(int qid) {
		String query = "UPDATE questions SET title = ?, text = ?, postedBy = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, "This question has been deleted.");
			pstmt.setString(2, "--");
			pstmt.setString(3, "--");
			pstmt.setInt(4, qid);
			pstmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	
	public void addReview(Review review) throws SQLException {
	    String query = "INSERT INTO reviews (text, reviewer, questionID, answerID) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, review.getText());
	        pstmt.setString(2, review.getReviewer());
	        pstmt.setObject(3, review.getQuestionID());
	        pstmt.setObject(4, review.getAnswerID());
	        pstmt.executeUpdate();
	    }
	}
	
	public List<Review> getReviews(Integer questionID, Integer answerID) {
	    List<Review> reviews = new ArrayList<>();
	    StringBuilder query = new StringBuilder("SELECT * FROM reviews WHERE ");

	    if (questionID != null) {
	        query.append("questionID = ? AND answerID IS NULL");
	    } else if (answerID != null) {
	        query.append("answerID = ? AND questionID IS NULL");
	    } else {
	        return reviews;
	    }

	    try (PreparedStatement pstmt = connection.prepareStatement(query.toString())) {
	        if (questionID != null) {
	            pstmt.setInt(1, questionID);
	        } else {
	            pstmt.setInt(1, answerID);
	        }

	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            reviews.add(new Review(
	                rs.getInt("id"),
	                rs.getString("text"),
	                rs.getString("reviewer"),
	                rs.getObject("questionID") != null ? rs.getInt("questionID") : null,
	                rs.getObject("answerID") != null ? rs.getInt("answerID") : null
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return reviews;
	}
	
	public void updateReview(Review review) throws SQLException {
	    String query = "UPDATE reviews SET text = ? WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, review.getText());
	        pstmt.setInt(2, review.getId());
	        pstmt.executeUpdate();
	    }
	}
	
	public void deleteReview(int reviewID) throws SQLException {
	    String query = "DELETE FROM reviews WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, reviewID);
	        pstmt.executeUpdate();
	    }
	}
	
	public void sendMessage(String sender, String recipient, String content) {
	    String query = "INSERT INTO messages (sender, recipient, content) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, sender);
	        pstmt.setString(2, recipient);
	        pstmt.setString(3, content);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public List<Message> getMessages(String user1, String user2) {
	    List<Message> messages = new ArrayList<>();
	    String query = "SELECT * FROM messages WHERE " +
	                   "(sender = ? AND recipient = ?) OR (sender = ? AND recipient = ?) " +
	                   "ORDER BY timestamp ASC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, user1);
	        pstmt.setString(2, user2);
	        pstmt.setString(3, user2);
	        pstmt.setString(4, user1);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            messages.add(new Message(
	                rs.getInt("id"),
	                rs.getString("sender"),
	                rs.getString("recipient"),
	                rs.getString("content"),
	                rs.getTimestamp("timestamp").toLocalDateTime()
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return messages;
	}
	
	public List<String> getChatPartners(String currentUser) {
	    List<String> users = new ArrayList<>();
	    String query = "SELECT DISTINCT CASE WHEN sender = ? THEN recipient ELSE sender END AS chatPartner " +
	                   "FROM messages WHERE sender = ? OR recipient = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, currentUser);
	        pstmt.setString(2, currentUser);
	        pstmt.setString(3, currentUser);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            users.add(rs.getString("chatPartner"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return users;
	}
	
	public User getUserByUserName(String userName) {
	    String query = "SELECT * FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return new User(
	                rs.getString("firstName"),
	                rs.getString("lastName"),
	                rs.getString("userName"),
	                rs.getString("email"),
	                rs.getString("role")
	            );
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public List<Review> getReviewsByReviewer(String reviewer) {
	    List<Review> reviews = new ArrayList<>();
	    String query = "SELECT * FROM reviews WHERE reviewer = ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, reviewer);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Review r = new Review(
	                rs.getInt("id"),
	                rs.getString("text"),
	                rs.getString("reviewer"),
	                (rs.getObject("questionID") != null) ? rs.getInt("questionID") : null,
	                (rs.getObject("answerID") != null) ? rs.getInt("answerID") : null
	            );
	            reviews.add(r);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return reviews;
	}
	
	public void addTrustedReviewer(String student, String reviewer) throws SQLException {
	    if (isTrustedReviewer(student, reviewer)) {
	        return;
	    }
	    String query = "INSERT INTO TrustedReviewers (student, reviewer) VALUES (?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, student);
	        pstmt.setString(2, reviewer);
	        pstmt.executeUpdate();
	    }
	}
	
	public void removeTrustedReviewer(String student, String reviewer) throws SQLException {
	    String query = "DELETE FROM TrustedReviewers WHERE student = ? AND reviewer = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, student);
	        pstmt.setString(2, reviewer);
	        pstmt.executeUpdate();
	    }
	}

	public boolean isTrustedReviewer(String student, String reviewer) {
	    String query = "SELECT 1 FROM TrustedReviewers WHERE student = ? AND reviewer = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, student);
	        pstmt.setString(2, reviewer);
	        ResultSet rs = pstmt.executeQuery();
	        return rs.next();
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public List<Review> getTrustedReviews(String student, Integer questionID, Integer answerID) {
	    List<Review> reviews = new ArrayList<>();
	    String query = "SELECT r.* FROM reviews r " +
	                   "JOIN TrustedReviewers t ON r.reviewer = t.reviewer " +
	                   "WHERE t.student = ?";
	    if (questionID != null) query += " AND r.questionID = " + questionID;
	    if (answerID != null) query += " AND r.answerID = " + answerID;

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, student);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            reviews.add(new Review(
	                rs.getInt("id"),
	                rs.getString("text"),
	                rs.getString("reviewer"),
	                (rs.getObject("questionID") != null) ? rs.getInt("questionID") : null,
	                (rs.getObject("answerID") != null) ? rs.getInt("answerID") : null
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return reviews;
	}
	
	public void setReviewerWeight(String student, String reviewer, int weight) throws SQLException {
	    String query = "MERGE INTO reviewerWeights (student, reviewer, weight) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, student);
	        pstmt.setString(2, reviewer);
	        pstmt.setInt(3, weight);
	        pstmt.executeUpdate();
	    }
	}
	
	public int getReviewerWeight(String student, String reviewer) {
	    String query = "SELECT weight FROM reviewerWeights WHERE student = ? AND reviewer = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, student);
	        pstmt.setString(2, reviewer);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("weight");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return 0; 
	}
	
	public List<Review> getSortedReviews(String student, Integer questionID, Integer answerID) {
	    List<Review> reviews = getReviews(questionID, answerID);
	    reviews.sort((r1, r2) -> {
	        int w1 = getReviewerWeight(student, r1.getReviewer());
	        int w2 = getReviewerWeight(student, r2.getReviewer());
	        return Integer.compare(w2, w1); 
	    });
	    return reviews;
	}

	public boolean hasPendingRequest(String studentUsername) throws SQLException {
	    String query = "SELECT COUNT(*) FROM reviewerRequests WHERE studentUsername = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, studentUsername);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt(1) > 0;
	        }
	    }
	    return false;
	}

	public boolean isReviewer(String studentUsername) throws SQLException {
	    String query = "SELECT role FROM cse360users WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, studentUsername);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return "reviewer".equals(rs.getString("role"));
	        }
	    }
	    return false;
	}

	public void insertReviewerRequest(String studentUsername) throws SQLException {
	    String query = "INSERT INTO reviewerRequests (studentUsername) VALUES (?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, studentUsername);
	        pstmt.executeUpdate();
	    }
	}
	
	public List<String> getPendingReviewerRequests() throws SQLException {
	    List<String> requests = new ArrayList<>();
	    String query = "SELECT studentUsername FROM reviewerRequests";
	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {
	        while (rs.next()) {
	            requests.add(rs.getString("studentUsername"));
	        }
	    }
	    return requests;
	}
	
	public void deleteReviewerRequest(String studentUsername) throws SQLException {
	    String query = "DELETE FROM reviewerRequests WHERE studentUsername = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, studentUsername);
	        pstmt.executeUpdate();
	    }
	}
	
	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	public List<User> getUsersFromDatabase() {
		List<User> users = new ArrayList<>();
    	String query = "SELECT firstName, lastName, userName, email, role FROM cse360users";
    	
    	try (PreparedStatement pstmt = connection.prepareStatement(query)) {
    		ResultSet rs = pstmt.executeQuery();
	        while(rs.next()) {
	        	String firstName = rs.getString("firstName");
	            String lastName = rs.getString("lastName");
	            String userName = rs.getString("userName");
	            String email = rs.getString("email");
	            String role = rs.getString("role");
	        	users.add(new User(firstName, lastName, userName, email, role));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
    	return users;
	}
	
	public List<String> getUserRoles(String userName) {
	    List<String> roles = new ArrayList<>();
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            String roleString = rs.getString("role");
	            roles = Arrays.asList(roleString.split(", ")); 
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return roles;
	}
	
	public void deleteUser(String userName) {
		String query = "DELETE FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1,  userName);
	        pstmt.executeUpdate();
		} catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void updateUserRole(String userName, String newRoles) {
		String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newRoles);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void addUserRole(String userName, String newRole) throws SQLException {
	    List<String> currentRoles = new ArrayList<>(getUserRoles(userName)); 

	    if (!currentRoles.contains(newRole)) {
	        currentRoles.add(newRole);
	        String updatedRoles = String.join(", ", currentRoles);
	        updateUserRole(userName, updatedRoles);
	    }
	}
	
	public boolean isUserStaff (String userName) {
		String query = "SELEVT role FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role").contains("staff");
	        }
		} catch (SQLException e) {
		    e.printStackTrace();
		}
		return false;
	}
	
	public boolean isUserAdmin(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role").contains("admin");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

}
