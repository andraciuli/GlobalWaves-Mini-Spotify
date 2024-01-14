package app.wrapped;

import app.audio.Collections.Album;
import lombok.Getter;
import lombok.Setter;

/**
 * A wrapper class for representing an album along with its listen count.
 */
public class AlbumWrapp {

    @Getter
    @Setter
    private Album album;

    @Getter
    @Setter
    private int listens;

    /**
     * Constructs an AlbumWrapp object with the specified album.
     *
     * @param album The album to be wrapped.
     */
    public AlbumWrapp(final Album album) {
        this.album = album;
        listens = 1;
    }

    /**
     * Increments the listen count for the wrapped album.
     */
    public void incrementListens() {
        listens++;
    }
}
