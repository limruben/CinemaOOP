package cinema.clerk;

/**
 * Represents a movie available in the cinema.
 *
 * <p>GUI usage: Display {@link #getTitle()} and {@link #getGenre()} in a list/table.
 * Use {@link #getDuration()} to show running time.</p>
 */
public class Movie {

    private int movieId;
    private String title;
    private int duration; // in minutes
    private String genre;

    /**
     * Constructs a Movie with all fields.
     *
     * @param movieId  unique identifier
     * @param title    movie title
     * @param duration running time in minutes
     * @param genre    genre label (e.g., "Action", "Drama")
     */
    public Movie(int movieId, String title, int duration, String genre) {
        this.movieId = movieId;
        this.title = title;
        this.duration = duration;
        this.genre = genre;
    }

    /** @return the movie's unique ID */
    public int getMovieId() { return movieId; }

    /** @param movieId the new movie ID */
    public void setMovieId(int movieId) { this.movieId = movieId; }

    /** @return the movie title */
    public String getTitle() { return title; }

    /** @param title the new title */
    public void setTitle(String title) { this.title = title; }

    /** @return the duration in minutes */
    public int getDuration() { return duration; }

    /** @param duration the new duration in minutes */
    public void setDuration(int duration) { this.duration = duration; }

    /** @return the genre label */
    public String getGenre() { return genre; }

    /** @param genre the new genre label */
    public void setGenre(String genre) { this.genre = genre; }

    @Override
    public String toString() {
        return String.format("Movie[id=%d, title='%s', duration=%d min, genre='%s']",
                movieId, title, duration, genre);
    }
}
