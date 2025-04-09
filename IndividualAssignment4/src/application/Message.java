package application;

import java.time.LocalDateTime;

/**
 * The Message class represents a private message sent between users.
 * Each message contains information about the sender, recipient, content, timestamp, and a unique ID.
 */
public class Message {
    private int id;
    private String sender;
    private String recipient;
    private String content;
    private LocalDateTime timestamp;

    /**
     * Constructs a new {@code Message} with the specified attributes.
     *
     * @param id the unique identifier for the message
     * @param sender the username of the sender
     * @param recipient the username of the recipient
     * @param content the text content of the message
     * @param timestamp the timestamp when the message was sent
     */
    public Message(int id, String sender, String recipient, String content, LocalDateTime timestamp) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    /**
     * Gets the sender's username.
     *
     * @return the sender's username
     */
    public String getSender() { return sender; }
    /**
     * Gets the recipient's username.
     *
     * @return the recipient's username
     */
    public String getRecipient() { return recipient; }
    /**
     * Gets the message content.
     *
     * @return the content of the message
     */
    public String getContent() { return content; }
    /**
     * Gets the timestamp of the message.
     *
     * @return the timestamp indicating when the message was sent
     */
    public LocalDateTime getTimestamp() { return timestamp; }
}
