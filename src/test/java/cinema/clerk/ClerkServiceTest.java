package cinema.clerk;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ClerkService}.
 *
 * <p>Each test uses a fresh temporary directory so that the real {@code data/}
 * folder is never touched and tests are fully isolated.</p>
 */
public class ClerkServiceTest {

    /** Temporary directory used as the data directory for each test. */
    private File tempDir;
    private ClerkService service;

    // =====================================================================
    // Setup / Teardown
    // =====================================================================

    @Before
    public void setUp() throws IOException {
        // Create a unique temp directory for each test
        tempDir = Files.createTempDirectory("cinema_test_").toFile();
        FileManager.setDataDirectory(tempDir.getAbsolutePath());

        // Write minimal data files
        writeFile("movies.txt",
                "1,Test Movie,120,Action\n"
                + "2,Drama Film,90,Drama\n");

        writeFile("showtimes.txt",
                "1,1,Auditorium A,2026-03-22 14:00\n"
                + "2,2,Auditorium B,2026-03-23 18:00\n");

        // Showtime 1: rows A (STANDARD) and C (VIP), 3 seats each
        writeFile("seats.txt",
                "1,A,1,AVAILABLE,STANDARD\n"
                + "1,A,2,AVAILABLE,STANDARD\n"
                + "1,A,3,AVAILABLE,STANDARD\n"
                + "1,C,1,AVAILABLE,VIP\n"
                + "1,C,2,AVAILABLE,VIP\n"
                + "1,C,3,AVAILABLE,VIP\n"
                + "2,A,1,AVAILABLE,STANDARD\n"
                + "2,A,2,AVAILABLE,STANDARD\n");

        writeFile("bookings.txt", "");

        service = new ClerkService();
    }

    @After
    public void tearDown() {
        // Reset to default so other (non-test) code still works
        FileManager.setDataDirectory("data");
        // Clean up temp directory
        deleteDirectory(tempDir);
    }

    // =====================================================================
    // getAvailableShowtimes
    // =====================================================================

    @Test
    public void getAvailableShowtimes_returnsAllShowtimes() {
        List<Showtime> showtimes = service.getAvailableShowtimes();
        assertEquals(2, showtimes.size());
    }

    @Test
    public void getAvailableShowtimes_linksMovieTitles() {
        List<Showtime> showtimes = service.getAvailableShowtimes();
        Showtime st1 = showtimes.get(0);
        assertNotNull("Movie should be linked", st1.getMovie());
        assertEquals("Test Movie", st1.getMovie().getTitle());
    }

    // =====================================================================
    // getSeatAvailability
    // =====================================================================

    @Test
    public void getSeatAvailability_returnsSeatsForShowtime() {
        List<Seat> seats = service.getSeatAvailability(1);
        assertEquals(6, seats.size()); // 3 STANDARD + 3 VIP
    }

    @Test(expected = BookingException.class)
    public void getSeatAvailability_throwsForUnknownShowtime() {
        service.getSeatAvailability(999);
    }

    // =====================================================================
    // bookTickets
    // =====================================================================

    @Test
    public void bookTickets_createsBookingAndMarksSeatBooked() {
        Booking booking = service.bookTickets("John Doe", 1, Arrays.asList("A1", "A2"));

        assertNotNull(booking);
        assertEquals("John Doe", booking.getCustomerName());
        assertEquals(1, booking.getShowtimeId());
        assertEquals(2, booking.getSeatNumbers().size());
        assertTrue(booking.getBookingId().startsWith("BK-"));

        // Verify seat status in memory
        List<Seat> seats = service.getSeatAvailability(1);
        for (Seat s : seats) {
            if (s.getSeatId().equals("A1") || s.getSeatId().equals("A2")) {
                assertEquals(Seat.SeatStatus.BOOKED, s.getStatus());
            }
        }
    }

    @Test
    public void bookTickets_calculatesCorrectPriceForStandardSeats() {
        Booking booking = service.bookTickets("Jane Doe", 1, Arrays.asList("A1", "A2"));
        assertEquals(ClerkService.STANDARD_PRICE * 2, booking.getTotalPrice(), 0.001);
    }

    @Test
    public void bookTickets_calculatesCorrectPriceForVipSeats() {
        Booking booking = service.bookTickets("VIP Customer", 1, Arrays.asList("C1", "C2"));
        assertEquals(ClerkService.VIP_PRICE * 2, booking.getTotalPrice(), 0.001);
    }

    @Test
    public void bookTickets_calculatesMixedSeatPrice() {
        Booking booking = service.bookTickets("Mixed Customer", 1,
                Arrays.asList("A1", "C1")); // 1 STANDARD + 1 VIP
        double expected = ClerkService.STANDARD_PRICE + ClerkService.VIP_PRICE;
        assertEquals(expected, booking.getTotalPrice(), 0.001);
    }

    @Test(expected = BookingException.class)
    public void bookTickets_throwsForBlankCustomerName() {
        service.bookTickets("  ", 1, Arrays.asList("A1"));
    }

    @Test(expected = BookingException.class)
    public void bookTickets_throwsForEmptySeatList() {
        service.bookTickets("Customer", 1, Arrays.asList());
    }

    @Test(expected = BookingException.class)
    public void bookTickets_throwsForUnknownShowtime() {
        service.bookTickets("Customer", 999, Arrays.asList("A1"));
    }

    @Test(expected = BookingException.class)
    public void bookTickets_throwsForUnknownSeat() {
        service.bookTickets("Customer", 1, Arrays.asList("Z9"));
    }

    @Test(expected = BookingException.class)
    public void bookTickets_throwsForAlreadyBookedSeat() {
        service.bookTickets("First", 1, Arrays.asList("A1"));
        service.bookTickets("Second", 1, Arrays.asList("A1")); // should fail
    }

    // =====================================================================
    // cancelBooking
    // =====================================================================

    @Test
    public void cancelBooking_removesBookingAndFreesSeats() {
        Booking booking = service.bookTickets("Alice", 1, Arrays.asList("A1"));
        String id = booking.getBookingId();

        boolean result = service.cancelBooking(id);

        assertTrue(result);
        // Seat should be available again
        List<Seat> seats = service.getSeatAvailability(1);
        for (Seat s : seats) {
            if (s.getSeatId().equals("A1")) {
                assertEquals(Seat.SeatStatus.AVAILABLE, s.getStatus());
            }
        }
    }

    @Test(expected = BookingException.class)
    public void cancelBooking_throwsForUnknownId() {
        service.cancelBooking("DOES_NOT_EXIST");
    }

    @Test(expected = BookingException.class)
    public void cancelBooking_throwsForBlankId() {
        service.cancelBooking("  ");
    }

    // =====================================================================
    // modifyBooking
    // =====================================================================

    @Test
    public void modifyBooking_replacesSeatsAndUpdatesPrice() {
        Booking booking = service.bookTickets("Bob", 1, Arrays.asList("A1", "A2"));
        String id = booking.getBookingId();

        // Replace A2 with A3
        boolean result = service.modifyBooking(id, Arrays.asList("A1", "A3"));

        assertTrue(result);
        assertEquals(2, booking.getSeatNumbers().size());
        assertTrue(booking.getSeatNumbers().contains("A1"));
        assertTrue(booking.getSeatNumbers().contains("A3"));
        assertFalse(booking.getSeatNumbers().contains("A2"));

        // A2 should be free again
        List<Seat> seats = service.getSeatAvailability(1);
        for (Seat s : seats) {
            if (s.getSeatId().equals("A2")) {
                assertEquals(Seat.SeatStatus.AVAILABLE, s.getStatus());
            }
            if (s.getSeatId().equals("A3")) {
                assertEquals(Seat.SeatStatus.BOOKED, s.getStatus());
            }
        }
    }

    @Test(expected = BookingException.class)
    public void modifyBooking_throwsWhenNewSeatAlreadyBooked() {
        service.bookTickets("Customer A", 1, Arrays.asList("A3")); // A3 now booked
        Booking booking = service.bookTickets("Customer B", 1, Arrays.asList("A1"));
        service.modifyBooking(booking.getBookingId(), Arrays.asList("A3")); // conflict
    }

    @Test(expected = BookingException.class)
    public void modifyBooking_throwsForUnknownBookingId() {
        service.modifyBooking("UNKNOWN_ID", Arrays.asList("A1"));
    }

    // =====================================================================
    // calculateTotalPrice
    // =====================================================================

    @Test
    public void calculateTotalPrice_correctForStandard() {
        Booking b = new Booking("BK1", "Test", 1, "2026-01-01 10:00", 0,
                Arrays.asList("A1", "A2", "A3"));
        double price = service.calculateTotalPrice(b);
        assertEquals(3 * ClerkService.STANDARD_PRICE, price, 0.001);
    }

    @Test
    public void calculateTotalPrice_correctForVip() {
        Booking b = new Booking("BK2", "Test", 1, "2026-01-01 10:00", 0,
                Arrays.asList("C1", "C2"));
        double price = service.calculateTotalPrice(b);
        assertEquals(2 * ClerkService.VIP_PRICE, price, 0.001);
    }

    @Test(expected = BookingException.class)
    public void calculateTotalPrice_throwsForNullBooking() {
        service.calculateTotalPrice(null);
    }

    // =====================================================================
    // processPayment & generateReceipt
    // =====================================================================

    @Test
    public void processPayment_returnsReceiptWithChange() {
        Booking booking = service.bookTickets("Carol", 1, Arrays.asList("A1"));
        double total = booking.getTotalPrice(); // $10.00
        String receipt = service.processPayment(booking, total + 5.0);

        assertNotNull(receipt);
        assertTrue(receipt.contains("Carol"));
        assertTrue(receipt.contains("$5.00")); // change
    }

    @Test(expected = BookingException.class)
    public void processPayment_throwsWhenInsufficientAmount() {
        Booking booking = service.bookTickets("Dave", 1, Arrays.asList("A1"));
        service.processPayment(booking, 0.01); // less than $10.00
    }

    @Test
    public void generateReceipt_containsBookingDetails() {
        Booking booking = service.bookTickets("Eve", 1, Arrays.asList("A1"));
        String receipt = service.generateReceipt(booking);

        assertNotNull(receipt);
        assertTrue(receipt.contains(booking.getBookingId()));
        assertTrue(receipt.contains("Eve"));
        assertTrue(receipt.contains("A1"));
    }

    // =====================================================================
    // Persistence (file round-trip)
    // =====================================================================

    @Test
    public void bookTickets_persistsToFile() {
        Booking booking = service.bookTickets("Frank", 1, Arrays.asList("A1", "C1"));

        // Create a fresh service instance reading the same temp dir
        ClerkService service2 = new ClerkService();
        List<Seat> seats = service2.getSeatAvailability(1);

        boolean a1Booked = false;
        boolean c1Booked = false;
        for (Seat s : seats) {
            if (s.getSeatId().equals("A1")) a1Booked = s.getStatus() == Seat.SeatStatus.BOOKED;
            if (s.getSeatId().equals("C1")) c1Booked = s.getStatus() == Seat.SeatStatus.BOOKED;
        }
        assertTrue("A1 should be BOOKED after reload", a1Booked);
        assertTrue("C1 should be BOOKED after reload", c1Booked);
    }

    @Test
    public void cancelBooking_persistsCancellationToFile() {
        Booking booking = service.bookTickets("Grace", 1, Arrays.asList("A2"));
        service.cancelBooking(booking.getBookingId());

        // Re-load and verify seat is free
        ClerkService service2 = new ClerkService();
        List<Seat> seats = service2.getSeatAvailability(1);
        for (Seat s : seats) {
            if (s.getSeatId().equals("A2")) {
                assertEquals(Seat.SeatStatus.AVAILABLE, s.getStatus());
            }
        }
    }

    // =====================================================================
    // FileManager helpers (escape / unescape)
    // =====================================================================

    @Test
    public void fileManager_escapeAndUnescape_roundtrip() {
        String original = "O'Brien, Inc. \\ Test";
        String escaped  = FileManager.escape(original);
        String restored = FileManager.unescape(escaped);
        assertEquals(original, restored);
    }

    @Test
    public void fileManager_escapeComma() {
        String escaped = FileManager.escape("Hello, World");
        assertFalse("Escaped value should not contain bare comma",
                escaped.contains(",") && !escaped.contains("\\,"));
        assertTrue(escaped.contains("\\,"));
    }

    // =====================================================================
    // Private helpers
    // =====================================================================

    private void writeFile(String filename, String content) throws IOException {
        try (FileWriter fw = new FileWriter(new File(tempDir, filename))) {
            fw.write(content);
        }
    }

    private void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }
}
