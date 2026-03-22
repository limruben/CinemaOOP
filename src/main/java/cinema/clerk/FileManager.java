package cinema.clerk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file I/O for the cinema system.
 *
 * <p>Data is stored in plain-text CSV files inside a configurable data
 * directory (default: {@code data/} relative to the working directory).
 * Call {@link #setDataDirectory(String)} before constructing a
 * {@link ClerkService} when you need a different location (e.g., in tests).</p>
 *
 * <h3>File formats</h3>
 * <ul>
 *   <li>{@code movies.txt}    – {@code movieId,title,duration,genre}</li>
 *   <li>{@code showtimes.txt} – {@code showtimeId,movieId,auditorium,dateTime}</li>
 *   <li>{@code seats.txt}     – {@code showtimeId,row,seatNumber,status,type}</li>
 *   <li>{@code bookings.txt}  – {@code bookingId,customerName,showtimeId,dateTimeBooked,totalPrice,seat1;seat2;...}</li>
 * </ul>
 *
 * <p>Commas inside field values are escaped as {@code \,} and back-slashes as
 * {@code \\}. Seat IDs in the bookings file are separated with {@code ;} to
 * avoid ambiguity with the outer comma delimiter.</p>
 */
public class FileManager {

    /** Delimiter used between CSV fields. */
    private static final String DELIMITER = ",";

    /** Delimiter used to separate multiple seat IDs within a booking record. */
    private static final String SEATS_DELIMITER = ";";

    /** Directory that holds all data files (default relative path). */
    private static String dataDir = "data";

    // =====================================================================
    // Configuration
    // =====================================================================

    /**
     * Overrides the data directory used by all file operations.
     * Must be called before any load/save method is invoked.
     *
     * @param dir path to the data directory (absolute or relative to the JVM
     *            working directory)
     */
    public static void setDataDirectory(String dir) {
        dataDir = dir;
    }

    /** @return the currently configured data directory path */
    public static String getDataDirectory() {
        return dataDir;
    }

    // =====================================================================
    // Path helpers
    // =====================================================================

    private static String moviesFile()    { return dataDir + File.separator + "movies.txt"; }
    private static String showtimesFile() { return dataDir + File.separator + "showtimes.txt"; }
    private static String seatsFile()     { return dataDir + File.separator + "seats.txt"; }
    private static String bookingsFile()  { return dataDir + File.separator + "bookings.txt"; }

    // =====================================================================
    // Initialisation
    // =====================================================================

    /**
     * Ensures the data directory and all required files exist.
     * Safe to call multiple times.
     */
    public static void initialize() {
        File dir = new File(dataDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BookingException("Could not create data directory: " + dataDir);
        }
        createIfAbsent(moviesFile());
        createIfAbsent(showtimesFile());
        createIfAbsent(seatsFile());
        createIfAbsent(bookingsFile());
    }

    private static void createIfAbsent(String path) {
        File f = new File(path);
        if (!f.exists()) {
            try {
                if (!f.createNewFile()) {
                    throw new BookingException("Could not create file: " + path);
                }
            } catch (IOException e) {
                throw new BookingException("Could not create file: " + path, e);
            }
        }
    }

    // =====================================================================
    // Movies
    // =====================================================================

    /**
     * Loads all movies from {@code movies.txt}.
     * Lines starting with {@code #} and blank lines are ignored.
     *
     * @return list of {@link Movie} objects; never {@code null}
     * @throws BookingException on I/O error
     */
    public static List<Movie> loadMovies() {
        List<Movie> movies = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(moviesFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] p = line.split(DELIMITER, -1);
                if (p.length < 4) {
                    continue;
                }
                int id       = Integer.parseInt(p[0].trim());
                String title = unescape(p[1].trim());
                int dur      = Integer.parseInt(p[2].trim());
                String genre = unescape(p[3].trim());
                movies.add(new Movie(id, title, dur, genre));
            }
        } catch (IOException e) {
            throw new BookingException("Failed to load movies: " + e.getMessage(), e);
        }
        return movies;
    }

    /**
     * Saves all movies to {@code movies.txt}, overwriting the previous contents.
     *
     * @param movies list of {@link Movie} objects to persist
     * @throws BookingException on I/O error
     */
    public static void saveMovies(List<Movie> movies) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(moviesFile()))) {
            for (Movie m : movies) {
                bw.write(m.getMovieId()
                        + DELIMITER + escape(m.getTitle())
                        + DELIMITER + m.getDuration()
                        + DELIMITER + escape(m.getGenre()));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new BookingException("Failed to save movies: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // Showtimes
    // =====================================================================

    /**
     * Loads all showtimes from {@code showtimes.txt}.
     *
     * @return list of {@link Showtime} objects; never {@code null}
     * @throws BookingException on I/O error
     */
    public static List<Showtime> loadShowtimes() {
        List<Showtime> showtimes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(showtimesFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] p = line.split(DELIMITER, -1);
                if (p.length < 4) {
                    continue;
                }
                int id          = Integer.parseInt(p[0].trim());
                int movieId     = Integer.parseInt(p[1].trim());
                String auditor  = unescape(p[2].trim());
                String dateTime = unescape(p[3].trim());
                showtimes.add(new Showtime(id, movieId, auditor, dateTime));
            }
        } catch (IOException e) {
            throw new BookingException("Failed to load showtimes: " + e.getMessage(), e);
        }
        return showtimes;
    }

    /**
     * Saves all showtimes to {@code showtimes.txt}, overwriting the previous contents.
     *
     * @param showtimes list of {@link Showtime} objects to persist
     * @throws BookingException on I/O error
     */
    public static void saveShowtimes(List<Showtime> showtimes) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(showtimesFile()))) {
            for (Showtime s : showtimes) {
                bw.write(s.getShowtimeId()
                        + DELIMITER + s.getMovieId()
                        + DELIMITER + escape(s.getAuditorium())
                        + DELIMITER + escape(s.getDateTime()));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new BookingException("Failed to save showtimes: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // Seats
    // =====================================================================

    /**
     * Loads all seats from {@code seats.txt}.
     * Expected format per line: {@code showtimeId,row,seatNumber,status,type}
     *
     * @return list of {@link Seat} objects; never {@code null}
     * @throws BookingException on I/O error or unknown enum value
     */
    public static List<Seat> loadSeats() {
        List<Seat> seats = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(seatsFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] p = line.split(DELIMITER, -1);
                if (p.length < 5) {
                    continue;
                }
                int showtimeId       = Integer.parseInt(p[0].trim());
                String row           = p[1].trim();
                String seatNumber    = p[2].trim();
                Seat.SeatStatus status = Seat.SeatStatus.valueOf(p[3].trim());
                Seat.SeatType type     = Seat.SeatType.valueOf(p[4].trim());
                seats.add(new Seat(showtimeId, row, seatNumber, status, type));
            }
        } catch (IOException e) {
            throw new BookingException("Failed to load seats: " + e.getMessage(), e);
        }
        return seats;
    }

    /**
     * Saves all seats to {@code seats.txt}, overwriting the previous contents.
     *
     * @param seats list of {@link Seat} objects to persist
     * @throws BookingException on I/O error
     */
    public static void saveSeats(List<Seat> seats) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(seatsFile()))) {
            for (Seat s : seats) {
                bw.write(s.getShowtimeId()
                        + DELIMITER + s.getRow()
                        + DELIMITER + s.getSeatNumber()
                        + DELIMITER + s.getStatus().name()
                        + DELIMITER + s.getType().name());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new BookingException("Failed to save seats: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // Bookings
    // =====================================================================

    /**
     * Loads all bookings from {@code bookings.txt}.
     * Expected format per line:
     * {@code bookingId,customerName,showtimeId,dateTimeBooked,totalPrice,seat1;seat2;...}
     *
     * @return list of {@link Booking} objects; never {@code null}
     * @throws BookingException on I/O error
     */
    public static List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(bookingsFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                // Limit split to 6 parts so the seats field (last) is kept intact
                String[] p = line.split(DELIMITER, 6);
                if (p.length < 6) {
                    continue;
                }
                String bookingId     = p[0].trim();
                String customerName  = unescape(p[1].trim());
                int showtimeId       = Integer.parseInt(p[2].trim());
                String dateTimeBooked = unescape(p[3].trim());
                double totalPrice    = Double.parseDouble(p[4].trim());
                String seatsRaw      = p[5].trim();

                List<String> seatNumbers = new ArrayList<>();
                if (!seatsRaw.isEmpty()) {
                    for (String seatId : seatsRaw.split(SEATS_DELIMITER)) {
                        String trimmed = seatId.trim();
                        if (!trimmed.isEmpty()) {
                            seatNumbers.add(trimmed);
                        }
                    }
                }
                bookings.add(new Booking(bookingId, customerName, showtimeId,
                        dateTimeBooked, totalPrice, seatNumbers));
            }
        } catch (IOException e) {
            throw new BookingException("Failed to load bookings: " + e.getMessage(), e);
        }
        return bookings;
    }

    /**
     * Saves all bookings to {@code bookings.txt}, overwriting the previous contents.
     *
     * @param bookings list of {@link Booking} objects to persist
     * @throws BookingException on I/O error
     */
    public static void saveBookings(List<Booking> bookings) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(bookingsFile()))) {
            for (Booking b : bookings) {
                String seatsList = String.join(SEATS_DELIMITER, b.getSeatNumbers());
                bw.write(b.getBookingId()
                        + DELIMITER + escape(b.getCustomerName())
                        + DELIMITER + b.getShowtimeId()
                        + DELIMITER + escape(b.getDateTimeBooked())
                        + DELIMITER + String.format("%.2f", b.getTotalPrice())
                        + DELIMITER + seatsList);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new BookingException("Failed to save bookings: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // CSV escaping helpers
    // =====================================================================

    /**
     * Escapes a field value so it is safe to store as a CSV token.
     * Back-slashes are doubled and commas are replaced with {@code \,}.
     *
     * @param value raw field value (may be {@code null})
     * @return escaped string, or empty string if {@code value} is {@code null}
     */
    static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace(",", "\\,");
    }

    /**
     * Reverses the escaping applied by {@link #escape(String)}.
     *
     * @param value escaped field value (may be {@code null})
     * @return unescaped string, or empty string if {@code value} is {@code null}
     */
    static String unescape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\,", ",").replace("\\\\", "\\");
    }
}
