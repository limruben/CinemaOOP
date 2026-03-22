package cinema.clerk;

/**
 * Custom exception for business-rule violations and data errors in the
 * cinema booking system.
 *
 * <p>This is an unchecked exception so that calling code (including GUI event
 * handlers) can catch it without being forced to declare checked exceptions,
 * while still providing a clear, user-readable error message.</p>
 */
public class BookingException extends RuntimeException {

    /**
     * Constructs a BookingException with a descriptive message.
     *
     * @param message human-readable description of the error
     */
    public BookingException(String message) {
        super(message);
    }

    /**
     * Constructs a BookingException wrapping a lower-level cause.
     *
     * @param message human-readable description of the error
     * @param cause   the underlying exception (e.g., {@link java.io.IOException})
     */
    public BookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
