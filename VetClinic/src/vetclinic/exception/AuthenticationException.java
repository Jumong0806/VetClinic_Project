package vetclinic.exception;

/**
 * AuthenticationException - thrown when the username or password is wrong.
 * Owner: Gab
 *
 * OOP concepts: INHERITANCE (extends Exception).
 */
public class AuthenticationException extends Exception {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
