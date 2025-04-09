package application;

import databasePart1.DatabaseHelper;

/**
 * <p>The QnATestAutomation class is responsible for running a series of automated test cases
 * on a Q&A system using a database backend. The tests validate core CRUD functionality
 * for questions and answers, including input validation logic.</p>
 * 
 * <p>This class provides a main method that runs multiple test cases using a simple
 * automation framework. It reports test results to the console and tracks the number
 * of passed and failed tests.</p>
 * 
 * @author Adil Pekel
 * @version 1.0		2025-03-26
 */

public class QnATestAutomation {
	//Counters to keep track of the passed and failed tests
	static int numPassed = 0;
    static int numFailed = 0;
    
    /**
     * Launches the test automation suite, executes test cases,
     * and reports results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
    	System.out.println("______________________________________");
		System.out.println("\nQ&A Testing Automation");

        DatabaseHelper db = new DatabaseHelper();

        try {
            db.connectToDatabase();
            
            //Test 1: Post a question and verify it appears in the database
            performTestCase(1, "Post a question", true, () -> {
            	//Create the new question through the Question class
                Question q = new Question("What is a baby?", "Explain it in OOP terms.", "tester");
                //Add the question to the database
                db.postQuestion(q);
                //Retrieve the list of questions from the database
                QuestionList all = db.listAllQuestions();
                //Check the retrieved list to confirm the newly posted question exists
                return all.getQuestions().stream().anyMatch(qs -> qs.contains("baby"));
            });
            
            //Test 2: Post an answer to the most recent question and verify it's saved
            performTestCase(2, "Post an answer to latest question", true, () -> {
            	//Retrieve the list of questions from the database
                QuestionList all = db.listAllQuestions();
                //Retrieve the ID of the most recent question
                int questionID = all.getIDs().get(all.getIDs().size() - 1);
                //Create the new answer object to be posted
                Answer a = new Answer("A baby is an object of a class called Parent.", "tester", questionID);
                //Add the new answer to the database
                db.postAnswer(a);
                //Retrieve the list of answers to the question
                AnswerList answers = db.viewAnswersToQuestion(questionID);
                //Verify that the answer appears in the database under the corresponding question
                return answers.getAnswers().stream().anyMatch(ans -> ans.contains("Parent"));
            });

            //Test 3: Edit the text of the most recent question and verify it was updated
            performTestCase(3, "Edit question's text", true, () -> {
            	//Retrieve the list of all questions
                QuestionList all = db.listAllQuestions();
                //Retrieve the ID of the most recent question
                int questionID = all.getIDs().get(all.getIDs().size() - 1);
                //Update the content of the question with the new text
                db.updateQuestion(questionID, "What is a baby?", "Updated explanation of a baby in OOP terms.");
                //Confirm the update was saved in the database
                return db.getQuestionText(questionID).contains("Updated explanation");
            });

            //Test 4: Increment the like count of an answer and verify the result
            performTestCase(4, "Like an answer", true, () -> {
            	//Retrieve the list of all questions
                QuestionList all = db.listAllQuestions();
                //Retrieve the ID of the most recent question
                int questionID = all.getIDs().get(all.getIDs().size() - 1);
                //Retrieve the list of all answers
                AnswerList answers = db.viewAnswersToQuestion(questionID);
                //Retrieve the ID of the most recent answer
                int answerID = answers.getIDs().get(0);
                //Store the current number of likes
                int before = db.getLikesDislikes(answerID)[0];
                //Increment the most recent answer's likes
                db.incrementLike(answerID);
                //Retrieve the new number of likes
                int after = db.getLikesDislikes(answerID)[0];
                //Verify the likes were incremented by comparing the stored like count to the new like count
                return after == before + 1;
            });
            
            //Test 5: Prevent posting an empty answer
            performTestCase(5, "Prevent posting empty answer", false, () -> {
            	//Create an answer object with a fake answerID
                Answer a = new Answer("", "tester", 9999);
                //Attempt to post the answer with the empty text
                //Return true if the answer was posted and false otherwise
                if(db.postAnswer(a)) {
                	return true;
                }
                return false;
            });
            
        //Catch unexpected errors
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeConnection();
        }

        System.out.println("____________________________________________________________________________");
		System.out.println();
		System.out.println("Number of tests passed: "+ numPassed);
		System.out.println("Number of tests failed: "+ numFailed);
    }

    /**
     * Executes a single test case and evaluates whether the outcome
     * matches the expected result.
     *
     * @param testCase    the test case number
     * @param description a short description of the test's purpose
     * @param expected    the expected result
     * @param logic       a functional interface defining the test logic
     */
    private static void performTestCase(int testCase, String description, boolean expected, TestLogic logic) {
        System.out.println("Test " + testCase + ": " + description);
        try {
            boolean result = logic.run();
            if (result == expected) {
                System.out.println("***PASS***\n");
                numPassed++;
            } else {
                System.out.println("***FAIL*** (Unexpected result)\n");
                numFailed++;
            }
        } catch (Exception e) {
            if (!expected) {
                System.out.println("***PASS*** (Caught expected error)\n");
                numPassed++;
            } else {
                System.out.println("***FAIL*** (Exception thrown)\n");
                e.printStackTrace();
                numFailed++;
            }
        }
    }

    /**
     * A functional interface representing the logic of a test case.
     */
    interface TestLogic {
        /**
         * Runs the test case logic.
         *
         * @return {@code true} if the test passed and {@code false} otherwise
         * @throws Exception if an error occurs during execution
         */
        boolean run() throws Exception;
    }
}