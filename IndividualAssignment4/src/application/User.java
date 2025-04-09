package application;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 */
public class User {
	private String firstName;
	private String lastName;
    private String userName;
    private String email;
    private String password;
    private String role;

    /**
     * Constructs a new User with all fields including password.
     *
     * @param firstName The user's first name.
     * @param lastName  The user's last name.
     * @param userName  The user's unique username.
     * @param email     The user's email address.
     * @param password  The user's password.
     * @param role      The role assigned to the user (e.g., student, admin).
     */
    public User( String firstName, String lastName, String userName, String email, String password, String role) {
    	this.firstName = firstName;
    	this.lastName = lastName;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.role = role;
    }
    
    /**
     * Constructs a new User without a password, typically used for display or non-authentication purposes.
     *
     * @param firstName The user's first name.
     * @param lastName  The user's last name.
     * @param userName  The user's unique username.
     * @param email     The user's email address.
     * @param role      The role assigned to the user.
     */
    public User( String firstName, String lastName, String userName, String email, String role) {
    	this.firstName = firstName;
    	this.lastName = lastName;
        this.userName = userName;
        this.email = email;
        this.role = role;
    }
    
    /**
     * Sets the role for the user.
     *
     * @param role The new role to assign to the user.
     */
    public void setRole(String role) {
    	this.role=role;
    }
    /**
     * Returns the user's first name.
     *
     * @return The user's first name.
     */
    public String getFirstName() { return firstName; }
    /**
     * Returns the user's last name.
     *
     * @return The user's last name.
     */
    public String getLastName() { return lastName; }
    /**
     * Returns the user's username.
     *
     * @return The user's username.
     */
    public String getUserName() { return userName; }
    /**
     * Returns the user's email.
     *
     * @return The user's email address.
     */
    public String getEmail() { return email; }
    /**
     * Returns the user's password.
     *
     * @return The user's password.
     */
    public String getPassword() { return password; }
    /**
     * Returns the user's current role.
     *
     * @return The user's role.
     */
    public String getRole() { return role; }
}
