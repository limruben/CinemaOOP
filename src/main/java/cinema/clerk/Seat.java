package cinema.clerk;

/**
 * Represents a physical seat in an auditorium for a specific showtime.
 *
 * <p>Seat identity within a showtime is the combination of {@link #getRow()} and
 * {@link #getSeatNumber()}, accessible via {@link #getSeatId()} (e.g., "A1", "B3").</p>
 *
 * <p>GUI usage: Display a seating chart by iterating the list returned by
 * {@link ClerkService#getSeatAvailability(int)}. Colour-code seats by
 * {@link SeatStatus} and {@link SeatType}.</p>
 */
public class Seat {

    /** Availability status of the seat. */
    public enum SeatStatus {
        AVAILABLE,
        BOOKED
    }

    /** Category of the seat, used for pricing. */
    public enum SeatType {
        STANDARD,
        VIP
    }

    private int showtimeId;
    private String row;
    private String seatNumber;
    private SeatStatus status;
    private SeatType type;

    /**
     * Constructs a Seat with all fields.
     *
     * @param showtimeId the showtime this seat belongs to
     * @param row        the row label (e.g., "A", "B")
     * @param seatNumber the seat number within the row (e.g., "1", "2")
     * @param status     current availability status
     * @param type       seat category (STANDARD or VIP)
     */
    public Seat(int showtimeId, String row, String seatNumber,
                SeatStatus status, SeatType type) {
        this.showtimeId = showtimeId;
        this.row = row;
        this.seatNumber = seatNumber;
        this.status = status;
        this.type = type;
    }

    /**
     * Returns the unique seat identifier within its showtime, formed by
     * concatenating the row label and the seat number (e.g., "A1", "C10").
     *
     * @return seat ID string
     */
    public String getSeatId() {
        return row + seatNumber;
    }

    /** @return the showtime ID this seat belongs to */
    public int getShowtimeId() { return showtimeId; }

    /** @param showtimeId the new showtime ID */
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }

    /** @return the row label */
    public String getRow() { return row; }

    /** @param row the new row label */
    public void setRow(String row) { this.row = row; }

    /** @return the seat number within the row */
    public String getSeatNumber() { return seatNumber; }

    /** @param seatNumber the new seat number */
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    /** @return the current availability status */
    public SeatStatus getStatus() { return status; }

    /** @param status the new status */
    public void setStatus(SeatStatus status) { this.status = status; }

    /** @return the seat type (STANDARD or VIP) */
    public SeatType getType() { return type; }

    /** @param type the new seat type */
    public void setType(SeatType type) { this.type = type; }

    @Override
    public String toString() {
        return String.format("Seat[showtime=%d, id='%s', status=%s, type=%s]",
                showtimeId, getSeatId(), status, type);
    }
}
