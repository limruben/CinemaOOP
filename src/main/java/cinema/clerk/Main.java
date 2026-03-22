package cinema.clerk;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Console demonstration entry point for the Ticketing Clerk module.
 *
 * <p>Run this class to exercise the {@link ClerkService} interactively from
 * the command line. It shows available showtimes, lets you book seats, cancel
 * a booking, modify it, and process a payment.</p>
 *
 * <p><strong>Not intended for production use.</strong> The GUI front-end will
 * replace this class and call {@link ClerkService} directly from Swing event
 * handlers.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Cinema Ticketing Clerk – Console Demo ===\n");

        ClerkService service = new ClerkService();

        // ── 1. List showtimes ──────────────────────────────────────────
        System.out.println("--- Available Showtimes ---");
        List<Showtime> showtimes = service.getAvailableShowtimes();
        if (showtimes.isEmpty()) {
            System.out.println("No showtimes loaded. Please add data to data/showtimes.txt "
                    + "and data/movies.txt.");
            return;
        }
        for (Showtime st : showtimes) {
            System.out.println(st);
        }

        // ── 2. Show seat availability for the first showtime ───────────
        int firstShowtimeId = showtimes.get(0).getShowtimeId();
        System.out.println("\n--- Seat Availability for Showtime " + firstShowtimeId + " ---");
        List<Seat> seats = service.getSeatAvailability(firstShowtimeId);
        for (Seat seat : seats) {
            System.out.println(seat);
        }

        // ── 3. Book two seats ──────────────────────────────────────────
        System.out.println("\n--- Booking Seats A1, A2 for 'Alice Smith' ---");
        try {
            Booking booking = service.bookTickets("Alice Smith", firstShowtimeId,
                    Arrays.asList("A1", "A2"));
            System.out.println("Booking created: " + booking);

            // ── 4. Generate a receipt ──────────────────────────────────
            System.out.println("\n--- Receipt (reprint, no payment) ---");
            System.out.println(service.generateReceipt(booking));

            // ── 5. Process payment ────────────────────────────────────
            System.out.println("--- Processing Payment ($30.00 tendered) ---");
            String receipt = service.processPayment(booking, 30.00);
            System.out.println(receipt);

            // ── 6. Modify booking (swap A2 → A3) ──────────────────────
            System.out.println("--- Modifying Booking (A1, A3 instead of A1, A2) ---");
            boolean modified = service.modifyBooking(booking.getBookingId(),
                    Arrays.asList("A1", "A3"));
            System.out.println("Modification successful: " + modified);
            System.out.println("Updated booking: " + booking);

            // ── 7. Cancel booking ─────────────────────────────────────
            System.out.println("\n--- Cancelling Booking ---");
            boolean cancelled = service.cancelBooking(booking.getBookingId());
            System.out.println("Cancellation successful: " + cancelled);

        } catch (BookingException e) {
            System.err.println("Booking error: " + e.getMessage());
        }

        // ── 8. Interactive mode (optional) ────────────────────────────
        System.out.println("\n--- Interactive Booking ---");
        interactiveBook(service, showtimes.get(0).getShowtimeId());
    }

    // ------------------------------------------------------------------
    // Private console helper
    // ------------------------------------------------------------------

    private static void interactiveBook(ClerkService service, int showtimeId) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter customer name (or press Enter to skip): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Skipping interactive booking.");
            scanner.close();
            return;
        }

        System.out.println("Available seats for showtime " + showtimeId + ":");
        List<Seat> available = service.getSeatAvailability(showtimeId);
        for (Seat s : available) {
            if (s.getStatus() == Seat.SeatStatus.AVAILABLE) {
                System.out.printf("  %-5s [%s]%n", s.getSeatId(), s.getType());
            }
        }

        System.out.print("Enter seat IDs separated by spaces (e.g. B1 B2): ");
        String seatInput = scanner.nextLine().trim();
        if (seatInput.isEmpty()) {
            System.out.println("No seats selected. Exiting.");
            scanner.close();
            return;
        }

        List<String> seatIds = Arrays.asList(seatInput.split("\\s+"));
        try {
            Booking booking = service.bookTickets(name, showtimeId, seatIds);
            System.out.println("\nBooking confirmed: " + booking);

            System.out.print("Enter amount paid: $");
            double paid = Double.parseDouble(scanner.nextLine().trim());
            String receipt = service.processPayment(booking, paid);
            System.out.println(receipt);
        } catch (BookingException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid amount entered.");
        }
        scanner.close();
    }
}
