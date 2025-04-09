package application;

import databasePart1.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
/*
 * This class contains tests for staff to ensure all the database interaction and functionality
 * work as expected, for example like flag question, flag answer, and message to instructor
 *  */
class StaffUnitTest {
	private DatabaseHelper dbHelper;
    private Connection connection;
    
	/*
	 * Build connection between unit test code and database
	 * @throws SQLException if an error appear while connecting to database
	 * */
    @BeforeEach
    
	void setUp() throws SQLException {
		 	dbHelper = new DatabaseHelper();
	        dbHelper.connectToDatabase();
	        connection = DriverManager.getConnection("jdbc:h2:~/HW2Database", "sa", "");
	}

	/*
	 * Terminate connection between unit test code and database
	 * @throws SQLException if an error appear during termination of database connection
	 * */
	 @AfterEach
	 
	void tearDown() throws SQLException {
		dbHelper.closeConnection();
        Statement stmt = connection.createStatement();
        stmt.execute("DROP ALL OBJECTS;");
        stmt.close();
	}
	
	/*
	 * Test to see if the staff can flag the problem correctly
	 * @throws SQLException if an error appear during database interaction
	 *  
	 * */
	@Test
	void flagQuestionTest() throws SQLException{
		//create and post test questions
		Question question = new Question("Test question", "should be flagged", "Test User");
		dbHelper.postQuestion(question);
		//return question table as a list and get question id
		QuestionList questionList = dbHelper.listAllQuestions();
		ArrayList<Integer> questionID = questionList.getIDs();
		// flag the question
		dbHelper.flagQuestion(questionID.get(0));
		// see if the question is flagged
		assertTrue(dbHelper.isQuestionFlagged(questionID.get(0)));
	}
	
	/*
	 * Test to see if the staff can flag answer correctly
	 * @throws SQLException if an error appear during database interaction
	 * */
	@Test
	void flagAnswerTest() throws SQLException{
		//create and post test questions and answers
		Question question = new Question("Test Question", "Should not be flagged", "Test User");
		dbHelper.postQuestion(question);
		// return question in an array list
		QuestionList questionList = dbHelper.listAllQuestions();
		ArrayList<Integer> questionID = questionList.getIDs();
		Answer answer = new Answer("Should be flagged", "Test User", questionID.get(0));
		dbHelper.postAnswer(answer);
		// return answer in an answer list
		AnswerList answerList = dbHelper.viewAnswersToQuestion(questionID.get(0));
		ArrayList<Integer> answerID = answerList.getIDs();
		// flag the answer
		dbHelper.flagAnswer(answerID.get(0));
		// see if the answer is flagged
		assertTrue(dbHelper.isAnswerFlagged(answerID.get(0)));
	}
	
	/*
	 * Test to see if the staff can correctly send message to specific user
	 * @throws SQLException if an error appear during database interaction
	 * */
	@Test
	
	void MessageSystemTest() throws SQLException{
		//create user
		User testUser1 = new User("George", "Jiang", "aabbcc", "aabbcc@asu.edu", "George123456!", "staff");
		User testUser2 = new User("George", "George", "bbccdd", "bbccdd@asu.edu", "George123456!", "instructor");
		dbHelper.register(testUser1);
		dbHelper.register(testUser2);
		// send message between user1 and user2
		dbHelper.sendMessage("testUser1", "testUser2", "From 1 to 2");
		dbHelper.sendMessage("testUser2", "testUser1", "From 2 to 1");
		// get message from database to test if the user are able to receive message
		List<Message> messageList = dbHelper.getMessages("testUser1", "testUser2");
		// see if the message is still in order
		assertTrue(messageList.get(0).getContent().equals("From 1 to 2"));
		assertTrue(messageList.get(1).getContent().equals("From 2 to 1"));
		
	}
	/*
	 * Test to see if all the method in database helper relevant to generate flag report
	 * work as expected
	 * @throws SQLException if an error appear during database interaction
	 * */
	@Test
	
	void flagReportTest() throws SQLException{
		// create questions and answers should be flagged and should not be flagged
		Question question1 = new Question("Test Question1", "Should not be flagged", "Test User");
		Question question2 = new Question("Test Question2", "Should be flagged", "Test User");
		dbHelper.postQuestion(question1);
		dbHelper.postQuestion(question2);
		QuestionList questionList = dbHelper.listAllQuestions();
		ArrayList<Integer> questionID = questionList.getIDs();
		Answer answer1 = new Answer("Should not be flagged", "Test User", questionID.get(0));
		Answer answer2 = new Answer("Should be flagged", "Test User", questionID.get(1));
		dbHelper.postAnswer(answer1);
		dbHelper.postAnswer(answer2);
		AnswerList answerList2 = dbHelper.viewAnswersToQuestion(questionID.get(1));
		ArrayList<Integer> answerID2 = answerList2.getIDs();
		dbHelper.flagQuestion(questionID.get(1));
		dbHelper.flagAnswer(answerID2.get(0));
		List<Answer> flaggedAnswer = dbHelper.getFlaggedAnswers();
		List<Question> flaggedQuestion = dbHelper.getFlaggedQuestions();
		// see if the flagged question and answer is the correct target
		assertTrue(flaggedAnswer.get(0).getText().equals("Should be flagged"));
		assertTrue(flaggedQuestion.get(0).getText().equals("Should be flagged"));
	}
}
