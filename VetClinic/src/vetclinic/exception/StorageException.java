package vetclinic.exception;

/**
 * StorageException - our own error type for problems while saving or loading data.
 * Owner: Unassigned
 *
 * OOP concepts: INHERITANCE (extends Exception). It is a CHECKED exception, so the compiler
 * forces the caller to handle it (the GUI shows it in a JOptionPane).
 */
public class StorageException extends Exception {

    private static final long serialVersionUID = 1L;

    public StorageException(String message, Throwable cause) {
        super(message, cause);   // keeps the message and the original error
    }
}
