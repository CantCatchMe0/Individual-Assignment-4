package application;
import java.util.ArrayList;

/**
 * The QuestionList class represents a list of questions in the system.
 * It contains an ArrayList of question text as well as the
 * questions' IDs in the SQL database (If they are not consecutive).
 */
public class QuestionList {
	private ArrayList<String> questions;
	private ArrayList<Integer> db_ids;
	
    // QuestionList constructors
    public QuestionList(ArrayList<String> questions) {
    	this.questions = questions;
    	this.db_ids = new ArrayList<>();
    }
    public QuestionList(ArrayList<String> questions, ArrayList<Integer> db_ids) {
    	this.questions = questions;
    	this.db_ids = (db_ids != null) ? db_ids : new ArrayList<>();
    }
    
    // QuestionList print function and getters
    public void print() {
    	for (int i = 0; i < questions.size(); i++) {
    		System.out.print(questions.get(i));
    	}
    }
    public ArrayList<String> getQuestions() { return questions; }
    public ArrayList<Integer> getIDs() { return db_ids; }
    
    // QuestionList search function
    public void search(String filter) {
    	ArrayList<String> updated_list = new ArrayList<String>();
    	for (int i = 0; i < questions.size(); i++) {
    		if (questions.get(i).contains(filter)) {
    			updated_list.add(questions.get(i));
    		}
    	}
    	this.questions = updated_list;
    }
}