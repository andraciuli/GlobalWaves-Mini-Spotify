package app.sortStrategy;

import app.audio.Files.Song;
import java.util.List;

/**
 * An implementation of the Strategy interface that sorts songs based on
 * their names in ascending order.
 */
public class SortByName implements Strategy {
    /**
     * Sorts the list of songs based on their names in ascending order.
     *
     * @param songs The list of songs to be sorted.
     */
    @Override
    public void sort(final List<Song> songs) {
        songs.sort((song1, song2) -> song1.getName().compareTo(song2.getName()));
    }
}
