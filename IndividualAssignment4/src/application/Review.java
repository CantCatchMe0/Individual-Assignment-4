package application;

/**
 * The Review class represents a review left by a user (reviewer)
 * on either a question or an answer in the Q&A system.
 */
public class Review {
    private int id;
    private String text;
    private String reviewer;
    private Integer questionID;
    private Integer answerID;
    
    /**
     * Constructs a Review without an ID (typically used before storing in the database).
     *
     * @param text       The review content.
     * @param reviewer   The username of the reviewer.
     * @param questionID The ID of the reviewed question (nullable).
     * @param answerID   The ID of the reviewed answer (nullable).
     */
    public Review(String text, String reviewer, Integer questionID, Integer answerID){
        this.text = text;
        this.reviewer = reviewer;
        this.questionID = questionID;
        this.answerID = answerID;
    }
    
    /**
     * Constructs a Review with an ID (typically retrieved from the database).
     *
     * @param id         The ID of the review.
     * @param text       The review content.
     * @param reviewer   The username of the reviewer.
     * @param questionID The ID of the reviewed question (nullable).
     * @param answerID   The ID of the reviewed answer (nullable).
     */
    public Review(int id, String text, String reviewer, Integer questionID, Integer answerID){
    	this.id = id;
        this.text = text;
        this.reviewer = reviewer;
        this.questionID = questionID;
        this.answerID = answerID;
    }
    
    /**
     * Returns the ID of the review.
     * @return The review ID.
     */
    public int getId(){
        return id;
    }
    
    /**
     * Sets the ID of the review.
     * @param id The review ID.
     */
    public void setId(int id){ 
    	this.id = id; 
    }

    /**
     * Returns the text content of the review.
     * @return The review text.
     */
    public String getText(){
        return text;
    }
    
    /**
     * Sets the text content of the review.
     * @param text The review text.
     */
    public void setText(String text){
        this.text = text;
    }
    
    /**
     * Returns the username of the reviewer.
     * @return The reviewer username.
     */
    public String getReviewer(){
        return reviewer;
    }

    /**
     * Sets the reviewer username.
     * @param reviewer The username of the reviewer.
     */
    public void setReviewer(String reviewer){
        this.reviewer = reviewer;
    }

    /**
     * Returns the ID of the associated question.
     * @return The question ID, or null if not applicable.
     */
    public Integer getQuestionID(){
        return questionID;
    }

    /**
     * Sets the ID of the associated question.
     * @param questionID The question ID.
     */
    public void setQuestionID(Integer questionID){
        this.questionID = questionID;
    }

    /**
     * Returns the ID of the associated answer.
     * @return The answer ID, or null if not applicable.
     */
    public Integer getAnswerID(){
        return answerID;
    }

    /**
     * Sets the ID of the associated answer.
     * @param answerID The answer ID.
     */
    public void setAnswerID(Integer answerID){
        this.answerID = answerID;
    }
}
