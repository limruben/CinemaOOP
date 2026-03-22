package cinema.clerk;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Main service class for the Ticketing Clerk module.
 *
 * <p>Provides all operations that a cinema ticketing clerk can perform:
 * viewing showtimes, checking seat availability, booking, modifying, and
 * cancelling tickets, processing payments, and generating receipts.</p>
 *
 * <h3>Pricing</h3>
 * <ul>
 *   <li>Standard seat: $10.00</li>
 *   <li>VIP seat: $20.00</li>
 * </ul>
 *
 * <h3>GUI integration guide</h3>
 * <ol>
 *   <li>Create a single {@code ClerkService} instance when your JFrame initialises.</li>
 *   <li>Populate combo-boxes / tables by calling {@link #getAvailableShowtimes()}
 *       and {@link #getSeatAvailability(int)}.</li>
 *   <li>When the clerk clicks "Book", call
 *       {@link #bookTickets(String, int, List)} with values read from the UI
 *       controls.</li>
 *   <li>Pass the returned {@link Booking} to {@link #processPayment(Booking, double)}
 *       to get a receipt string, then display it in a dialog or text pane.</li>
 *   <li>For cancel / modify operations, collect the booking ID from the UI and
 *       call {@link #cancelBooking(String)} or
 *       {@link #modifyBooking(String, List)} accordingly.</li>
 *   <li>All methods throw {@link BookingException} (unchecked) on validation
 *       or I/O errors; catch it in the GUI layer and show an error dialog.</li>
 * </ol>
 */
public class ClerkService {

    /** Price per standard seat in dollars. */
    public static final double STANDARD_PRICE = 10.00;

    /** Price per VIP seat in dollars. */
    public static final double VIP_PRICE = 20.00;

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private List<Movie>    movies;
    private List<Showtime> showtimes;
    private List<Seat>     seats;
    private List<Booking>  bookings;

    // =====================================================================
    // Constructor
    // =====================================================================

    /**
     * Constructs a {@code ClerkService} and loads all data from the text files
     * in the configured data directory.
     *
     * <p>Call {@link FileManager#setDataDirectory(String)} <em>before</em>
     * constructing this object if you need a non-default data path.</p>
     */
    public ClerkService() {
        FileManager.initialize();
        reloadData();
    }

    // =====================================================================
    // Data management
    // =====================================================================

    /**
     * Reloads all data from disk into memory.
     * Useful after external modifications to the data files.
     */
    public void reloadData() {
        movies    = FileManager.loadMovies();
        showtimes = FileManager.loadShowtimes();
        seats     = FileManager.loadSeats();
        bookings  = FileManager.loadBookings();
        linkMoviesToShowtimes();
    }

    /** Populates the transient {@link Showtime#getMovie()} reference. */
    private void linkMoviesToShowtimes() {
        for (Showtime st : showtimes) {
            for (Movie m : movies) {
                if (m.getMovieId() == st.getMovieId()) {
                    st.setMovie(m);
                    break;
                }
            }
        }
    }

    // =====================================================================
    // Public clerk operations
    // =====================================================================

    /**
     * Returns all showtimes with their linked movie details.
     *
     * <p>GUI usage: Populate a table or combo-box with this list.
     * Each item's {@link Showtime#getMovie()} provides the movie title.</p>
     *
     * @return unmodifiable list of all {@link Showtime} objects
     */
    public List<Showtime> getAvailableShowtimes() {
        return Collections.unmodifiableList(showtimes);
    }

    /**
     * Returns all seats for a given showtime with their current status.
     *
     * <p>GUI usage: Render a seating chart; colour seats by
     * {@link Seat.SeatStatus} and {@link Seat.SeatType}.</p>
     *
     * @param showtimeId the ID of the showtime to query
     * @return list of {@link Seat} objects belonging to the showtime
     * @throws BookingException if the showtime ID does not exist
     */
    public List<Seat> getSeatAvailability(int showtimeId) {
        validateShowtimeExists(showtimeId);
        List<Seat> result = new ArrayList<>();
        for (Seat s : seats) {
            if (s.getShowtimeId() == showtimeId) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Creates a new booking for a walk-in customer, marks the selected seats as
     * BOOKED, persists the changes, and returns the created {@link Booking}.
     *
     * <p>GUI usage: Collect customer name from a text field, the selected
     * showtime ID from a combo-box, and the chosen seat IDs from a seating-chart
     * widget, then call this method. Display the returned booking in a
     * confirmation dialog.</p>
     *
     * @param customerName full name of the customer (non-blank)
     * @param showtimeId   ID of the desired showtime
     * @param seatNumbers  one or more seat IDs to book (e.g., {@code ["A1","B3"]})
     * @return the newly created {@link Booking} with its generated ID and total price
     * @throws BookingException if validation fails or any requested seat is
     *                          unavailable
     */
    public Booking bookTickets(String customerName, int showtimeId,
                               List<String> seatNumbers) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new BookingException("Customer name cannot be empty.");
        }
        if (seatNumbers == null || seatNumbers.isEmpty()) {
            throw new BookingException("At least one seat must be selected.");
        }
        validateShowtimeExists(showtimeId);

        // Verify every requested seat is available
        for (String seatId : seatNumbers) {
            Seat seat = findSeat(showtimeId, seatId);
            if (seat == null) {
                throw new BookingException(
                        "Seat not found: " + seatId + " for showtime " + showtimeId + ".");
            }
            if (seat.getStatus() == Seat.SeatStatus.BOOKED) {
                throw new BookingException("Seat " + seatId + " is already booked.");
            }
        }

        // Mark seats as booked
        for (String seatId : seatNumbers) {
            findSeat(showtimeId, seatId).setStatus(Seat.SeatStatus.BOOKED);
        }

        // Build and persist the booking
        String bookingId     = generateBookingId();
        String dateTimeBooked = LocalDateTime.now().format(DT_FORMAT);
        Booking booking = new Booking(bookingId, customerName.trim(), showtimeId,
                dateTimeBooked, 0.0, seatNumbers);
        booking.setTotalPrice(calculateTotalPrice(booking));

        bookings.add(booking);
        FileManager.saveSeats(seats);
        FileManager.saveBookings(bookings);

        return booking;
    }

    /**
     * Cancels an existing booking, frees all of its seats, and removes it from
     * the data files.
     *
     * @param bookingId the unique ID of the booking to cancel (non-blank)
     * @return {@code true} always (a {@link BookingException} is thrown on failure)
     * @throws BookingException if the booking ID is blank or not found
     */
    public boolean cancelBooking(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new BookingException("Booking ID cannot be empty.");
        }
        Booking booking = findBooking(bookingId.trim());
        if (booking == null) {
            throw new BookingException("Booking not found: " + bookingId + ".");
        }

        // Free all seats
        for (String seatId : booking.getSeatNumbers()) {
            Seat seat = findSeat(booking.getShowtimeId(), seatId);
            if (seat != null) {
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
            }
        }

        bookings.remove(booking);
        FileManager.saveSeats(seats);
        FileManager.saveBookings(bookings);

        return true;
    }

    /**
     * Replaces the seats in an existing booking with a new selection.
     *
     * <p>Old seats not in the new list are freed; newly added seats are checked
     * for availability before any change is committed, so the original booking
     * is preserved if any new seat is unavailable.</p>
     *
     * @param bookingId      ID of the booking to modify (non-blank)
     * @param newSeatNumbers replacement list of seat IDs (non-empty)
     * @return {@code true} always (a {@link BookingException} is thrown on failure)
     * @throws BookingException if the booking is not found, the new seat list is
     *                          empty, or any newly requested seat is unavailable
     */
    public boolean modifyBooking(String bookingId, List<String> newSeatNumbers) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new BookingException("Booking ID cannot be empty.");
        }
        if (newSeatNumbers == null || newSeatNumbers.isEmpty()) {
            throw new BookingException("At least one seat must be selected.");
        }

        Booking booking = findBooking(bookingId.trim());
        if (booking == null) {
            throw new BookingException("Booking not found: " + bookingId + ".");
        }

        int showtimeId = booking.getShowtimeId();
        List<String> oldSeats = booking.getSeatNumbers();

        // Validate new seats that weren't already held by this booking
        for (String seatId : newSeatNumbers) {
            if (oldSeats.contains(seatId)) {
                continue; // already held – fine
            }
            Seat seat = findSeat(showtimeId, seatId);
            if (seat == null) {
                throw new BookingException("Seat not found: " + seatId + ".");
            }
            if (seat.getStatus() == Seat.SeatStatus.BOOKED) {
                throw new BookingException("Seat " + seatId + " is already booked.");
            }
        }

        // Free seats being dropped
        for (String seatId : oldSeats) {
            if (!newSeatNumbers.contains(seatId)) {
                Seat seat = findSeat(showtimeId, seatId);
                if (seat != null) {
                    seat.setStatus(Seat.SeatStatus.AVAILABLE);
                }
            }
        }

        // Reserve newly added seats
        for (String seatId : newSeatNumbers) {
            if (!oldSeats.contains(seatId)) {
                Seat seat = findSeat(showtimeId, seatId);
                if (seat != null) {
                    seat.setStatus(Seat.SeatStatus.BOOKED);
                }
            }
        }

        booking.setSeatNumbers(newSeatNumbers);
        booking.setTotalPrice(calculateTotalPrice(booking));

        FileManager.saveSeats(seats);
        FileManager.saveBookings(bookings);

        return true;
    }

    /**
     * Validates the tendered amount for a booking and returns a formatted
     * payment receipt.
     *
     * <p>GUI usage: Pass this method the {@link Booking} and the amount entered
     * by the clerk. Display the returned string in a receipt dialog or print it.</p>
     *
     * @param booking    the booking to pay for (must not be {@code null})
     * @param amountPaid the amount of money tendered by the customer
     * @return a multi-line receipt string including change information
     * @throws BookingException if {@code booking} is {@code null} or
     *                          {@code amountPaid} is less than the total price
     */
    public String processPayment(Booking booking, double amountPaid) {
        return PaymentProcessor.processPayment(booking, amountPaid);
    }

    /**
     * Generates a receipt for a booking without payment / change information.
     * Useful for reprinting after the transaction has been completed.
     *
     * @param booking the booking to receipt (must not be {@code null})
     * @return a multi-line receipt string
     * @throws BookingException if {@code booking} is {@code null}
     */
    public String generateReceipt(Booking booking) {
        return PaymentProcessor.generateReceipt(booking);
    }

    /**
     * Calculates the total price for a booking based on the type of each seat.
     *
     * <ul>
     *   <li>Standard seat: $10.00</li>
     *   <li>VIP seat: $20.00</li>
     * </ul>
     *
     * <p>If a seat cannot be found in the loaded data (e.g., data file mismatch),
     * the standard price is used as a fallback.</p>
     *
     * @param booking the booking whose price to calculate (must not be {@code null})
     * @return total price as a {@code double}
     * @throws BookingException if {@code booking} is {@code null}
     */
    public double calculateTotalPrice(Booking booking) {
        if (booking == null) {
            throw new BookingException("Booking cannot be null.");
        }
        double total = 0.0;
        for (String seatId : booking.getSeatNumbers()) {
            Seat seat = findSeat(booking.getShowtimeId(), seatId);
            total += (seat != null) ? priceFor(seat.getType()) : STANDARD_PRICE;
        }
        return total;
    }

    // =====================================================================
    // Private helpers
    // =====================================================================

    private void validateShowtimeExists(int showtimeId) {
        for (Showtime st : showtimes) {
            if (st.getShowtimeId() == showtimeId) {
                return;
            }
        }
        throw new BookingException("Showtime not found: " + showtimeId + ".");
    }

    private Seat findSeat(int showtimeId, String seatId) {
        for (Seat s : seats) {
            if (s.getShowtimeId() == showtimeId && s.getSeatId().equals(seatId)) {
                return s;
            }
        }
        return null;
    }

    private Booking findBooking(String bookingId) {
        for (Booking b : bookings) {
            if (b.getBookingId().equals(bookingId)) {
                return b;
            }
        }
        return null;
    }

    private double priceFor(Seat.SeatType type) {
        return (type == Seat.SeatType.VIP) ? VIP_PRICE : STANDARD_PRICE;
    }

    /**
     * Generates a unique booking ID using a random UUID prefix.
     *
     * @return booking ID string in the format {@code BK-<uuid-prefix>}
     */
    private String generateBookingId() {
        return "BK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
