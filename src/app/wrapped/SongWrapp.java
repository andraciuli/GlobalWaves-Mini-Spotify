package app.wrapped;

import app.audio.Files.Song;
import lombok.Getter;
import lombok.Setter;

/**
 * A class representing a wrapped version of a song, including the song and the number of listens.
 */
public class SongWrapp {

    @Getter
    @Setter
    private Song song;

    @Getter
    @Setter
    private int listens;

    /**
     * Constructs a SongWrapp instance for the given song with an initial listen count of 1.
     *
     * @param song The song to wrap.
     */
    public SongWrapp(final Song song) {
        this.song = song;
        listens = 1;
    }

    /**
     * Increments the listen count for the wrapped song.
     */
    public void incrementListens() {
        listens++;
    }
}
