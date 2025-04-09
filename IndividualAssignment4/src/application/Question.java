package application;

/**
 * The Question class represents a question entity in the system.
 * It contains the question's attributes such as its
 * title, body text, and whether or not it has been resolved.
 */
public class Question {
	private String title;
    private String text;
    private String postedBy;
    private boolean resolved;
    
    // Question constructor
    public Question(String title, String text, String postedBy) {
        this.title = title;
        this.text = text;
        this.postedBy = postedBy;
        this.resolved = false;
    }
    
    // Question edit actions
    public void setTitle(String title) {
    	this.title = title;
    }
    public void setText(String text) {
    	this.text = text;
    }
    public void resolve() {
    	this.resolved=true;
    }
    /* The CRUD operations on questions used by the console application
    are implemented in DatabaseHelper.java
    */
    
    // Question getters
    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getPostedBy() { return postedBy; }
    public boolean getResolved() { return resolved; }
}