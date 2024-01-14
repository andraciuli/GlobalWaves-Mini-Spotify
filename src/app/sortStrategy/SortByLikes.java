package app.sortStrategy;

import app.audio.Files.Song;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of the {@link Strategy} interface that sorts a list of songs based on their likes.
 */
public class SortByLikes implements Strategy {

    /**
     * Sorts the list of songs based on the number of likes in descending order.
     *
     * @param songs The list of songs to be sorted.
     */
    @Override
    public void sort(final List<Song> songs) {
        Collections.sort(songs, Comparator.comparingInt(Song::getLikes).reversed());
    }
}
