package app.audio.Collections;

import app.audio.Files.AudioFile;
import app.audio.Files.Song;
import lombok.Getter;

import java.util.ArrayList;

public final class Album extends AudioCollection {
    @Getter
    private String name;
    private int releaseYear;
    private String description;
    @Getter
    private ArrayList<Song> songs;

    public Album(final String name, final String owner,
                 final int releaseYear, final String description,
                 final ArrayList<Song> songs) {
        super(name, owner);
        this.name = name;
        this.releaseYear = releaseYear;
        this.description = description;
        this.songs = songs;
    }

    @Override
    public int getNumberOfTracks() {
        return songs.size();
    }

    @Override
    public AudioFile getTrackByIndex(final int index) {
        return songs.get(index);
    }

    /**
     * Calculates and returns the total number of likes across all songs in the collection.
     *
     * @return The total number of likes for all songs in the collection.
     */
    public int getNumberLikes() {
        int likes = 0;
        for (Song song : this.songs) {
            likes += song.getLikes();
        }
        return likes;
    }

}
