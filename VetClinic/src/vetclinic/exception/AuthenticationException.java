package vetclinic.exception;

/**
 * AuthenticationException - thrown when the username or password is wrong.
 * Owner: Gab
 *
 * OOP concepts: INHERITANCE (extends Exception).
 */
public class AuthenticationException extends Exception {

    public AuthenticationException(String message) {
        super(message);
    }
}
