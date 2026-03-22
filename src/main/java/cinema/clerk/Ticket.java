package cinema.clerk;

/**
 * Represents an individual ticket for a single seat within a booking.
 *
 * <p>A {@link Booking} can contain multiple tickets, one per reserved seat.
 * This class holds the per-seat price and type information for receipt
 * line-item display.</p>
 *
 * <p>GUI usage: Iterate the list of tickets to render individual ticket details
 * (seat, type, price) in a receipt or print-preview component.</p>
 */
public class Ticket {

    private String ticketId;
    private String bookingId;
    private String seatId;
    private Seat.SeatType seatType;
    private double price;

    /**
     * Constructs a Ticket with all fields.
     *
     * @param ticketId  unique ticket identifier
     * @param bookingId ID of the parent booking
     * @param seatId    seat ID (e.g., "A1")
     * @param seatType  seat category (STANDARD or VIP)
     * @param price     price charged for this ticket
     */
    public Ticket(String ticketId, String bookingId, String seatId,
                  Seat.SeatType seatType, double price) {
        this.ticketId = ticketId;
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.seatType = seatType;
        this.price = price;
    }

    /** @return the unique ticket ID */
    public String getTicketId() { return ticketId; }

    /** @param ticketId the new ticket ID */
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    /** @return the parent booking ID */
    public String getBookingId() { return bookingId; }

    /** @param bookingId the new booking ID reference */
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    /** @return the seat ID this ticket is for */
    public String getSeatId() { return seatId; }

    /** @param seatId the new seat ID */
    public void setSeatId(String seatId) { this.seatId = seatId; }

    /** @return the seat type (STANDARD or VIP) */
    public Seat.SeatType getSeatType() { return seatType; }

    /** @param seatType the new seat type */
    public void setSeatType(Seat.SeatType seatType) { this.seatType = seatType; }

    /** @return the price charged for this ticket */
    public double getPrice() { return price; }

    /** @param price the new ticket price */
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return String.format("Ticket[id='%s', booking='%s', seat='%s', type=%s, price=%.2f]",
                ticketId, bookingId, seatId, seatType, price);
    }
}
