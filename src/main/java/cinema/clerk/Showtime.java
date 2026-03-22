package cinema.clerk;

/**
 * Represents a scheduled movie screening (showtime) in the cinema.
 *
 * <p>GUI usage: Populate a combo-box or table with the list returned by
 * {@link ClerkService#getAvailableShowtimes()}. Call {@link #getMovie()} to
 * show the linked movie details alongside the showtime.</p>
 */
public class Showtime {

    private int showtimeId;
    private int movieId;
    private String auditorium;
    private String dateTime;
    /** Transient reference populated after data is loaded. */
    private Movie movie;

    /**
     * Constructs a Showtime with all fields.
     *
     * @param showtimeId  unique identifier
     * @param movieId     ID of the movie being screened
     * @param auditorium  name or number of the screening room
     * @param dateTime    date and time of the screening (e.g., "2026-03-22 14:00")
     */
    public Showtime(int showtimeId, int movieId, String auditorium, String dateTime) {
        this.showtimeId = showtimeId;
        this.movieId = movieId;
        this.auditorium = auditorium;
        this.dateTime = dateTime;
    }

    /** @return the showtime's unique ID */
    public int getShowtimeId() { return showtimeId; }

    /** @param showtimeId the new showtime ID */
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }

    /** @return the ID of the movie being screened */
    public int getMovieId() { return movieId; }

    /** @param movieId the new movie ID reference */
    public void setMovieId(int movieId) { this.movieId = movieId; }

    /** @return the auditorium name/number */
    public String getAuditorium() { return auditorium; }

    /** @param auditorium the new auditorium name/number */
    public void setAuditorium(String auditorium) { this.auditorium = auditorium; }

    /** @return the date and time string of the screening */
    public String getDateTime() { return dateTime; }

    /** @param dateTime the new date/time string */
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    /**
     * Returns the linked Movie object (populated after loading data).
     *
     * @return the Movie, or {@code null} if not yet linked
     */
    public Movie getMovie() { return movie; }

    /** @param movie the Movie object to link */
    public void setMovie(Movie movie) { this.movie = movie; }

    @Override
    public String toString() {
        String movieTitle = (movie != null) ? movie.getTitle() : "Unknown";
        return String.format("Showtime[id=%d, movie='%s', auditorium='%s', dateTime='%s']",
                showtimeId, movieTitle, auditorium, dateTime);
    }
}
