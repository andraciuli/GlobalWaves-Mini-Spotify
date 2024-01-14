package app.wrapped;

import app.audio.Files.Episode;
import lombok.Getter;
import lombok.Setter;

/**
 * A wrapper class for representing an episode along with its listen count.
 */
public class EpisodeWrapp {

    @Getter
    @Setter
    private Episode episode;

    @Getter
    @Setter
    private int listens;

    /**
     * Constructs an EpisodeWrapp object with the specified episode.
     *
     * @param episode The episode to be wrapped.
     */
    public EpisodeWrapp(final Episode episode) {
        this.episode = episode;
        listens = 1;
    }

    /**
     * Increments the listen count for the wrapped episode.
     */
    public void incrementListens() {
        listens++;
    }
}
