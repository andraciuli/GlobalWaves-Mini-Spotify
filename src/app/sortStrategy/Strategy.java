package app.sortStrategy;

import app.audio.Files.Song;

import java.util.List;

/**
 * The Strategy interface defines a contract for sorting a list of songs.
 */
public interface Strategy {

    /**
     * Sorts the list of songs based on a specific strategy.
     *
     * @param songs The list of songs to be sorted.
     */
    void sort(List<Song> songs);
}
