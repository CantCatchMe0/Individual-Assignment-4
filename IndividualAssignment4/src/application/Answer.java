package application;

/**
 * The Answer class represents an answer entity in the system.
 * It contains the answer's attributes such as whether
 * it is marked as solution, and which question it is posted under.
 */
public class Answer {
    private boolean isSolution;
    private String text;
    private String postedBy;
    private int underQuestion;

    // Answer constructor
    public Answer(String text, String postedBy, int underQuestion) {
        this.isSolution = false;
        this.text = text;
        this.postedBy = postedBy;
        this.underQuestion = underQuestion;
    }
    
    // Answer edit actions
    public void setAnswerText(String text) {
    	this.text = text;
    }
    public void markSolution() {
    	this.isSolution=true;
    }
    /* The CRUD operations on answers used by the console application
       are implemented in DatabaseHelper.java
    */
    
    // Answer getters
    public boolean getIsSolution() { return isSolution; }
    public String getText() { return text; }
    public String getPostedBy() { return postedBy; }
    public int getUnderQuestion() { return underQuestion; }
}