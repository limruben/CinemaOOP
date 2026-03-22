package cinema.clerk;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ticket booking made by a customer.
 *
 * <p>A booking ties one or more {@link Seat} IDs to a customer and a showtime.
 * The {@link #getTotalPrice()} reflects the sum of all booked seat prices.</p>
 *
 * <p>GUI usage: Display booking details (ID, customer name, seats, total price)
 * in a confirmation dialog or receipt panel after calling
 * {@link ClerkService#bookTickets(String, int, List)}.</p>
 */
public class Booking {

    private String bookingId;
    private String customerName;
    private int showtimeId;
    private String dateTimeBooked;
    private double totalPrice;
    /** List of seat IDs (e.g., ["A1", "B2"]) included in this booking. */
    private List<String> seatNumbers;

    /**
     * Constructs a Booking with all fields.
     *
     * @param bookingId      unique booking reference
     * @param customerName   full name of the customer
     * @param showtimeId     ID of the showtime booked
     * @param dateTimeBooked timestamp when the booking was created
     * @param totalPrice     total amount charged
     * @param seatNumbers    list of seat IDs included in this booking
     */
    public Booking(String bookingId, String customerName, int showtimeId,
                   String dateTimeBooked, double totalPrice, List<String> seatNumbers) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.showtimeId = showtimeId;
        this.dateTimeBooked = dateTimeBooked;
        this.totalPrice = totalPrice;
        this.seatNumbers = new ArrayList<>(seatNumbers);
    }

    /** @return the unique booking reference */
    public String getBookingId() { return bookingId; }

    /** @param bookingId the new booking ID */
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    /** @return the customer's full name */
    public String getCustomerName() { return customerName; }

    /** @param customerName the new customer name */
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    /** @return the ID of the showtime booked */
    public int getShowtimeId() { return showtimeId; }

    /** @param showtimeId the new showtime ID */
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }

    /** @return the booking creation timestamp */
    public String getDateTimeBooked() { return dateTimeBooked; }

    /** @param dateTimeBooked the new booking timestamp */
    public void setDateTimeBooked(String dateTimeBooked) { this.dateTimeBooked = dateTimeBooked; }

    /** @return the total price charged for this booking */
    public double getTotalPrice() { return totalPrice; }

    /** @param totalPrice the new total price */
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    /** @return a mutable copy of the seat ID list */
    public List<String> getSeatNumbers() { return seatNumbers; }

    /** @param seatNumbers the replacement seat ID list */
    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = new ArrayList<>(seatNumbers);
    }

    @Override
    public String toString() {
        return String.format(
                "Booking[id='%s', customer='%s', showtime=%d, dateBooked='%s', total=%.2f, seats=%s]",
                bookingId, customerName, showtimeId, dateTimeBooked, totalPrice, seatNumbers);
    }
}
