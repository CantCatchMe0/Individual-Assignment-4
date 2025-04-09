package application;
import java.util.ArrayList;

/**
 * The AnswerList class represents a list of answers in the system.
 * It contains an ArrayList of answer text as well as the
 * answers' IDs in the SQL database (If they are not consecutive).
 */
public class AnswerList {
	private ArrayList<String> answers;
	private ArrayList<Integer> db_ids;
	
    // AnswerList constructors
    public AnswerList(ArrayList<String> answers) {
    	this.answers = answers;
    	this.db_ids = new ArrayList<>();
    }
    public AnswerList(ArrayList<String> answers, ArrayList<Integer> db_ids) {
    	this.answers = answers;
    	this.db_ids = (db_ids != null) ? db_ids : new ArrayList<>();
    }
    
    // AnswerList print function and getters
    public void print() {
    	for (int i = 0; i < answers.size(); i++) {
    		System.out.print(answers.get(i));
    	}
    }
    public ArrayList<String> getAnswers() { return answers; }
    public ArrayList<Integer> getIDs() { return db_ids; }
}