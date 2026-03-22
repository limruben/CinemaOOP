package cinema.clerk;

/**
 * Handles payment simulation and receipt generation for cinema bookings.
 *
 * <p>All methods are static; there is no state to manage. The class is kept
 * separate from {@link ClerkService} so that payment logic can be swapped or
 * extended independently (e.g., to integrate a real payment gateway) without
 * touching the service layer.</p>
 *
 * <p>GUI usage: Call {@link #processPayment(Booking, double)} when the clerk
 * clicks "Confirm Payment". Display the returned receipt string in a dialog
 * or text area.</p>
 */
public class PaymentProcessor {

    private PaymentProcessor() {
        // Utility class – no instantiation needed.
    }

    /**
     * Validates the tendered amount and produces a payment receipt.
     *
     * @param booking    the booking being paid for (must not be {@code null})
     * @param amountPaid the amount of money the customer hands over
     * @return a formatted receipt string including change information
     * @throws BookingException if {@code booking} is {@code null} or the
     *                          tendered amount is less than the total price
     */
    public static String processPayment(Booking booking, double amountPaid) {
        if (booking == null) {
            throw new BookingException("Booking cannot be null.");
        }
        if (amountPaid < booking.getTotalPrice()) {
            throw new BookingException(String.format(
                    "Insufficient payment. Required: $%.2f, Provided: $%.2f",
                    booking.getTotalPrice(), amountPaid));
        }
        double change = amountPaid - booking.getTotalPrice();
        return buildReceipt(booking, amountPaid, change);
    }

    /**
     * Formats a booking receipt without payment / change information.
     * Useful for reprinting a receipt after the payment session has ended.
     *
     * @param booking the booking to generate a receipt for (must not be {@code null})
     * @return formatted receipt string
     * @throws BookingException if {@code booking} is {@code null}
     */
    public static String generateReceipt(Booking booking) {
        if (booking == null) {
            throw new BookingException("Booking cannot be null.");
        }
        return buildReceipt(booking, booking.getTotalPrice(), 0.0);
    }

    // =====================================================================
    // Private helpers
    // =====================================================================

    /**
     * Builds the full receipt text.
     *
     * @param booking    the booking details
     * @param amountPaid amount tendered
     * @param change     change to return to the customer
     * @return multi-line receipt string
     */
    private static String buildReceipt(Booking booking, double amountPaid, double change) {
        String sep  = "============================================";
        String line = "--------------------------------------------";
        StringBuilder sb = new StringBuilder();
        sb.append(sep).append('\n');
        sb.append("           CINEMA TICKET RECEIPT            \n");
        sb.append(sep).append('\n');
        sb.append(String.format("Booking ID   : %s%n",  booking.getBookingId()));
        sb.append(String.format("Customer     : %s%n",  booking.getCustomerName()));
        sb.append(String.format("Showtime ID  : %d%n",  booking.getShowtimeId()));
        sb.append(String.format("Date Booked  : %s%n",  booking.getDateTimeBooked()));
        sb.append(line).append('\n');
        sb.append(String.format("Seats        : %s%n",  String.join(", ", booking.getSeatNumbers())));
        sb.append(line).append('\n');
        sb.append(String.format("Total Price  : $%.2f%n", booking.getTotalPrice()));
        sb.append(String.format("Amount Paid  : $%.2f%n", amountPaid));
        sb.append(String.format("Change       : $%.2f%n", change));
        sb.append(sep).append('\n');
        sb.append("     Thank you for choosing our cinema!     \n");
        sb.append(sep).append('\n');
        return sb.toString();
    }
}
